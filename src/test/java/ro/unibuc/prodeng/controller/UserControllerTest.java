package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.ChangeNameRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.request.UpdateContactRequest;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserResponse user1 = new UserResponse("1", "John Doe", "john@example.com", "+40123456789");
    private final UserResponse user2 = new UserResponse("2", "Jane Smith", "jane@example.com", "+40987654321");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllUsers_withMultipleUsers_returnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].phone", is("+40123456789")))
                .andExpect(jsonPath("$[1].email", is("jane@example.com")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserById_whenUserExists_returnsUser() throws Exception {
        when(userService.getUserById("1")).thenReturn(user1);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.phone", is("+40123456789")));
    }

    @Test
    void testGetUserById_whenUserMissing_returnsNotFound() throws Exception {
        when(userService.getUserById("404")).thenThrow(new EntityNotFoundException("404"));

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Entity: 404 was not found")));
    }

    @Test
    void testCreateUser_whenPayloadIsValid_returnsCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", "+40123456789");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(user1);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    void testCreateUser_whenPayloadIsInvalid_returnsBadRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "invalid", "123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasKey("name")))
                .andExpect(jsonPath("$", hasKey("email")))
                .andExpect(jsonPath("$", hasKey("phone")));
    }

    @Test
    void testUpdateUserContact_whenPayloadIsValid_returnsUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("John Updated", "new@example.com", "+40111111111");
        UserResponse updated = new UserResponse("1", "John Updated", "new@example.com", "+40111111111");
        when(userService.updateUser("1", request)).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.phone", is("+40111111111")));
    }

    @Test
    void testPatchContact_whenPayloadIsValid_returnsUpdatedUser() throws Exception {
        UpdateContactRequest request = new UpdateContactRequest("new@example.com", "+40111111111");
        UserResponse updated = new UserResponse("1", "John Doe", "new@example.com", "+40111111111");
        when(userService.changeContact("1", "new@example.com", "+40111111111")).thenReturn(updated);

        mockMvc.perform(patch("/api/users/1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.phone", is("+40111111111")));
    }

    @Test
    void testChangeName_whenPayloadIsValid_returnsUpdatedUser() throws Exception {
        ChangeNameRequest request = new ChangeNameRequest("Johnny Doe");
        UserResponse updated = new UserResponse("1", "Johnny Doe", "john@example.com", "+40123456789");
        when(userService.changeName("1", "Johnny Doe")).thenReturn(updated);

        mockMvc.perform(patch("/api/users/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Johnny Doe")));
    }

    @Test
    void testDeleteUser_whenUserExists_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser("1");
    }

    @Test
    void testGetUserByEmail_whenUserExists_returnsUser() throws Exception {
        when(userService.getUserByEmail("john@example.com")).thenReturn(user1);

        mockMvc.perform(get("/api/users/by-email").param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    void testSearchUsersByName_whenQueryIsValid_returnsMatchingUsers() throws Exception {
        when(userService.searchUsersByName("John")).thenReturn(List.of(user1));

        mockMvc.perform(get("/api/users/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("1")));
    }

    @Test
    void testGetUserByPhone_whenUserExists_returnsUser() throws Exception {
        when(userService.getUserByPhone("+40123456789")).thenReturn(user1);

        mockMvc.perform(get("/api/users/by-phone").param("phone", "+40123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }
}
