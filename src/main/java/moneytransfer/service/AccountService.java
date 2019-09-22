package moneytransfer.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import moneytransfer.dao.DaoInstance;
import moneytransfer.model.Account;
import moneytransfer.service.utils.AccountDeserializer;

@Path("accounts")
public class AccountService {
	
		private final DaoInstance daoInstance = DaoInstance.getInstance();
		
		@GET
		@Produces(MediaType.APPLICATION_JSON)
	    public List<Account> getAllAccounts() {
			return daoInstance.getAccountDao().getAllAccounts();
	    }
		
		@GET
		@Path("/{accountId}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getAccount(@PathParam("accountId") int accountId) {    
	        Optional<Account> account = daoInstance.getAccountDao().getAccountById(accountId);
			if(!account.isPresent()){
	            String erroMsg = String.format(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, accountId);
	            throw new WebApplicationException(erroMsg, Response.Status.NOT_FOUND);
	        }
			return Response.ok()
		  	          .entity(account.get())
		  	          .type(MediaType.APPLICATION_JSON)
		  	          .build();
	    }
		
		@GET
		@Path("/{accountId}/username")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getAccountUsername(@PathParam("accountId") int accountId) {  
			Optional<Account> account = daoInstance.getAccountDao().getAccountById(accountId);
			if(!account.isPresent()){
				String erroMsg = String.format(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, accountId);
	            throw new WebApplicationException(erroMsg, Response.Status.NOT_FOUND);
	        }
			JsonObject usernameJson = new JsonObject();
			usernameJson.addProperty("accountHolder", account.get().getAccountHolder());
			Gson gson = new Gson();
			String message = gson.toJson(usernameJson);
			return Response.ok()
		  	          .entity(message)
		  	          .type(MediaType.APPLICATION_JSON)
		  	          .build();
	    }
		
		@GET
		@Path("/{accountId}/balance")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getAccountBalance(@PathParam("accountId") int accountId) {
			Optional<Account> account = daoInstance.getAccountDao().getAccountById(accountId);
			if(!account.isPresent()){
				String erroMsg = String.format(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, accountId);
	            throw new WebApplicationException(erroMsg, Response.Status.NOT_FOUND);
	        }
			
			JsonObject usernameJson = new JsonObject();
			usernameJson.addProperty("balance", account.get().getBalance());
			Gson gson = new Gson();
			String message = gson.toJson(usernameJson);
			return Response
				      .status(Response.Status.OK)
				      .entity(message)
				      .type(MediaType.APPLICATION_JSON)
				      .build();
	    }
		
		@POST
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response create(InputStream is) {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule("AccountDeserializer", new Version(1, 0, 0, null, null, null));
			module.addDeserializer(Account.class, new AccountDeserializer());
			mapper.registerModule(module);
			Account account;
			try {
				account = mapper.readValue(is, Account.class);
				daoInstance.getAccountDao().save(account);
				
				return Response
					      .status(Response.Status.CREATED)
					      .entity(account)
					      .type(MediaType.APPLICATION_JSON)
					      .build();
			} catch (IOException | NullPointerException e) {
				//Unimplemented error logging
				throw new WebApplicationException(ErrorTexts.ACCOUNT_CREATION_FAILED, Response.Status.BAD_REQUEST);
			}
	    }
		
		
		@PUT
		@Path("/{accountId}/deposit/{depositAmount}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response deposit(@PathParam("accountId") int accountId, @PathParam("depositAmount") BigDecimal depositAmount) {
			if(depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new WebApplicationException(ErrorTexts.CANNOT_DEPOSIT_BELOW_ZERO, Response.Status.BAD_REQUEST);
			}
			Optional<Account> accountOpt = daoInstance.getAccountDao().getAccountById(accountId);
			if(!accountOpt.isPresent()){
		        throw new WebApplicationException(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		    }
			
			Account account = accountOpt.get();
			account.deposit(depositAmount);
			return Response
				      .status(Response.Status.OK)
				      .entity(daoInstance.getAccountDao().getAccountById(accountId).get())
				      .type(MediaType.APPLICATION_JSON)
				      .build();
	    }
		
		@PUT
		@Path("/{accountId}/withdraw/{withdrawAmount}")
		@Produces(MediaType.APPLICATION_JSON)
		public Response withdraw(@PathParam("accountId") int accountId, @PathParam("withdrawAmount") BigDecimal withdrawAmount) {
			if(withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new WebApplicationException(ErrorTexts.CANNOT_WITHDRAW_BELOW_ZERO, Response.Status.BAD_REQUEST);
			}
			Optional<Account> accountOpt = daoInstance.getAccountDao().getAccountById(accountId);
			if(!accountOpt.isPresent()){
		        throw new WebApplicationException(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		    }
			Account account = accountOpt.get();
			Status status;
			if(account.withdraw(withdrawAmount)) {
				status = Response.Status.OK;
			}
			else {
				status = Response.Status.BAD_REQUEST;
			}
			return Response
				      .status(status)
				      .entity(account)
				      .type(MediaType.APPLICATION_JSON)
				      .build();
	    }
		
		@DELETE
		@Path("/{accountId}")
		public Response delete(@PathParam("accountId") int accountId) {
			Optional<Account> accountOpt = daoInstance.getAccountDao().getAccountById(accountId);
			if(!accountOpt.isPresent()){
		        throw new WebApplicationException(ErrorTexts.ACCOUNT_DOES_NOT_EXIST, Response.Status.BAD_REQUEST);
		    }
			if(!daoInstance.getAccountDao().delete(accountId)) {
				throw new WebApplicationException(ErrorTexts.ACCOUNT_DELETION_FAILED, Response.Status.BAD_REQUEST);
			}
			return Response
				      .status(Response.Status.OK)
				      .build();
	    }
}
