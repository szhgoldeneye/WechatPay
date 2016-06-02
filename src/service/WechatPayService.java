package service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

import constant.GlobalConfig;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jdom2.JDOMException;
import utils.XMLUtil;

import javax.net.ssl.SSLContext;

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

	public Map<String, Object> forRefund(SortedMap<String, String> packageParams) throws KeyStoreException, IOException,
			UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
		String sign = createSign(packageParams);
		String xml = "<xml>" + "<appid><![CDATA[" + packageParams.get("appid") + "]]></appid>" + "<mch_id><![CDATA["
				+ packageParams.get("mch_id") + "]]></mch_id>" + "<nonce_str><![CDATA[" + packageParams.get("nonce_str")
				+ "]]></nonce_str>" + "<out_trade_no><![CDATA[" + packageParams.get("out_trade_no")
				+ "]]></out_trade_no>" + "<out_refund_no><![CDATA[" + packageParams.get("out_refund_no")
				+ "]]></out_refund_no>" + "<total_fee><![CDATA[" + packageParams.get("total_fee") + "]]></total_fee>"
				+ "<refund_fee><![CDATA[" + packageParams.get("refund_fee") + "]]></refund_fee>"
				+ "<op_user_id><![CDATA[" + packageParams.get("mch_id") + "]]></op_user_id>" + "<sign>" + sign
				+ "</sign>" + "</xml>";
		Map doXMLtoMap = new HashMap();
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		String P12_PASSWORD = GlobalConfig.MCH_ID;
		FileInputStream inputStream = new FileInputStream(System.getProperty("user.dir") + "//src//apiclient_cert.p12");
		try {
			keyStore.load(inputStream, P12_PASSWORD.toCharArray());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} finally {
			inputStream.close();
		}
		SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, P12_PASSWORD.toCharArray()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		HttpPost httpPost = new HttpPost(refundUrl);

		try {
			httpPost.setEntity(new StringEntity(xml, "UTF-8"));
			CloseableHttpResponse response = httpClient.execute(httpPost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (jsonStr.indexOf("FAIL") >= 0) {
				return null;
			}
			doXMLtoMap = XMLUtil.doXMLParse(jsonStr);
			return doXMLtoMap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.close();
		}
		return null;
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
