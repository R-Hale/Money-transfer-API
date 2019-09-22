package moneytransfer.dao;

import java.util.List;
import java.util.Optional;

import moneytransfer.model.Transfer;

public interface TransferDao {
	List<Transfer> getAllTransfers();
    
    List<Transfer> getTransfersByAccountId(Integer accountId);
    
    public Optional<Transfer> getTransferById(Integer id);

    public void save(Transfer transfer);

}
