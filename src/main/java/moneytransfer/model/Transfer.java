package moneytransfer.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class Transfer {
	
	private static final AtomicInteger COUNTER = new AtomicInteger();
    
    @JsonIgnore
    @JsonProperty(access = Access.READ_ONLY)
    private final int id;
    private final int fromAccountId;
    private final int toAccountId;
    private final BigDecimal transferAmount;

    public Transfer(Integer fromAccountId, Integer toAccountId, BigDecimal transferAmount) {
        Objects.requireNonNull(fromAccountId, "Source account cannot be null");
        Objects.requireNonNull(toAccountId, "Destination account cannot be null");
        Objects.requireNonNull(transferAmount, "Transfer amount cannot be null");
        
        this.id = COUNTER.getAndIncrement();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transferAmount = transferAmount;
    }

    public int getId() {
        return id;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }
    
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }

        Transfer transfer = (Transfer) o;
        return id == transfer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", fromAccountId='" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", transferAmount=" + transferAmount +
                '}';
    }
}
