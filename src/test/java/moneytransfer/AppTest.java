package moneytransfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import moneytransfer.dao.AccountDaoDefault;
import moneytransfer.dao.DaoInstance;
import moneytransfer.model.Account;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
public class AppTest {
	
	private static String url;
	private static int port;
	private static int TEST_ACCOUNT_ID;
	private static String TEST_ACCOUNT_NAME;
	
	@BeforeAll
	private static void startService() throws Exception {
		url = TestingUtils.url;
		port = TestingUtils.port;
		TEST_ACCOUNT_NAME = "John Smith";
		
		initialiseDao();
		try {
			TestingUtils.startService();
		}
		catch(Exception e) {
			System.out.println("Bind address already in use");
		}
	}
	
	protected static void initialiseDao() {
		AccountDaoDefault accountInstance = DaoInstance.getInstance().getAccountDao();
		Account testAccount = new Account(TEST_ACCOUNT_NAME);
		TEST_ACCOUNT_ID = testAccount.getId();
		accountInstance.save(testAccount);
	}
	
	@AfterAll
	private static void stopService() throws Exception {
		TestingUtils.stopService();
	}
	
	@Test
    @Order(1)
    public void getAccountById() throws IOException
    {
		CloseableHttpClient httpClient = HttpClients.createDefault();
    	final HttpGet httpGet = new HttpGet(url + port + "/accounts/" + TEST_ACCOUNT_ID);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        
        final HttpEntity entity = response.getEntity();
        assertNotNull(entity);
        
        final String json = EntityUtils.toString(entity);
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        assertTrue(jsonObject.get("id").getAsInt() == 0);
        assertTrue(jsonObject.get("accountHolder").getAsString().equals(TEST_ACCOUNT_NAME));
    }
	
	@Test
    @Order(2)
    public void getAccountByInvalidId() throws IOException
    {
		CloseableHttpClient httpClient = HttpClients.createDefault();
    	final HttpGet httpGet = new HttpGet(url + port + "/accounts/" + Integer.MAX_VALUE);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    }
    
	@Test
    @Order(3)
    public void getAccountNameById() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       final HttpGet httpGet = new HttpGet(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/username");
       CloseableHttpResponse response = httpClient.execute(httpGet);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       final HttpEntity entity = response.getEntity();
       
       final String json = EntityUtils.toString(entity);
       JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
       assertTrue(jsonObject.get("accountHolder").getAsString().equals(TEST_ACCOUNT_NAME));
    }
    
	@Test
    @Order(4)
    public void getAccountBalanceById() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       final HttpGet httpGet = new HttpGet(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/balance");
       CloseableHttpResponse response = httpClient.execute(httpGet);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       final HttpEntity entity = response.getEntity();
       
       final String json = EntityUtils.toString(entity);
       JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
       assertTrue(jsonObject.get("balance").getAsBigDecimal().equals(BigDecimal.ZERO));
    }
    
	@Test
    @Order(5)
    public void createAccount() throws IOException
    {
	   CloseableHttpClient httpClient = HttpClients.createDefault();
	   final HttpPost httpPost = new HttpPost(url + port + "/accounts");

	   JsonObject accountJson = new JsonObject();
	   accountJson.addProperty("accountHolder", "Jane Smith");
	   Gson gson = new Gson();
	   String accountJsonDeserlialized = gson.toJson(accountJson);
	   
	   final StringEntity stringEntity = new StringEntity(accountJsonDeserlialized);
	   httpPost.setEntity(stringEntity);
	   httpPost.setHeader("Content-type", "application/json");
	   CloseableHttpResponse response = httpClient.execute(httpPost);
	   assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    }
	
	@Test
    @Order(6)
    public void createInvalidAccount() throws IOException
    {
	   CloseableHttpClient httpClient = HttpClients.createDefault();
	   final HttpPost httpPost = new HttpPost(url + port + "/accounts");

	   JsonObject accountJson = new JsonObject();
	   accountJson.addProperty("accountHolder", 12345);
	   Gson gson = new Gson();
	   String accountJsonDeserlialized = gson.toJson(accountJson);
	
	   final StringEntity stringEntity = new StringEntity(accountJsonDeserlialized);
	   httpPost.setEntity(stringEntity);
	   httpPost.setHeader("Content-type", "application/json");
	   CloseableHttpResponse response = httpClient.execute(httpPost);
	   assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
    }
	
	@Test
    @Order(7)
    public void createAccountWithNulls() throws IOException
    {
	   CloseableHttpClient httpClient = HttpClients.createDefault();
	   final HttpPost httpPost = new HttpPost(url + port + "/accounts");
	   String json = "{\"accountHolder\":}";
	   Gson gson = new Gson();
	   String accountJson = gson.toJson(json);
	
	   final StringEntity stringEntity = new StringEntity(accountJson);
	   httpPost.setEntity(stringEntity);
	   httpPost.setHeader("Content-type", "application/json");
	   CloseableHttpResponse response = httpClient.execute(httpPost);
	   assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
    }

    
	@Test
    @Order(8)
    public void accountDeposit() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int depositAmount = 100;
       final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/deposit/" + depositAmount);
       CloseableHttpResponse response = httpClient.execute(httpPut);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       final HttpEntity entity = response.getEntity();
       
       final String json = EntityUtils.toString(entity);
       JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
       assertTrue(jsonObject.get("id").getAsInt() == 0);
       assertTrue(jsonObject.get("balance").getAsInt() == depositAmount);
    }
	
	@Test
    @Order(9)
    public void accountWithdraw() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int withdrawAmount = 100;
       final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/withdraw/" + withdrawAmount);
       CloseableHttpResponse response = httpClient.execute(httpPut);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       final HttpEntity entity = response.getEntity();
       
       final String json = EntityUtils.toString(entity);
       JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
       assertTrue(jsonObject.get("id").getAsInt() == 0);
       assertTrue(jsonObject.get("balance").getAsInt() == 0);
    }
	
	@Test
    @Order(10)
    public void accountDepositOneEdgeCase() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int depositAmount = 1;
       final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/deposit/" + depositAmount);
       CloseableHttpResponse response = httpClient.execute(httpPut);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }
	
	@Test
    @Order(11)
    public void accountDepositZeroEdgeCase() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int depositAmount = 0;
       final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + TEST_ACCOUNT_ID + "/deposit/" + depositAmount);
       CloseableHttpResponse response = httpClient.execute(httpPut);
       assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
    }
	
	@Test
    @Order(12)
    public void accountWithdrawEmptyAccount() throws IOException
    {
	   int accountId = TestingUtils.createAccount("Dummy One");
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int withdrawAmount = 100;
       final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + accountId + "/withdraw/" + withdrawAmount);
       CloseableHttpResponse response = httpClient.execute(httpPut);
       assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
    }
	
	@Test
    @Order(13)
    public void accountDelete() throws IOException
    {
       CloseableHttpClient httpClient = HttpClients.createDefault();
       int accountIdToDelete = TestingUtils.createAccount("Dummy");
       final HttpGet httpGetAccountExists = new HttpGet(url + port + "/accounts/" + accountIdToDelete);
       CloseableHttpResponse responseExists = httpClient.execute(httpGetAccountExists);
       assertEquals(HttpStatus.SC_OK, responseExists.getStatusLine().getStatusCode());
       
       final HttpDelete httpDelete = new HttpDelete(url + port + "/accounts/" + accountIdToDelete);
       CloseableHttpResponse response = httpClient.execute(httpDelete);
       assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       
       final HttpGet httpGetFailure = new HttpGet(url + port + "/accounts/" + accountIdToDelete);
       CloseableHttpResponse responseFailure = httpClient.execute(httpGetFailure);
       assertEquals(HttpStatus.SC_NOT_FOUND, responseFailure.getStatusLine().getStatusCode());
    }

    
	@Test
    @Order(14)
    public void performTransfer() throws IOException
    {
		BigDecimal transferAmount = (BigDecimal.TEN);
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountId = TestingUtils.createAccount("Dummy Two");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		httpClient.execute(new HttpPut(url + port + "/accounts/" + fromAccountId + "/deposit/" + transferAmount));
		BigDecimal fromAccountBalanceBefore = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceBefore = TestingUtils.getAccountBalance(toAccountId);
		
		CloseableHttpResponse response = TestingUtils.transfer(fromAccountId, toAccountId, transferAmount);
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
       
		BigDecimal fromAccountBalanceAfter = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceAfter = TestingUtils.getAccountBalance(toAccountId);
		
		assertTrue(fromAccountBalanceAfter.compareTo(fromAccountBalanceBefore.subtract(transferAmount)) == 0);
		assertTrue(toAccountBalanceAfter.compareTo(toAccountBalanceBefore.add(transferAmount)) == 0);
    }
	
	@Test
    @Order(15)
    public void insufficientFundsTransfer() throws IOException
    {
		BigDecimal transferAmount = (BigDecimal.TEN);
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountId = TestingUtils.createAccount("Dummy Two");
		
		BigDecimal fromAccountBalanceBefore = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceBefore = TestingUtils.getAccountBalance(toAccountId);
		
		CloseableHttpResponse response = TestingUtils.transfer(fromAccountId, toAccountId, transferAmount);
		assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
       
		BigDecimal fromAccountBalanceAfter = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceAfter = TestingUtils.getAccountBalance(toAccountId);
		
		assertTrue(fromAccountBalanceAfter.compareTo(fromAccountBalanceBefore) == 0);
		assertTrue(toAccountBalanceAfter.compareTo(toAccountBalanceBefore) == 0);
    }
	
	@Test
    @Order(16)
    public void negativeAmountTransfer() throws IOException
    {
		BigDecimal transferAmount = (BigDecimal.ONE.subtract(BigDecimal.TEN));
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountId = TestingUtils.createAccount("Dummy Two");
		
		BigDecimal fromAccountBalanceBefore = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceBefore = TestingUtils.getAccountBalance(toAccountId);
		
		CloseableHttpResponse response = TestingUtils.transfer(fromAccountId, toAccountId, transferAmount);
		assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
       
		BigDecimal fromAccountBalanceAfter = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceAfter = TestingUtils.getAccountBalance(toAccountId);
		
		assertTrue(fromAccountBalanceAfter.compareTo(fromAccountBalanceBefore) == 0);
		assertTrue(toAccountBalanceAfter.compareTo(toAccountBalanceBefore) == 0);
    }
	
	@Test
    @Order(17)
    public void zeroAmountTransfer() throws IOException
    {
		BigDecimal transferAmount = (BigDecimal.ONE.subtract(BigDecimal.ZERO));
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountId = TestingUtils.createAccount("Dummy Two");
		
		BigDecimal fromAccountBalanceBefore = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceBefore = TestingUtils.getAccountBalance(toAccountId);
		
		CloseableHttpResponse response = TestingUtils.transfer(fromAccountId, toAccountId, transferAmount);
		assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
       
		BigDecimal fromAccountBalanceAfter = TestingUtils.getAccountBalance(fromAccountId);
		BigDecimal toAccountBalanceAfter = TestingUtils.getAccountBalance(toAccountId);
		assertTrue(fromAccountBalanceAfter.compareTo(fromAccountBalanceBefore) == 0);
		assertTrue(toAccountBalanceAfter.compareTo(toAccountBalanceBefore) == 0);
    }
	
	@Test
    @Order(18)
    public void getAllTransfers() throws IOException {
    	CloseableHttpClient httpClient = HttpClients.createDefault();
    	final HttpGet httpGet = new HttpGet(url + port + "/transfers");
    	httpGet.setHeader("Accept", "application/json");
    	CloseableHttpResponse response = httpClient.execute(httpGet);
    	assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    	final HttpEntity entity = response.getEntity();
    	
        String jsonString = EntityUtils.toString(entity);
        JsonArray jsonArray = (JsonArray) new JsonParser().parse(jsonString);
        assertTrue(jsonArray.size() == 1);
    }
	
	@Test
    @Order(19)
    public void getTransfersByAccountId() throws IOException {
		BigDecimal depositAmount = BigDecimal.TEN.multiply(BigDecimal.TEN);
		BigDecimal transferAmount = BigDecimal.TEN;
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountXId = TestingUtils.createAccount("Dummy Two");
		int toAccountYId = TestingUtils.createAccount("Dummy Three");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + fromAccountId + "/deposit/" + depositAmount);
		httpClient.execute(httpPut);
		
		TestingUtils.transfer(fromAccountId, toAccountXId, transferAmount);
		TestingUtils.transfer(fromAccountId, toAccountYId, transferAmount);
		
    	CloseableHttpResponse response = httpClient.execute(new HttpGet(url + port + "/transfers/getTransfersByAccountId/" + fromAccountId));
    	String jsonString = EntityUtils.toString(response.getEntity());
        JsonArray jsonArray = (JsonArray) new JsonParser().parse(jsonString);
        assertTrue(jsonArray.size() == 2);
        
        response = httpClient.execute(new HttpGet(url + port + "/transfers/getTransfersByAccountId/" + toAccountXId));
    	jsonString = EntityUtils.toString(response.getEntity());
        jsonArray = (JsonArray) new JsonParser().parse(jsonString);
        assertTrue(jsonArray.size() == 1);
		
        response = httpClient.execute(new HttpGet(url + port + "/transfers/getTransfersByAccountId/" + toAccountYId));
    	jsonString = EntityUtils.toString(response.getEntity());
        jsonArray = (JsonArray) new JsonParser().parse(jsonString);
        assertTrue(jsonArray.size() == 1);
    }
	
	@Test
    @Order(20)
    public void getTransfersToDeletedAccount() throws IOException {
		BigDecimal depositAmount = BigDecimal.TEN.multiply(BigDecimal.TEN);
		BigDecimal transferAmount = BigDecimal.TEN;
		int fromAccountId = TestingUtils.createAccount("Dummy One");
		int toAccountId = TestingUtils.createAccount("Dummy Two");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + fromAccountId + "/deposit/" + depositAmount);
		httpClient.execute(httpPut);
		
	    final HttpDelete httpDelete = new HttpDelete(url + port + "/accounts/" + toAccountId);
	    httpClient.execute(httpDelete);
	    
	    CloseableHttpResponse response = TestingUtils.transfer(fromAccountId, toAccountId, transferAmount);
	    assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
    }
}
