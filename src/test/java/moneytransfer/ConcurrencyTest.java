package moneytransfer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
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
	}
	
	private CloseableHttpResponse withdraw(int withdrawAccountId, BigDecimal withdrawAmount) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPut = new HttpPut(url + port + "/accounts/" + withdrawAccountId + "/withdraw/" + withdrawAmount);
	    return httpClient.execute(httpPut);
	}
	
	@Test
	public void testSynchronousWithdraw() throws ClientProtocolException, IOException, InterruptedException, ExecutionException {
		AccountDaoDefault accountInstance = DaoInstance.getInstance().getAccountDao();
		
		String withdrawAccountName = "Jane Johnson";
		
		Account testAccount = new Account(withdrawAccountName);
		int testAccountId = testAccount.getId();
		accountInstance.save(testAccount);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPutInit = new HttpPut(url + port + "/accounts/" + testAccountId + "/deposit/" + 100);
		httpClient.execute(httpPutInit);
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean running = new AtomicBoolean();
		AtomicInteger overlapingThreadsCount = new AtomicInteger();
		int threads = 10;
	    ExecutorService service = Executors.newFixedThreadPool(threads);
	    
	   Collection<Future<CloseableHttpResponse>> futures = new ArrayList<>(threads);
		for (int t = 0; t < threads; ++t) {
			futures.add(
		    service.submit(
		      () -> {
		    	  CloseableHttpResponse response = null;
		    	  try {
						latch.await();
				        if (running.get()) {
				        	overlapingThreadsCount.incrementAndGet();
				        }
				        running.set(true);
				        response = withdraw(testAccountId, BigDecimal.TEN.multiply(BigDecimal.TEN));
				        running.set(false);
				        
		    	  }
		    	  catch(InterruptedException | IOException e) {
		    		  e.printStackTrace();
		    	  }
		    	  
		        return response;
		      }
		    )
		  );
		}
		latch.countDown();
		Set<CloseableHttpResponse> ids = new HashSet<>();
		for (Future<CloseableHttpResponse> f : futures) {
		  ids.add(f.get());
		}
		assertTrue(overlapingThreadsCount.get() > 0);

		BigDecimal testAccountBalance = TestingUtils.getAccountBalance(testAccountId);
		assertTrue(testAccountBalance.compareTo(new BigDecimal("0")) == 0);
	}
	
	@Test
	public void testSynchronousTransfers() throws ClientProtocolException, IOException, InterruptedException, ExecutionException {
		AccountDaoDefault accountInstance = DaoInstance.getInstance().getAccountDao();
		
		String senderAccountName = "John Johnson";
		String receiverAccountName = "Andrew Johnson";
		
		Account testAccountSender = new Account(senderAccountName);
		int senderAccountId = testAccountSender.getId();
		accountInstance.save(testAccountSender);
		
		Account testAccountReceiver = new Account(receiverAccountName);
		int receiverAccountId = testAccountReceiver.getId();
		accountInstance.save(testAccountReceiver);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		final HttpPut httpPutInit = new HttpPut(url + port + "/accounts/" + senderAccountId + "/deposit/" + 100);
		httpClient.execute(httpPutInit);
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean running = new AtomicBoolean();
		AtomicInteger overlapingThreadsCount = new AtomicInteger();
		
		int threads = 10;
	    ExecutorService service = Executors.newFixedThreadPool(threads);
	    
	   Collection<Future<CloseableHttpResponse>> futures = new ArrayList<>(threads);
		for (int t = 0; t < threads; ++t) {
			futures.add(
		    service.submit(
		      () -> {
		    	  CloseableHttpResponse response = null;
		    	  try {
						latch.await();
				        if (running.get()) {
				        	overlapingThreadsCount.incrementAndGet();
				        }
				        running.set(true);
				        response = TestingUtils.transfer(senderAccountId, receiverAccountId, BigDecimal.TEN.multiply(BigDecimal.TEN));
				        running.set(false);
				        
		    	  }
		    	  catch(InterruptedException | IOException e) {
		    		  e.printStackTrace();
		    	  }
		    	  
		        return response;
		      }
		    )
		  );
		}
		latch.countDown();
		Set<CloseableHttpResponse> ids = new HashSet<>();
		for (Future<CloseableHttpResponse> f : futures) {
		  ids.add(f.get());
		}
		assertTrue(overlapingThreadsCount.get() > 0);

		BigDecimal fromAccountBalance = TestingUtils.getAccountBalance(senderAccountId);
		BigDecimal toAccountBalance = TestingUtils.getAccountBalance(receiverAccountId);
		assertTrue(fromAccountBalance.compareTo(new BigDecimal("0")) == 0);
		assertTrue(toAccountBalance.compareTo(new BigDecimal("100")) == 0);
	}
	
	@AfterAll
	private static void stopService() throws Exception {
		TestingUtils.stopService();
	}

}
