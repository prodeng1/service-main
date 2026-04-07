package ro.unibuc.prodeng.controller;

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

import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.ChangeCustomerNameRequest;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerContactRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CustomerResponse customer = new CustomerResponse("1", "John Doe", "john@example.com", "+40123456789");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllCustomers_returnsCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("john@example.com")));
    }

    @Test
    void testCreateCustomer_returnsCreatedCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("John Doe", "john@example.com", "+40123456789");
        when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(customer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")));
    }

    @Test
    void testUpdateCustomer_returnsUpdatedCustomer() throws Exception {
        UpdateCustomerRequest request = new UpdateCustomerRequest("John Updated", "new@example.com", "+40111111111");
        CustomerResponse updated = new CustomerResponse("1", "John Updated", "new@example.com", "+40111111111");
        when(customerService.updateCustomer("1", request)).thenReturn(updated);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")));
    }

    @Test
    void testUpdateCustomerContact_returnsUpdatedCustomer() throws Exception {
        UpdateCustomerContactRequest request = new UpdateCustomerContactRequest("new@example.com", "+40111111111");
        CustomerResponse updated = new CustomerResponse("1", "John Doe", "new@example.com", "+40111111111");
        when(customerService.updateCustomerContact("1", request)).thenReturn(updated);

        mockMvc.perform(patch("/api/customers/1/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("+40111111111")));
    }

    @Test
    void testChangeCustomerName_returnsUpdatedCustomer() throws Exception {
        ChangeCustomerNameRequest request = new ChangeCustomerNameRequest("Johnny Doe");
        CustomerResponse updated = new CustomerResponse("1", "Johnny Doe", "john@example.com", "+40123456789");
        when(customerService.changeCustomerName("1", "Johnny Doe")).thenReturn(updated);

        mockMvc.perform(patch("/api/customers/1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Johnny Doe")));
    }

    @Test
    void testSearchCustomers_returnsMatchingCustomers() throws Exception {
        when(customerService.searchCustomersByName("John")).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("1")));
    }

    @Test
    void testDeleteCustomer_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer("1");
    }
}
