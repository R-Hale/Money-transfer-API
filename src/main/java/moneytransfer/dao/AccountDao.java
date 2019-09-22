package moneytransfer.dao;

import java.util.List;
import java.util.Optional;

import moneytransfer.model.Account;

public interface AccountDao {
	
	List<Account> getAllAccounts();
    
    Optional<Account> getAccountById(Integer accountId);

    void save(Account account);
    
    boolean delete(int accountId);
}
