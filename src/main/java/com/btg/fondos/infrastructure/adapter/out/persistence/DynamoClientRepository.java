package com.btg.fondos.infrastructure.adapter.out.persistence;

import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.out.ClientRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.util.*;

@Repository
public class DynamoClientRepository implements ClientRepository {

    private static final String TABLE_NAME = "clients";
    private final DynamoDbClient dynamoDbClient;

    public DynamoClientRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public Optional<Client> findById(String id) {
        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToClient(response.item()));
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("email = :email")
                .expressionAttributeValues(Map.of(":email", AttributeValue.builder().s(email).build()))
                .build());

        if (response.items().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToClient(response.items().get(0)));
    }

    @Override
    public Optional<Client> findByUser(String user) {
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("#u = :user")
                .expressionAttributeNames(Map.of("#u", "user"))
                .expressionAttributeValues(Map.of(":user", AttributeValue.builder().s(user).build()))
                .build());

        if (response.items().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToClient(response.items().get(0)));
    }

    @Override
    public Client save(Client client) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(client.getId()).build());
        item.put("user", AttributeValue.builder().s(client.getUser()).build());
        item.put("name", AttributeValue.builder().s(client.getName()).build());
        item.put("email", AttributeValue.builder().s(client.getEmail()).build());
        item.put("phone", AttributeValue.builder().s(client.getPhone()).build());
        item.put("balance", AttributeValue.builder().n(client.getBalance().toPlainString()).build());
        item.put("preferredNotification", AttributeValue.builder().s(client.getPreferredNotification()).build());
        item.put("password", AttributeValue.builder().s(client.getPassword()).build());
        item.put("role", AttributeValue.builder().s(client.getRole()).build());

        if (client.getSubscribedFundIds() != null && !client.getSubscribedFundIds().isEmpty()) {
            item.put("subscribedFundIds", AttributeValue.builder().ss(client.getSubscribedFundIds()).build());
        } else {
            item.put("subscribedFundIds", AttributeValue.builder().ss("EMPTY").build());
        }

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        return client;
    }

    @Override
    public void deleteById(String id) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build());
    }

    @Override
    public List<Client> findAll() {
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build());

        return response.items().stream()
                .map(this::mapToClient)
                .toList();
    }

    private Client mapToClient(Map<String, AttributeValue> item) {
        List<String> fundIds = new ArrayList<>();
        if (item.containsKey("subscribedFundIds")) {
            fundIds = new ArrayList<>(item.get("subscribedFundIds").ss());
            fundIds.remove("EMPTY");
        }

        return new Client(
                item.get("id").s(),
                item.containsKey("user") ? item.get("user").s() : "",
                item.get("name").s(),
                item.get("email").s(),
                item.containsKey("phone") ? item.get("phone").s() : "",
                new BigDecimal(item.get("balance").n()),
                item.containsKey("preferredNotification") ? item.get("preferredNotification").s() : "EMAIL",
                fundIds,
                item.get("password").s(),
                item.containsKey("role") ? item.get("role").s() : "ROLE_CLIENT"
        );
    }
}
