package moneytransfer.service;

public class ErrorTexts {
	/*
	 * Account texts
	 */
	public static final String ACCOUNT_DOES_NOT_EXIST = "account with id %s does not exist";
	public static final String ACCOUNT_CREATION_FAILED = "account with id %s does not exist";
	public static final String CANNOT_DEPOSIT_BELOW_ZERO = "cannot deposit a value of 0 or less";
	public static final String CANNOT_WITHDRAW_BELOW_ZERO = "account with id %s does not exist";
	public static final String ACCOUNT_DELETION_FAILED = "The account deletion failed";
	public static final String SOURCE_ACCOUNT_DOES_NOT_EXIST = "Source account does not exist";
	public static final String DESTINATION_ACCOUNT_DOES_NOT_EXIST = "Destination account does not exist";
	
	/*
	 * Transfer texts
	 */
	public static final String TRANSFER_DOES_NOT_EXIST = "Transfer does not exist";
	public static final String TRANSFER_CREATION_FAILED = "Transfer creation failed";
	public static final String TRANSFER_ZERO_OR_BELOW = "Cannot transfer 0 or less";
	public static final String TRANSFER_FAILED = "Transfer failed";
}
