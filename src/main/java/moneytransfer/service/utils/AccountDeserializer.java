package moneytransfer.service.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import moneytransfer.model.Account;

public class AccountDeserializer extends StdDeserializer<Account> {
	private static final long serialVersionUID = 1L;

	public AccountDeserializer() {
        this(null);
    }
 
    public AccountDeserializer(Class<?> vc) {
        super(vc);
    }
 
    @Override
    public Account deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);
        JsonNode accountHolderNode = node.get("accountHolder");
        String accountHolder = accountHolderNode.asText();
        
        if(!isAlphabetic(accountHolder.replaceAll("\\s+",""))) {
        	throw new IOException("Accountholder value must be alphabetic");
        }
        return new Account(accountHolder);
    }
    
    public boolean isAlphabetic(String name) {
        return name.matches("[a-zA-Z]+");
    }
}
