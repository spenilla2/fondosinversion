package com.btg.fondos.infrastructure.adapter.out.persistence;

import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.domain.port.out.TransactionRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoTransactionRepository implements TransactionRepository {

    private static final String TABLE_NAME = "transactions";
    private final DynamoDbClient dynamoDbClient;

    public DynamoTransactionRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public Transaction save(Transaction transaction) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(transaction.getId()).build());
        item.put("clientId", AttributeValue.builder().s(transaction.getClientId()).build());
        item.put("fundId", AttributeValue.builder().s(transaction.getFundId()).build());
        item.put("fundName", AttributeValue.builder().s(transaction.getFundName()).build());
        item.put("type", AttributeValue.builder().s(transaction.getType()).build());
        item.put("amount", AttributeValue.builder().n(transaction.getAmount().toPlainString()).build());
        item.put("timestamp", AttributeValue.builder().s(transaction.getTimestamp().toString()).build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        return transaction;
    }

    @Override
    public List<Transaction> findByClientId(String clientId) {
        QueryResponse response = dynamoDbClient.query(QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("clientId-index")
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(Map.of(
                        ":clientId", AttributeValue.builder().s(clientId).build()
                ))
                .scanIndexForward(false)
                .build());

        return response.items().stream()
                .map(this::mapToTransaction)
                .toList();
    }

    private Transaction mapToTransaction(Map<String, AttributeValue> item) {
        return new Transaction(
                item.get("id").s(),
                item.get("clientId").s(),
                item.get("fundId").s(),
                item.get("fundName").s(),
                item.get("type").s(),
                new BigDecimal(item.get("amount").n()),
                Instant.parse(item.get("timestamp").s())
        );
    }
}
