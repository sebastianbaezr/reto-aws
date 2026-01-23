package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateUserHandlerTest {

    private UpdateUserHandler handler;
    @Mock
    private Context context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        handler = new UpdateUserHandler();
    }

    @Test
    void testUpdateUserSuccess() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        // Use ID 3 which is a hardcoded user
        pathParameters.put("id", "3");
        input.put("pathParameters", pathParameters);

        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "Updated Name");
        userJson.addProperty("email", "updated.user@test.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateUserNotFound() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "999");
        input.put("pathParameters", pathParameters);

        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "Updated Name");
        userJson.addProperty("email", "updated@test.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(404, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateUserMissingId() {
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", new HashMap<>());

        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "Updated Name");
        userJson.addProperty("email", "updated@test.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateUserMissingBody() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "1");
        input.put("pathParameters", pathParameters);
        input.put("body", "");

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateUserWithDuplicateEmail() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        // Use ID 2 and try to update with ID 1's email
        pathParameters.put("id", "2");
        input.put("pathParameters", pathParameters);

        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "Updated Name");
        // Try to set email to one of the hardcoded users' emails
        userJson.addProperty("email", "juan.perez@bancolombia.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
