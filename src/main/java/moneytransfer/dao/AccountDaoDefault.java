package moneytransfer.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import moneytransfer.model.Account;

public class AccountDaoDefault implements AccountDao {
	
	private final ConcurrentMap<Integer, Account> accountMap;
	
    public AccountDaoDefault() {
        this.accountMap = new ConcurrentHashMap<Integer, Account>();
    }
    
    public List<Account> getAllAccounts() {
    	return accountMap.values()
                .stream()
                .sorted(Comparator.comparing((Account a) -> a.getId()))
                .collect(Collectors.toList());
    }
    
    public Optional<Account> getAccountById(Integer accountId) {
        return Optional.ofNullable(accountMap.get(accountId));
    }

    public void save(Account account) {
    	accountMap.put(account.getId(), account);
    }
    
    public boolean delete(int accountId) {
        return null != accountMap.remove(accountId);
    }
}