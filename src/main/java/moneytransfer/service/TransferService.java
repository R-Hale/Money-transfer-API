package moneytransfer.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import moneytransfer.dao.DaoInstance;
import moneytransfer.model.Account;
import moneytransfer.model.Transfer;
import moneytransfer.service.utils.TransferDeserializer;

@Path("transfers")
public class TransferService {
	
	private final DaoInstance daoInstance = DaoInstance.getInstance();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    public List<Transfer> getAllTransfers() {
		return daoInstance.getTransferDao().getAllTransfers();
    }
	
	@GET
	@Path("/getTransfersByAccountId/{accountId}")
	@Produces(MediaType.APPLICATION_JSON)
    public List<Transfer> getTransfersByAccountId(@PathParam("accountId") int accountId) {
		return daoInstance.getTransferDao().getTransfersByAccountId(accountId);
    }
	
	@GET
	@Path("/{transferId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransfer(@PathParam("transferId") int transferId) {
        Optional<Transfer> transfer = daoInstance.getTransferDao().getTransferById(transferId);
		if(!transfer.isPresent()){
            throw new WebApplicationException(ErrorTexts.TRANSFER_DOES_NOT_EXIST, Response.Status.NOT_FOUND);
        }
		return Response.ok()
	  	          .entity(transfer.get())
	  	          .type(MediaType.APPLICATION_JSON)
	  	          .build();
    }
	
	private Transfer createTransfer(InputStream is) {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("TransferDeserializer", new Version(1, 0, 0, null, null, null));
		module.addDeserializer(Transfer.class, new TransferDeserializer());
		mapper.registerModule(module);
		
		try {
			return mapper.readValue(is, Transfer.class);
		} catch (IOException | NullPointerException e) {
			//Unimplemented error logging
			throw new WebApplicationException(ErrorTexts.TRANSFER_CREATION_FAILED, Response.Status.BAD_REQUEST);
		}
	}
	
	private Account fromAccountValidation(Transfer transfer) {
		Optional<Account> fromAccountOpt = daoInstance.getAccountDao().getAccountById(transfer.getFromAccountId());
		if(!fromAccountOpt.isPresent()){
			throw new WebApplicationException(ErrorTexts.SOURCE_ACCOUNT_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		}
		return fromAccountOpt.get();
	}
	
	private Account toAccountValidation(Transfer transfer) {
		Optional<Account> toAccountOpt = daoInstance.getAccountDao().getAccountById(transfer.getToAccountId());
		if(!toAccountOpt.isPresent()){
			throw new WebApplicationException(ErrorTexts.DESTINATION_ACCOUNT_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		}
		return toAccountOpt.get();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response transferMoney(InputStream is) {
		
		Transfer transfer = createTransfer(is);
		if(transfer.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new WebApplicationException(ErrorTexts.TRANSFER_ZERO_OR_BELOW, Response.Status.BAD_REQUEST);
		}
		
		Account fromAccount = fromAccountValidation(transfer);
 		Account toAccount = toAccountValidation(transfer);
		
		BigDecimal transferAmount = transfer.getTransferAmount();
		Object lock1 = fromAccount.getId() < toAccount.getId() ? fromAccount.getLock() : toAccount.getId();
	    Object lock2 = fromAccount.getId() < toAccount.getId() ? toAccount.getLock() : fromAccount.getLock();
	    synchronized (lock1) {
	       synchronized (lock2) {
	          if(fromAccount.withdraw(transferAmount)){
	        	  toAccount.deposit(transferAmount);
	          }
	          else {
	        	  throw new WebApplicationException(ErrorTexts.TRANSFER_FAILED, Response.Status.BAD_REQUEST);
	          }
	       }
	    }
		daoInstance.getTransferDao().save(transfer);

		return Response
			      .status(Response.Status.OK)
			      .type(MediaType.APPLICATION_JSON)
			      .build();
    }
}
