package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerContactRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testGetAllCustomers_returnsMappedCustomers() {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponse("1", "John Doe", "john@example.com", "+40123456789"),
                new UserResponse("2", "Jane Doe", "jane@example.com", "+40987654321")));

        List<CustomerResponse> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).name());
    }

    @Test
    void testCreateCustomer_delegatesToUserService() {
        CreateCustomerRequest request = new CreateCustomerRequest("John Doe", "john@example.com", "+40123456789");
        when(userService.createUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse("1", "John Doe", "john@example.com", "+40123456789"));

        CustomerResponse result = customerService.createCustomer(request);

        assertEquals("1", result.id());
        assertEquals("john@example.com", result.email());
    }

    @Test
    void testUpdateCustomer_delegatesToUserService() throws EntityNotFoundException {
        UpdateCustomerRequest request = new UpdateCustomerRequest("John Updated", "new@example.com", "+40111111111");
        when(userService.updateUser(org.mockito.ArgumentMatchers.eq("1"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse("1", "John Updated", "new@example.com", "+40111111111"));

        CustomerResponse result = customerService.updateCustomer("1", request);

        assertEquals("John Updated", result.name());
        assertEquals("+40111111111", result.phone());
    }

    @Test
    void testUpdateCustomerContact_delegatesToUserService() throws EntityNotFoundException {
        UpdateCustomerContactRequest request = new UpdateCustomerContactRequest("new@example.com", "+40111111111");
        when(userService.changeContact("1", "new@example.com", "+40111111111"))
                .thenReturn(new UserResponse("1", "John Doe", "new@example.com", "+40111111111"));

        CustomerResponse result = customerService.updateCustomerContact("1", request);

        assertEquals("new@example.com", result.email());
    }

    @Test
    void testDeleteCustomer_delegatesToUserService() throws EntityNotFoundException {
        customerService.deleteCustomer("1");

        verify(userService, times(1)).deleteUser("1");
    }
}
