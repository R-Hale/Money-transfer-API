package moneytransfer.dao;

public class DaoInstance {
	
	private final AccountDaoDefault accountDAO = new AccountDaoDefault();
	private final TransferDaoDefault transferDAO = new TransferDaoDefault();
	
	private static final DaoInstance INSTANCE = new DaoInstance();

    private DaoInstance() {}
	
	public AccountDaoDefault getAccountDao() {
		return accountDAO;
	}

	public TransferDaoDefault getTransferDao() {
		return transferDAO;
	}
	
	public static DaoInstance getInstance() {
        return INSTANCE;
    }
}
