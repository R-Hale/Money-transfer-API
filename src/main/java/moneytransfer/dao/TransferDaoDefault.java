package moneytransfer.dao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import moneytransfer.model.Transfer;

public class TransferDaoDefault implements TransferDao{
	
	private final ConcurrentMap<Integer, Transfer> moneyTransfersMap;
	
    public TransferDaoDefault() {
        this.moneyTransfersMap = new ConcurrentHashMap<Integer, Transfer>();
    }
    
    public List<Transfer> getAllTransfers() {
    	return moneyTransfersMap.values()
                .stream()
                .sorted(Comparator.comparing((Transfer a) -> a.getId()))
                .collect(Collectors.toList());
    }
    
    public List<Transfer> getTransfersByAccountId(Integer accountId) {
    	Predicate<Transfer> predicate = t -> t.getFromAccountId() == (accountId) || t.getToAccountId() == (accountId);
    	return moneyTransfersMap.values()
                .stream()
                .filter(predicate)
                .sorted(Comparator.comparing((Transfer a) -> a.getId()))
                .collect(Collectors.toList());
    }
    
    public Optional<Transfer> getTransferById(Integer id) {
        return Optional.ofNullable(moneyTransfersMap.get(id));
    }

    public void save(Transfer transfer) {
    	moneyTransfersMap.put(transfer.getId(), transfer);
    }
}
