package co.com.bancolombia.lambda.handler;

import co.com.bancolombia.lambda.dto.LambdaResponse;
import co.com.bancolombia.lambda.dto.UserRequestDto;
import co.com.bancolombia.lambda.dto.UserResponseDto;
import co.com.bancolombia.lambda.exception.LambdaException;
import co.com.bancolombia.lambda.exception.ValidationException;
import co.com.bancolombia.lambda.mapper.UserMapper;
import co.com.bancolombia.lambda.model.User;
import co.com.bancolombia.lambda.response.ResponseFactory;
import co.com.bancolombia.lambda.serialization.JsonSerializer;
import co.com.bancolombia.lambda.usecase.UpdateUserUseCase;
import co.com.bancolombia.lambda.validation.ValidationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserHandlerTest {

    private UpdateUserHandler handler;
    @Mock
    private Context context;
    @Mock
    private JsonSerializer jsonSerializer;
    @Mock
    private ResponseFactory responseFactory;
    @Mock
    private UpdateUserUseCase updateUserUseCase;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        
        handler = new UpdateUserHandler(jsonSerializer, responseFactory, 
                updateUserUseCase, userMapper, validationService);
    }

    @Test
    void testUpdateUserSuccess() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "550e8400-e29b-41d4-a716-446655440001");
        input.put("pathParameters", pathParameters);
        
        String requestBody = "{\"nombre\":\"Updated Name\",\"email\":\"updated.user@test.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("Updated Name")
                .email("updated.user@test.com")
                .build();
        User user = User.builder()
                .nombre("Updated Name")
                .email("updated.user@test.com")
                .build();
        User updatedUser = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440001")
                .nombre("Updated Name")
                .email("updated.user@test.com")
                .build();
        UserResponseDto responseDto = UserResponseDto.builder()
                .id("550e8400-e29b-41d4-a716-446655440001")
                .nombre("Updated Name")
                .email("updated.user@test.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        when(userMapper.requestToModel(requestDto)).thenReturn(user);
        when(updateUserUseCase.execute("550e8400-e29b-41d4-a716-446655440001", user)).thenReturn(updatedUser);
        when(userMapper.modelToResponse(updatedUser)).thenReturn(responseDto);
        when(jsonSerializer.toJson(responseDto)).thenReturn("{\"id\":3,\"nombre\":\"Updated Name\",\"email\":\"updated.user@test.com\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(updateUserUseCase).execute("550e8400-e29b-41d4-a716-446655440001", user);
    }

    @Test
    void testUpdateUserNotFound() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "550e8400-e29b-41d4-a716-446655440999");
        input.put("pathParameters", pathParameters);
        
        String requestBody = "{\"nombre\":\"Updated Name\",\"email\":\"updated@test.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("Updated Name")
                .email("updated@test.com")
                .build();
        User user = User.builder()
                .nombre("Updated Name")
                .email("updated@test.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        when(userMapper.requestToModel(requestDto)).thenReturn(user);
        when(updateUserUseCase.execute("550e8400-e29b-41d4-a716-446655440999", user)).thenThrow(new LambdaException("User not found with ID: 550e8400-e29b-41d4-a716-446655440999", 404));
        when(responseFactory.createError("User not found with ID: 999"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("User not found with ID: 999"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"User not found with ID: 999\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(404, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(updateUserUseCase).execute("550e8400-e29b-41d4-a716-446655440999", user);
    }

    @Test
    void testUpdateUserMissingId() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        input.put("pathParameters", new HashMap<>());
        
        String requestBody = "{\"nombre\":\"Updated Name\",\"email\":\"updated@test.com\"}";
        input.put("body", requestBody);
        
        when(responseFactory.createError("User ID is required"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("User ID is required"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"User ID is required\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService, never()).validate(any());
        verify(updateUserUseCase, never()).execute(any(), any());
    }

    @Test
    void testUpdateUserMissingBody() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "550e8400-e29b-41d4-a716-446655440002");
        input.put("pathParameters", pathParameters);
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
        verify(updateUserUseCase, never()).execute(any(), any());
    }

    @Test
    void testUpdateUserWithDuplicateEmail() {
        // Arrange
        Map<String, Object> input = new HashMap<>();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "2");
        input.put("pathParameters", pathParameters);
        
        String requestBody = "{\"nombre\":\"Updated Name\",\"email\":\"juan.perez@bancolombia.com\"}";
        input.put("body", requestBody);
        
        UserRequestDto requestDto = UserRequestDto.builder()
                .nombre("Updated Name")
                .email("juan.perez@bancolombia.com")
                .build();
        User user = User.builder()
                .nombre("Updated Name")
                .email("juan.perez@bancolombia.com")
                .build();
        
        when(jsonSerializer.fromJson(requestBody, UserRequestDto.class)).thenReturn(requestDto);
        when(userMapper.requestToModel(requestDto)).thenReturn(user);
        when(updateUserUseCase.execute("550e8400-e29b-41d4-a716-446655440002", user)).thenThrow(new ValidationException("Email already exists: juan.perez@bancolombia.com"));
        when(responseFactory.createError("Email already exists: juan.perez@bancolombia.com"))
                .thenReturn(new co.com.bancolombia.lambda.dto.ErrorResponseDto("Email already exists: juan.perez@bancolombia.com"));
        when(jsonSerializer.toJson(any())).thenReturn("{\"error\":\"Email already exists: juan.perez@bancolombia.com\"}");
        
        // Act
        LambdaResponse response = handler.handleRequest(input, context);
        
        // Assert
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validationService).validate(requestDto);
        verify(updateUserUseCase).execute("550e8400-e29b-41d4-a716-446655440002", user);
    }
}
