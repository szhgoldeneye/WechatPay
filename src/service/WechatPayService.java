package service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jdom2.JDOMException;

import com.cloume.wechat.constant.GlobalConfig;
import com.cloume.wechat.utils.MD5Util;
import com.cloume.wechat.utils.XMLUtil;

public class WechatPayService {

	public static String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	private HttpClientBuilder httpClientBuilder;
	@SuppressWarnings("unused")
	private CloseableHttpClient httpClient;
	private String responseMessage;

	public WechatPayService() {
		httpClientBuilder = HttpClientBuilder.create();
		httpClient = httpClientBuilder.build();
	}

	public String getUrlCode(SortedMap<String, String> packageParams)
			throws ClientProtocolException, IOException, JDOMException {
		String sign = createSign(packageParams);
		String xml = "<xml>" + "<appid>" + packageParams.get("appid") + "</appid>" + "<mch_id>"
				+ packageParams.get("mch_id") + "</mch_id>" + "<nonce_str>" + packageParams.get("nonce_str")
				+ "</nonce_str>" + "<sign>" + sign + "</sign>" + "<body><![CDATA[" + packageParams.get("body")
				+ "]]></body>" + "<out_trade_no>" + packageParams.get("out_trade_no") + "</out_trade_no>"
				+ "<total_fee>" + packageParams.get("total_fee") + "</total_fee>" + "<spbill_create_ip>"
				+ packageParams.get("spbill_create_ip") + "</spbill_create_ip>" + "<notify_url>"
				+ packageParams.get("notify_url") + "</notify_url>" + "<trade_type>" + packageParams.get("trade_type")
				+ "</trade_type>" + "</xml>";

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(xml, "UTF-8"));
		CloseableHttpResponse response = httpClient.execute(httpPost);
		responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
		Map<?, ?> map = XMLUtil.doXMLParse(responseMessage);
		String codeUrl = map.get("code_url").toString();
		response.close();
		httpClient.close();
		return codeUrl;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + GlobalConfig.KEY);
		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
		return sign;
	}
}
