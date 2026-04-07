package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerContactRequest;
import ro.unibuc.prodeng.request.UpdateUserRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.response.UserResponse;

@Service
public class CustomerService {

    @Autowired
    private UserService userService;

    public List<CustomerResponse> getAllCustomers() {
        return userService.getAllUsers().stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    public List<CustomerResponse> searchCustomersByName(String name) {
        return userService.searchUsersByName(name).stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(String id) throws EntityNotFoundException {
        return toCustomerResponse(userService.getUserById(id));
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        return toCustomerResponse(userService.createUser(
                new CreateUserRequest(request.name(), request.email(), request.phone())));
    }

    public CustomerResponse updateCustomer(String id, UpdateCustomerRequest request) throws EntityNotFoundException {
        return toCustomerResponse(userService.updateUser(
                id,
                new UpdateUserRequest(request.name(), request.email(), request.phone())));
    }

    public CustomerResponse updateCustomerContact(String id, UpdateCustomerContactRequest request)
            throws EntityNotFoundException {
        return toCustomerResponse(userService.changeContact(id, request.email(), request.phone()));
    }

    public CustomerResponse changeCustomerName(String id, String name) throws EntityNotFoundException {
        return toCustomerResponse(userService.changeName(id, name));
    }

    public void deleteCustomer(String id) throws EntityNotFoundException {
        userService.deleteUser(id);
    }

    public CustomerResponse getCustomerByEmail(String email) throws EntityNotFoundException {
        return toCustomerResponse(userService.getUserByEmail(email));
    }

    public CustomerResponse getCustomerByPhone(String phone) throws EntityNotFoundException {
        return toCustomerResponse(userService.getUserByPhone(phone));
    }

    private CustomerResponse toCustomerResponse(UserResponse user) {
        return new CustomerResponse(user.id(), user.name(), user.email(), user.phone());
    }
}
