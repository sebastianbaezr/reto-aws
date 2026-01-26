package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.dto.UserRequestDto;
import co.com.bancolombia.lambda.dto.UserResponseDto;
import co.com.bancolombia.lambda.exception.ValidationException;
import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.service.SqsMessagePublisher;
import co.com.bancolombia.lambda.usecase.CreateUserUseCase;
import co.com.bancolombia.lambda.validation.ValidationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CreateUserHandlerTest {

    private CreateUserHandler handler;
    @Mock
    private Context context;
    @Mock
    private JsonSerializer jsonSerializer;
    @Mock
    private ResponseFactory responseFactory;
    @Mock
    private CreateUserUseCase createUserUseCase;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ValidationService validationService;
    @Mock
    private SqsMessagePublisher sqsMessagePublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        when(context.getAwsRequestId()).thenReturn("test-request-id");
        when(sqsMessagePublisher.publishUserCreatedEvent(any(), anyString())).thenReturn(null);

        handler = new CreateUserHandler(jsonSerializer, responseFactory,
                createUserUseCase, userMapper, validationService, sqsMessagePublisher);
    }

    @Test
    void testCreateUserSuccess() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String requestBody = "{\"nombre\":\"New User\",\"email\":\"newuser@test.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("New User")
                .email("newuser@test.com")
                .build();
        User user = User.builder()
                .nombre("New User")
                .email("newuser@test.com")
                .build();
        User createdUser = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .nombre("New User")
                .email("newuser@test.com")
                .build();
        UserResponseDto responseDto = UserResponseDto.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .nombre("New User")
                .email("newuser@test.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        when(userMapper.requestToModel(requestDto)).thenReturn(user);
        when(createUserUseCase.execute(user)).thenReturn(createdUser);
        when(userMapper.modelToResponse(createdUser)).thenReturn(responseDto);
        when(jsonSerializer.toJson(responseDto)).thenReturn("{\"id\":1,\"nombre\":\"New User\",\"email\":\"newuser@test.com\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(createUserUseCase).execute(user);
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String requestBody = "{\"nombre\":\"Juan Pérez\",\"email\":\"juan.perez@bancolombia.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("Juan Pérez")
                .email("juan.perez@bancolombia.com")
                .build();
        User user = User.builder()
                .nombre("Juan Pérez")
                .email("juan.perez@bancolombia.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        when(userMapper.requestToModel(requestDto)).thenReturn(user);
        when(createUserUseCase.execute(user)).thenThrow(new ValidationException("Email already exists: juan.perez@bancolombia.com"));
        when(responseFactory.createError("Email already exists: juan.perez@bancolombia.com"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Email already exists: juan.perez@bancolombia.com"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Email already exists: juan.perez@bancolombia.com\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(createUserUseCase).execute(user);
    }

    @Test
    void testCreateUserMissingEmail() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String requestBody = "{\"nombre\":\"New User\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("New User")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        doThrow(new ValidationException("Email is required")).when(validationService).validate(requestDto);
        when(responseFactory.createError("Email is required"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Email is required"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Email is required\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void testCreateUserMissingNombre() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String requestBody = "{\"email\":\"test@test.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .email("test@test.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        doThrow(new ValidationException("Nombre is required")).when(validationService).validate(requestDto);
        when(responseFactory.createError("Nombre is required"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Nombre is required"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Nombre is required\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void testCreateUserInvalidEmail() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String requestBody = "{\"nombre\":\"New User\",\"email\":\"invalidemail\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("New User")
                .email("invalidemail")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        doThrow(new ValidationException("Invalid email format")).when(validationService).validate(requestDto);
        when(responseFactory.createError("Invalid email format"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Invalid email format"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Invalid email format\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void testCreateUserEmptyBody() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        input.put("body", "");
        
        when(responseFactory.createError("Request body is required"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Request body is required"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Request body is required\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService, never()).validate(any());
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void testCreateUserNullBody() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        input.put("body", null);
        
        when(responseFactory.createError("Request body is required"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Request body is required"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Request body is required\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService, never()).validate(any());
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void testCreateUserInvalidJson() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        String invalidJson = "{invalid json}";
        input.put("body", invalidJson);
        
        when(jsonSerializer.fromJson(invalidJson, UserRequestDto.class))
                .thenThrow(new com.google.gson.JsonSyntaxException("Invalid JSON"));
        when(responseFactory.createError("Invalid JSON format"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Invalid JSON format"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Invalid JSON format\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService, never()).validate(any());
        verify(createUserUseCase, never()).execute(any());
    }
}
