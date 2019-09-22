package moneytransfer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import moneytransfer.dao.AccountDaoDefault;
import moneytransfer.dao.DaoInstance;
import moneytransfer.model.Account;
import moneytransfer.service.AccountService;
import moneytransfer.service.TransferService;


public class App
{
	private final static int port = 8080;
	
	public static void main(String[] args) throws Exception {
		initialiseDao();
		startService();
	}
	
	private static void initialiseDao() {
		AccountDaoDefault accountInstance = DaoInstance.getInstance().getAccountDao();
		Account testAccount1 = new Account("John Smith");
		Account testAccount2 = new Account("Jane Smith");
		Account testAccount3 = new Account("Andrew Smith");
		Account testAccount4 = new Account("Anna Smith");
		
		accountInstance.save(testAccount1);
		accountInstance.save(testAccount2);
		accountInstance.save(testAccount3);
		accountInstance.save(testAccount4);
	}
	private static void startService() throws Exception {
		
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(20);
		Server server = new Server(threadPool);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.addConnector(connector);
		
		server.setHandler(context);
		ServletHolder servlet = context.addServlet(ServletContainer.class, "/*");
		servlet.setInitParameter("jersey.config.server.provider.classnames",
				AccountService.class.getCanonicalName() + "," + TransferService.class.getCanonicalName());
		try {
			server.start();
			server.join();
		} finally {
			server.destroy();
		}
	}
}
