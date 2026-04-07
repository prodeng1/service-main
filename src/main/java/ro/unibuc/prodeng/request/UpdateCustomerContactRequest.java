package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerContactRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,15}$", message = "Phone must be 10-15 characters, can include +, spaces, -, (, )")
    String phone
) {}
