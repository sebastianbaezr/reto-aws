package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
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

class GetUserHandlerTest {

    private GetUserHandler handler;
    @Mock
    private Context context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        handler = new GetUserHandler();
    }

    @Test
    void testGetUserSuccess() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        // Use ID 2 which is a hardcoded user (María López)
        pathParameters.put("id", "550e8400-e29b-41d4-a716-446655440002");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetUserNotFound() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "550e8400-e29b-41d4-a716-446655440999");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(404, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetUserMissingId() {
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", new HashMap<>());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetUserInvalidId() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "invalid");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetUserNullPathParameters() {
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", null);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
