package com.btg.fondos.infrastructure.adapter.out.persistence;

import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.port.out.FundRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoFundRepository implements FundRepository {

    private static final String TABLE_NAME = "funds";
    private final DynamoDbClient dynamoDbClient;

    public DynamoFundRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public Optional<Fund> findById(String id) {
        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapToFund(response.item()));
    }

    @Override
    public List<Fund> findAll() {
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build());

        return response.items().stream()
                .map(this::mapToFund)
                .toList();
    }

    private Fund mapToFund(Map<String, AttributeValue> item) {
        return new Fund(
                item.get("id").s(),
                item.get("name").s(),
                new BigDecimal(item.get("minimumAmount").n()),
                item.get("category").s()
        );
    }
}
