package moneytransfer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import moneytransfer.dao.AccountDaoDefault;
import moneytransfer.dao.DaoInstance;
import moneytransfer.model.Account;

@Execution(ExecutionMode.CONCURRENT)
public class ConcurrencyTest {
	
	protected static HttpClient client ;
	protected static PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	private static int TEST_WITHDRAW_ACCOUNT_ID;
	private static int TEST_ACCOUNT_ID_SENDER;
	private static int TEST_ACCOUNT_ID_RECEIVER;
	private static String TEST_ACCOUNT_WITHDRAW_NAME = "Andrew Johnson";
	private static String TEST_ACCOUNT_SENDER_NAME = "John Johnson";
	private static String TEST_ACCOUNT_RECEIVER_NAME = "Jane Johnson";
	private static String url;
	private static int port;
	
	@BeforeAll
	private static void startService() throws Exception {
		url = TestingUtils.url;
		port = TestingUtils.port;
		
		try {
			TestingUtils.startService();
		}
		catch(Exception e) {
			System.out.println("Bind address already in use");
		}

		initialiseDao();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPutInit = new HttpPut(url + port + "/accounts/" + TEST_WITHDRAW_ACCOUNT_ID + "/deposit/" + 100);
		httpClient.execute(httpPutInit);
		final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + TEST_ACCOUNT_ID_SENDER + "/deposit/" + 100);
		httpClient.execute(httpPut);
	}
	
	private static void initialiseDao() {
		AccountDaoDefault accountInstance = DaoInstance.getInstance().getAccountDao();
		
		Account testAccountWithdraw = new Account(TEST_ACCOUNT_WITHDRAW_NAME);
		TEST_WITHDRAW_ACCOUNT_ID = testAccountWithdraw.getId();
		accountInstance.save(testAccountWithdraw);
		
		Account testAccountSender = new Account(TEST_ACCOUNT_SENDER_NAME);
		TEST_ACCOUNT_ID_SENDER = testAccountSender.getId();
		accountInstance.save(testAccountSender);
		
		Account testAccountReceiver = new Account(TEST_ACCOUNT_RECEIVER_NAME);
		TEST_ACCOUNT_ID_RECEIVER = testAccountReceiver.getId();
		accountInstance.save(testAccountReceiver);
	}
	
	@Test
	public void withdraw1() throws IOException {
		withdraw(TEST_WITHDRAW_ACCOUNT_ID, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void withdraw2() throws IOException {
		withdraw(TEST_WITHDRAW_ACCOUNT_ID, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void withdraw3() throws IOException {
		withdraw(TEST_WITHDRAW_ACCOUNT_ID, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void withdraw4() throws IOException {
		withdraw(TEST_WITHDRAW_ACCOUNT_ID, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void withdraw5() throws IOException {
		withdraw(TEST_WITHDRAW_ACCOUNT_ID, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	
	@Test
	public void transfer1() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer2() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer3() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer4() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer5() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer6() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer7() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer8() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer9() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	@Test
	public void transfer10() throws IOException {
		transfer(TEST_ACCOUNT_ID_SENDER, TEST_ACCOUNT_ID_RECEIVER, BigDecimal.TEN.multiply(BigDecimal.TEN));
	}
	
	private void withdraw(int withdrawAccountId, BigDecimal withdrawAmount) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + withdrawAccountId + "/withdraw/" + withdrawAmount);
	    httpClient.execute(httpPut);
	}
	
	private void transfer(int fromAccountId, int tooAccountId, BigDecimal transferAmount) throws ClientProtocolException, IOException {
		TestingUtils.transfer(fromAccountId, tooAccountId, transferAmount);
	}
	
	@AfterAll
	private static void stopService() throws Exception {
		BigDecimal withdrawAccountBalance = TestingUtils.getAccountBalance(TEST_WITHDRAW_ACCOUNT_ID);
		assertTrue(withdrawAccountBalance.compareTo(new BigDecimal("0")) == 0);
		
		BigDecimal fromAccountBalance = TestingUtils.getAccountBalance(TEST_ACCOUNT_ID_SENDER);
		BigDecimal toAccountBalance = TestingUtils.getAccountBalance(TEST_ACCOUNT_ID_RECEIVER);
		assertTrue(fromAccountBalance.compareTo(new BigDecimal("0")) == 0);
		assertTrue(toAccountBalance.compareTo(new BigDecimal("100")) == 0);
		
		TestingUtils.stopService();
	}

}
