package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetAllUsers_withMultipleUsers_returnsAllUsers() {
        List<UserEntity> users = List.of(
                new UserEntity("1", "Alice", "alice@example.com", "+40123456789"),
                new UserEntity("2", "Bob", "bob@example.com", "+40987654321"));
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).name());
        assertEquals("+40987654321", result.get(1).phone());
    }

    @Test
    void testSearchUsersByName_whenUsersMatch_returnsCustomers() {
        List<UserEntity> users = List.of(
                new UserEntity("1", "Alice Johnson", "alice@example.com", "+40123456789"),
                new UserEntity("2", "Alicia Stone", "alicia@example.com", "+40987654321"));
        when(userRepository.findByNameContainingIgnoreCase("Alice")).thenReturn(users);

        List<UserResponse> result = userService.searchUsersByName("  Alice  ");

        assertEquals(2, result.size());
        assertEquals("Alice Johnson", result.get(0).name());
    }

    @Test
    void testGetUserById_whenUserExists_returnsMappedUser() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById("1");

        assertEquals("1", result.id());
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
        assertEquals("+40123456789", result.phone());
    }

    @Test
    void testGetUserById_whenUserMissing_throwsEntityNotFoundException() {
        when(userRepository.findById("404")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById("404"));
    }

    @Test
    void testGetUserEntityById_whenUserExists_returnsEntity() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserEntity result = userService.getUserEntityById("1");

        assertEquals(user, result);
    }

    @Test
    void testCreateUser_whenEmailAlreadyExists_throwsIllegalArgumentException() {
        CreateUserRequest request = new CreateUserRequest("Alice", "alice@example.com", "+40123456789");
        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new UserEntity("1", "Alice", "alice@example.com", "+40123456789")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request));

        assertEquals("Email already exists: alice@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_whenPhoneAlreadyExists_throwsIllegalArgumentException() {
        CreateUserRequest request = new CreateUserRequest("Alice", "alice@example.com", "+40123456789");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("+40123456789"))
                .thenReturn(Optional.of(new UserEntity("1", "Bob", "bob@example.com", "+40123456789")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request));

        assertEquals("Phone already exists: +40123456789", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_whenDataIsValid_savesAndReturnsUser() {
        CreateUserRequest request = new CreateUserRequest("  Alice   Smith ", "ALICE@EXAMPLE.COM", "+40 123-456-789");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("+40123456789")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity user = invocation.getArgument(0);
                    return new UserEntity("generated-id", user.name(), user.email(), user.phone());
                });

        UserResponse result = userService.createUser(request);

        assertNotNull(result.id());
        assertEquals("Alice Smith", result.name());
        assertEquals("alice@example.com", result.email());
        assertEquals("+40123456789", result.phone());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_whenDataIsValid_returnsUpdatedCustomer() throws EntityNotFoundException {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        UpdateUserRequest request = new UpdateUserRequest(" Alice Smith ", "ALICE.NEW@EXAMPLE.COM", "+40 111-111-111");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("alice.new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("+40111111111")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.updateUser("1", request);

        assertEquals("Alice Smith", result.name());
        assertEquals("alice.new@example.com", result.email());
        assertEquals("+40111111111", result.phone());
    }

    @Test
    void testUpdateUser_whenNothingChanges_throwsIllegalArgumentException() {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        UpdateUserRequest request = new UpdateUserRequest("Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser("1", request));

        assertEquals("Updated customer data is the same as current", exception.getMessage());
    }

    @Test
    void testChangeName_whenUserMissing_throwsEntityNotFoundException() {
        when(userRepository.findById("404")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.changeName("404", "Alicia"));
    }

    @Test
    void testChangeName_whenNameIsUnchanged_throwsIllegalArgumentException() {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeName("1", "Alice"));

        assertEquals("New name is the same as current name", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangeName_whenNameChanges_returnsUpdatedUser() throws EntityNotFoundException {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.changeName("1", "Alicia");

        assertEquals("1", result.id());
        assertEquals("Alicia", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testChangeContact_whenUserMissing_throwsEntityNotFoundException() {
        when(userRepository.findById("404")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.changeContact("404", "new@example.com", "+40111111111"));
    }

    @Test
    void testChangeContact_whenContactIsUnchanged_throwsIllegalArgumentException() {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeContact("1", "alice@example.com", "+40123456789"));

        assertEquals("New contact information is the same as current", exception.getMessage());
    }

    @Test
    void testChangeContact_whenNewEmailAlreadyExists_throwsIllegalArgumentException() {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(new UserEntity("2", "Bob", "other@example.com", "+40987654321")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeContact("1", "other@example.com", "+40123456789"));

        assertEquals("Email already exists: other@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangeContact_whenNewPhoneAlreadyExists_throwsIllegalArgumentException() {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByPhone("+40987654321"))
                .thenReturn(Optional.of(new UserEntity("2", "Bob", "other@example.com", "+40987654321")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeContact("1", "alice@example.com", "+40987654321"));

        assertEquals("Phone already exists: +40987654321", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testChangeContact_whenDataIsValid_returnsUpdatedUser() throws EntityNotFoundException {
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("+40111111111")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.changeContact("1", "new@example.com", "+40111111111");

        assertEquals("1", result.id());
        assertEquals("Alice", result.name());
        assertEquals("new@example.com", result.email());
        assertEquals("+40111111111", result.phone());
    }

    @Test
    void testDeleteUser_whenUserExists_deletesUser() throws EntityNotFoundException {
        when(userRepository.existsById("1")).thenReturn(true);

        userService.deleteUser("1");

        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteUser_whenUserMissing_throwsEntityNotFoundException() {
        when(userRepository.existsById("404")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser("404"));
    }

    @Test
    void testGetUserByEmail_whenUserExists_returnsUser() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByEmail("  ALICE@example.com ");

        assertEquals("1", result.id());
        assertEquals("Alice", result.name());
    }

    @Test
    void testGetUserEntityByEmail_whenUserExists_returnsEntity() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserEntity result = userService.getUserEntityByEmail(" ALICE@example.com ");

        assertEquals(user, result);
    }

    @Test
    void testGetUserByPhone_whenUserExists_returnsUser() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(userRepository.findByPhone("+40123456789")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByPhone("+40 123-456-789");

        assertEquals("1", result.id());
        assertEquals("+40123456789", result.phone());
    }
}
