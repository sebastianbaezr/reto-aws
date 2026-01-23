package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateUserHandlerTest {

    private CreateUserHandler handler;
    @Mock
    private Context context;
    private Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        handler = new CreateUserHandler();
    }

    @Test
    void testCreateUserSuccess() {
        Map<String, Object> input = new HashMap<>();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "New User");
        userJson.addProperty("email", "newuser@test.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        Map<String, Object> input = new HashMap<>();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "Juan Pérez");
        userJson.addProperty("email", "juan.perez@bancolombia.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserMissingEmail() {
        Map<String, Object> input = new HashMap<>();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "New User");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserMissingNombre() {
        Map<String, Object> input = new HashMap<>();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("email", "test@test.com");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserInvalidEmail() {
        Map<String, Object> input = new HashMap<>();
        JsonObject userJson = new JsonObject();
        userJson.addProperty("nombre", "New User");
        userJson.addProperty("email", "invalidemail");
        input.put("body", userJson.toString());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserEmptyBody() {
        Map<String, Object> input = new HashMap<>();
        input.put("body", "");

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserNullBody() {
        Map<String, Object> input = new HashMap<>();
        input.put("body", null);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateUserInvalidJson() {
        Map<String, Object> input = new HashMap<>();
        input.put("body", "{invalid json}");

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
