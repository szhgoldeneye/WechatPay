/**
 * Created by He on 2016/5/18.
 */

import java.io.BufferedOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import constant.GlobalConfig;
import service.WechatPayService;
import utils.QRCodeUtil;
import utils.RequestHandler;
import utils.ResponseHandler;
import utils.TenpayUtil;

@RestController
@RequestMapping("/api/weixin")
public class WechatPayController extends AbstractController {

	@RequestMapping("/pay")
	public RestResponse<String> getCodeUrl(HttpServletResponse response, HttpServletRequest request)
			throws Exception {

		String appid = GlobalConfig.APPID;
		String mch_id = GlobalConfig.MCH_ID;
		String appsecret = GlobalConfig.APPSECRET;
		String key = GlobalConfig.KEY;

		String currTime = TenpayUtil.getCurrTime();
		String strTime = currTime.substring(8, currTime.length());
		String strRandom = TenpayUtil.buildRandom(4) + "";
		String nonce_str = strTime + strRandom;

		String body = request.getParameter("client");
		String out_trade_no = request.getParameter("contractId") + strTime;
		String order_price = request.getParameter("price") + "00";
		String spbill_create_ip = request.getRemoteAddr();
		String notify_url = GlobalConfig.URL + "api/weixin/result";
		String trade_type = "NATIVE";

		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", appid);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("out_trade_no", out_trade_no);
		packageParams.put("total_fee", order_price);
		packageParams.put("spbill_create_ip", spbill_create_ip);
		packageParams.put("notify_url", notify_url);
		packageParams.put("trade_type", trade_type);

		RequestHandler requestHandler = new RequestHandler(request, response);
		requestHandler.init(appid, appsecret, key);

		String sign = requestHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>" + mch_id + "</mch_id>" + "<nonce_str>"
				+ nonce_str + "</nonce_str>" + "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body + "]]></body>"
				+ "<out_trade_no>" + out_trade_no + "</out_trade_no>" + "<total_fee>" + order_price + "</total_fee>"
				+ "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" + "<notify_url>" + notify_url
				+ "</notify_url>" + "<trade_type>" + trade_type + "</trade_type>" + "</xml>";

		String code_url;
		WechatPayService wechatPayService = new WechatPayService();
		code_url = wechatPayService.getUrlCode(xml);
		System.out.println(wechatPayService.getResponseMessage());

		if (code_url.equals(""))
			System.err.println("err");

		return RestResponse.good(code_url);
	}

	@RequestMapping("/QRcode")
	@ResponseBody
	public void getQrCode(String code_url, HttpServletResponse response) throws Exception {
		ServletOutputStream sos = response.getOutputStream();
		QRCodeUtil.encode(code_url, sos);
	}

	/**
	 * 微信回调接口
	 */
	@RequestMapping(value = "/result")
	public void wechatOrderBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("ok!!!!!");
		// 创建支付应答对象
		ResponseHandler resHandler = new ResponseHandler(request, response);
		resHandler.setKey(GlobalConfig.KEY);
		// 判断签名是否正确
		if (resHandler.isTenpaySign()) {
			String resXml = "";
			if ("SUCCESS".equals(resHandler.getParameter("result_code"))) {
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			} else {
				System.out.println("支付失败,错误信息：" + resHandler.getParameter("err_code"));
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
						+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			}
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
			out.write(resXml.getBytes());
			out.flush();
			out.close();
		} else {
			System.out.println("通知签名验证失败");
		}
	}
}

