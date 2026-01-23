package co.com.bancolombia.lambda.repository;

import co.com.bancolombia.lambda.model.User;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DynamoDbUserRepository implements UserRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbUserRepository() {
        this.dynamoDbClient = DynamoDbClient.builder().build();

        String tableName = System.getenv("DYNAMODB_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("DYNAMODB_TABLE_NAME environment variable must be set");
        }

        this.tableName = tableName;
    }

    @Override
    public User create(User user) {
        try {
            String id = UUID.randomUUID().toString();
            User newUser = user.toBuilder().id(id).build();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(newUser.getId()).build());
            item.put("nombre", AttributeValue.builder().s(newUser.getNombre()).build());
            item.put("email", AttributeValue.builder().s(newUser.getEmail()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            return newUser;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to create user in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

            if (!response.hasItem()) {
                return Optional.empty();
            }

            return Optional.of(mapFromDynamoDb(response.item()));
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to find user in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(String id, User user) {
        try {
            User updatedUser = user.toBuilder().id(id).build();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(updatedUser.getId()).build());
            item.put("nombre", AttributeValue.builder().s(updatedUser.getNombre()).build());
            item.put("email", AttributeValue.builder().s(updatedUser.getEmail()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
            return updatedUser;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to update user in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();

            dynamoDbClient.deleteItem(deleteItemRequest);
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to delete user in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email) {
        try {
            return queryByEmail(email).size() > 0;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to check email existence in DynamoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExistsExcept(String email, String excludeId) {
        try {
            java.util.List<User> results = queryByEmail(email);
            return results.stream()
                    .anyMatch(user -> !user.getId().equals(excludeId));
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to check email existence in DynamoDB: " + e.getMessage(), e);
        }
    }

    private java.util.List<User> queryByEmail(String email) {
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":email", AttributeValue.builder().s(email).build());

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("email-index")
                    .keyConditionExpression("email = :email")
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);

            return response.items().stream()
                    .map(this::mapFromDynamoDb)
                    .toList();
        } catch (DynamoDbException e) {
            throw new RuntimeException("Failed to query by email in DynamoDB: " + e.getMessage(), e);
        }
    }

    private User mapFromDynamoDb(Map<String, AttributeValue> item) {
        return User.builder()
                .id(item.get("id").s())
                .nombre(item.get("nombre").s())
                .email(item.get("email").s())
                .build();
    }
}
