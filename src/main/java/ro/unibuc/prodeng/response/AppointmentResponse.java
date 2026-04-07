package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        String id,
        String customerName,
        String customerEmail,
        String customerPhone,
        String vehicleBrand,
        String vehicleModel,
        String vehicleRegistrationNumber,
        String serviceType,
        LocalDateTime appointmentAt,
        String status,
        String notes) {
}
