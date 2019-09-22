package moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.owlike.genson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class Account {
	
	private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final BigDecimal DEFAULT_BALANCE = BigDecimal.ZERO;
    
    @JsonIgnore
    private final Object accountLock = new Object();
    @JsonProperty(access = Access.READ_ONLY)
    private final int id;
    @JsonProperty(access = Access.READ_ONLY)
    private final String accountHolder;
    @JsonProperty(access = Access.READ_ONLY)
    private BigDecimal balance;

    public Account(String name) {
    	Objects.requireNonNull(name, "Name cannot be null");
        this.id = COUNTER.getAndIncrement();
        this.accountHolder = name;
        this.balance = DEFAULT_BALANCE;
    }

    public int getId() {
        return id;
    }
    
    public String getAccountHolder() {
        return accountHolder;
    }

    public BigDecimal getBalance() {
        return balance;
    }
    
    @JsonIgnore
    public Object getLock() {
    	return accountLock;
    }
    
    public synchronized void deposit(BigDecimal depositAmount) {
		this.balance = balance.add(depositAmount);
	}
    
    public synchronized boolean withdraw(BigDecimal withdrawAmount) {
    	if(balance.compareTo(withdrawAmount) < 0){
            return false;
        }
		this.balance = balance.subtract(withdrawAmount);
		return true;
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }

        Account account = (Account) o;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountHolder='" + accountHolder + '\'' +
                ", balance=" + balance +
                '}';
    }
}
