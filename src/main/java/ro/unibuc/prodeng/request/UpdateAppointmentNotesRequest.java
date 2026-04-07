package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAppointmentNotesRequest(
        @NotBlank(message = "Notes are required")
        @Size(max = 300, message = "Notes must not exceed 300 characters")
        String notes) {
}
