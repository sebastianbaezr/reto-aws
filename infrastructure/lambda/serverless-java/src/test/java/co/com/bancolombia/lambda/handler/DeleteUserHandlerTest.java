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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteUserHandlerTest {

    private DeleteUserHandler handler;
    @Mock
    private Context context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        handler = new DeleteUserHandler();
    }

    @Test
    void testDeleteUserSuccess() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "1");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteUserNotFound() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "999");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(404, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteUserMissingId() {
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", new HashMap<>());

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteUserInvalidId() {
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "invalid");
        input.put("pathParameters", pathParameters);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteUserNullPathParameters() {
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", null);

        LambdaResponse response = handler.handleRequest(input, context);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
