package moneytransfer.service.utils;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import moneytransfer.model.Transfer;

public class TransferDeserializer extends StdDeserializer<Transfer> {
	private static final long serialVersionUID = 1L;

	public TransferDeserializer() {
        this(null);
    }
 
    public TransferDeserializer(Class<?> vc) {
        super(vc);
    }
 
    @Override
    public Transfer deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);
        //JsonObject jsonObj = codec.readTree(parser);
        JsonNode fromAccountIdNode = node.get("fromAccountId");
        JsonNode toAccountIdNode = node.get("toAccountId");
        JsonNode transferAmountNode = node.get("transferAmount");
        
        int fromAccountId = fromAccountIdNode.asInt();
        int toAccountId = toAccountIdNode.asInt();
        BigDecimal transferAmount = new BigDecimal(transferAmountNode.asText());
        return new Transfer(fromAccountId, toAccountId, transferAmount);
    }
}
