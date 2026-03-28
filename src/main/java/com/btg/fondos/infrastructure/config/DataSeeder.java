package com.btg.fondos.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.UUID;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedData(DynamoDbClient dynamoDbClient, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Iniciando seed de datos...");
            createTableIfNotExists(dynamoDbClient, "funds", "id");
            createTableIfNotExists(dynamoDbClient, "clients", "id");
            createTransactionsTableIfNotExists(dynamoDbClient);

            seedFunds(dynamoDbClient);
            log.info("Fondos insertados.");
            seedDefaultClient(dynamoDbClient, passwordEncoder);
            log.info("Cliente demo insertado.");
            seedAdminClient(dynamoDbClient, passwordEncoder);
            log.info("Admin insertado.");
            log.info("Seed de datos completado.");
        };
    }

    private void createTableIfNotExists(DynamoDbClient client, String tableName, String keyName) {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
        } catch (ResourceNotFoundException e) {
            client.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder().attributeName(keyName).keyType(KeyType.HASH).build())
                    .attributeDefinitions(AttributeDefinition.builder().attributeName(keyName).attributeType(ScalarAttributeType.S).build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            client.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
        }
    }

    private void createTransactionsTableIfNotExists(DynamoDbClient client) {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName("transactions").build());
        } catch (ResourceNotFoundException e) {
            client.createTable(CreateTableRequest.builder()
                    .tableName("transactions")
                    .keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build())
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("clientId").attributeType(ScalarAttributeType.S).build()
                    )
                    .globalSecondaryIndexes(GlobalSecondaryIndex.builder()
                            .indexName("clientId-index")
                            .keySchema(KeySchemaElement.builder().attributeName("clientId").keyType(KeyType.HASH).build())
                            .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
            client.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName("transactions").build());
        }
    }

    private void seedFunds(DynamoDbClient client) {
        putFund(client, "1", "FPV_BTG_PACTUAL_RECAUDADORA", "75000", "FPV");
        putFund(client, "2", "FPV_BTG_PACTUAL_ECOPETROL", "125000", "FPV");
        putFund(client, "3", "DEUDAPRIVADA", "50000", "FIC");
        putFund(client, "4", "FDO-ACCIONES", "250000", "FIC");
        putFund(client, "5", "FPV_BTG_PACTUAL_DINAMICA", "100000", "FPV");
    }

    private void putFund(DynamoDbClient client, String id, String name, String minAmount, String category) {
        client.putItem(PutItemRequest.builder()
                .tableName("funds")
                .item(Map.of(
                        "id", AttributeValue.builder().s(id).build(),
                        "name", AttributeValue.builder().s(name).build(),
                        "minimumAmount", AttributeValue.builder().n(minAmount).build(),
                        "category", AttributeValue.builder().s(category).build()
                ))
                .build());
    }

    private void seedDefaultClient(DynamoDbClient client, PasswordEncoder passwordEncoder) {
        seedClient(client, passwordEncoder, "client-001", "Cliente Demo",
                "demo@btgpactual.com", "+573001234567", "btg2025", "ROLE_CLIENT");
    }

    private void seedAdminClient(DynamoDbClient client, PasswordEncoder passwordEncoder) {
        seedClient(client, passwordEncoder, "admin-001", "Administrador BTG",
                "admin@btgpactual.com", "+573009999999", "admin2025", "ROLE_ADMIN");
    }

    private void seedClient(DynamoDbClient client, PasswordEncoder passwordEncoder,
                            String user, String name, String email, String phone,
                            String password, String role) {
        ScanResponse scan = client.scan(ScanRequest.builder()
                .tableName("clients")
                .filterExpression("email = :email")
                .expressionAttributeValues(Map.of(":email", AttributeValue.builder().s(email).build()))
                .build());

        if (scan.items().isEmpty()) {
            String id = UUID.randomUUID().toString();
            client.putItem(PutItemRequest.builder()
                    .tableName("clients")
                    .item(Map.of(
                            "id", AttributeValue.builder().s(id).build(),
                            "user", AttributeValue.builder().s(user).build(),
                            "name", AttributeValue.builder().s(name).build(),
                            "email", AttributeValue.builder().s(email).build(),
                            "phone", AttributeValue.builder().s(phone).build(),
                            "balance", AttributeValue.builder().n("500000").build(),
                            "preferredNotification", AttributeValue.builder().s("EMAIL").build(),
                            "subscribedFundIds", AttributeValue.builder().ss("EMPTY").build(),
                            "password", AttributeValue.builder().s(passwordEncoder.encode(password)).build(),
                            "role", AttributeValue.builder().s(role).build()
                    ))
                    .build());
            log.info("Cliente creado - user: {}, email: {}, id: {}, role: {}", user, email, id, role);
        } else {
            log.info("Cliente ya existe - email: {}", email);
        }
    }
}
