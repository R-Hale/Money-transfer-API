package moneytransfer;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import moneytransfer.service.AccountService;
import moneytransfer.service.TransferService;

public class TestingUtils {
	
	private static Server server;
	protected static HttpClient client ;
	protected static PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	protected final static int port = 8080;
	protected final static String url = "http://localhost:";
	
	public static void startService() throws Exception {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(20);
		server = new Server(threadPool);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.addConnector(connector);
		
		server.setHandler(context);
		ServletHolder servlet = context.addServlet(ServletContainer.class, "/*");
		servlet.setInitParameter("jersey.config.server.provider.classnames",
				AccountService.class.getCanonicalName() + "," + TransferService.class.getCanonicalName());
		server.start();
		connManager.setDefaultMaxPerRoute(100);
        connManager.setMaxTotal(200);
		client= HttpClients.custom()
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .build();
	}

	public static void stopService() {
		HttpClientUtils.closeQuietly(client);
	}
	
	protected static int createAccount(String accountName) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPost httpPost = new HttpPost(url + port + "/accounts");

		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("accountHolder", accountName);
		Gson gson = new Gson();
		String accountJson = gson.toJson(jsonObj);
	 
		final StringEntity stringEntity = new StringEntity(accountJson);
		httpPost.setEntity(stringEntity);
		httpPost.setHeader("Content-type", "application/json");
		CloseableHttpResponse response = httpClient.execute(httpPost);
      
		final HttpEntity entity = response.getEntity();
		final String json = EntityUtils.toString(entity);
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		return jsonObject.get("id").getAsInt();
	}
	
	protected static BigDecimal getAccountBalance(int accountId) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse accountResponse = httpClient.execute(new HttpGet(url + port + "/accounts/" + accountId));
		final String accountJson = EntityUtils.toString(accountResponse.getEntity());
		JsonObject jsonObject = new JsonParser().parse(accountJson).getAsJsonObject();
		return jsonObject.get("balance").getAsBigDecimal();
	}
	
	protected static CloseableHttpResponse transfer(int fromAccountId, int toAccountId, BigDecimal transferAmount) throws ClientProtocolException, IOException {
		JsonObject transferJson = new JsonObject();
		transferJson.addProperty("fromAccountId", fromAccountId);
		transferJson.addProperty("toAccountId", toAccountId);
		transferJson.addProperty("transferAmount", transferAmount);
		Gson gson = new Gson();
		String transferJsonSerlialized = gson.toJson(transferJson);
		
		final HttpPost httpPost = new HttpPost(url + port + "/transfers");
		final StringEntity stringEntity = new StringEntity(transferJsonSerlialized);
		httpPost.setEntity(stringEntity);
		httpPost.setHeader("Content-type", "application/json");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		return httpClient.execute(httpPost);
	}
}
