package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.ChangeCustomerNameRequest;
import ro.unibuc.prodeng.request.CreateCustomerRequest;
import ro.unibuc.prodeng.request.UpdateCustomerContactRequest;
import ro.unibuc.prodeng.request.UpdateCustomerRequest;
import ro.unibuc.prodeng.response.CustomerResponse;
import ro.unibuc.prodeng.service.CustomerService;

@RestController
@Validated
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable String id) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @PatchMapping("/{id}/contact")
    public ResponseEntity<CustomerResponse> updateCustomerContact(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerContactRequest request) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.updateCustomerContact(id, request));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<CustomerResponse> changeCustomerName(
            @PathVariable String id,
            @Valid @RequestBody ChangeCustomerNameRequest request) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.changeCustomerName(id, request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) throws EntityNotFoundException {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(
            @RequestParam @Email(message = "Invalid email format") String email) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @GetMapping("/by-phone")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(
            @RequestParam
            @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,15}$", message = "Phone must be 10-15 characters, can include +, spaces, -, (, )")
            String phone) throws EntityNotFoundException {
        return ResponseEntity.ok(customerService.getCustomerByPhone(phone));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomersByName(
            @RequestParam
            @Size(min = 2, max = 50, message = "Search query must be between 2 and 50 characters")
            String name) {
        return ResponseEntity.ok(customerService.searchCustomersByName(name));
    }
}
