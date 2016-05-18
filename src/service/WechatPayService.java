package service;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jdom2.JDOMException;

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

	public String getUrlCode(String xml) throws ClientProtocolException, IOException, JDOMException {
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
}
