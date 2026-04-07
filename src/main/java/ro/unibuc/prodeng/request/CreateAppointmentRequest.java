package ro.unibuc.prodeng.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
        @NotBlank(message = "Customer name is required")
        @Size(min = 2, max = 50, message = "Customer name must be between 2 and 50 characters")
        String customerName,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Customer email is required")
        String customerEmail,

        @NotBlank(message = "Customer phone is required")
        @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{10,15}$", message = "Phone must be 10-15 characters, can include +, spaces, -, (, )")
        String customerPhone,

        @NotBlank(message = "Vehicle brand is required")
        @Size(max = 30, message = "Vehicle brand must not exceed 30 characters")
        String vehicleBrand,

        @NotBlank(message = "Vehicle model is required")
        @Size(max = 40, message = "Vehicle model must not exceed 40 characters")
        String vehicleModel,

        @NotBlank(message = "Registration number is required")
        @Size(max = 15, message = "Registration number must not exceed 15 characters")
        String vehicleRegistrationNumber,

        @NotBlank(message = "Service type is required")
        @Size(max = 60, message = "Service type must not exceed 60 characters")
        String serviceType,

        @NotNull(message = "Appointment date is required")
        @Future(message = "Appointment date must be in the future")
        LocalDateTime appointmentAt,

        @Size(max = 300, message = "Notes must not exceed 300 characters")
        String notes) {
}
