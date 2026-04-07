package ro.unibuc.prodeng.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "appointments")
public record AppointmentEntity(
        @Id String id,
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
