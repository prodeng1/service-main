package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import ro.unibuc.prodeng.request.ChangeNameRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.request.UpdateContactRequest;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.service.UserService;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) throws EntityNotFoundException {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) throws EntityNotFoundException {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/contact")
    public ResponseEntity<UserResponse> updateUserContact(
            @PathVariable String id,
            @Valid @RequestBody UpdateContactRequest request) throws EntityNotFoundException {
        UserResponse user = userService.changeContact(id, request.email(), request.phone());
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<UserResponse> changeName(
            @PathVariable String id,
            @Valid @RequestBody ChangeNameRequest request) throws EntityNotFoundException {
        UserResponse user = userService.changeName(id, request.name());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) throws EntityNotFoundException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @RequestParam
            @Email(message = "Invalid email format")
            String email)
            throws EntityNotFoundException {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsersByName(
            @RequestParam
            @Size(min = 2, max = 50, message = "Search query must be between 2 and 50 characters")
            String name) {
        List<UserResponse> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-phone")
    public ResponseEntity<UserResponse> getUserByPhone(
            @RequestParam
            @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,15}$", message = "Phone must be 10-15 characters, can include +, spaces, -, (, )")
            String phone)
            throws EntityNotFoundException {
        UserResponse user = userService.getUserByPhone(phone);
        return ResponseEntity.ok(user);
    }
}
