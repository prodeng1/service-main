package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAppointmentStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(
                regexp = "REQUESTED|CONFIRMED|COMPLETED|CANCELLED",
                message = "Status must be one of: REQUESTED, CONFIRMED, COMPLETED, CANCELLED")
        String status) {
}
