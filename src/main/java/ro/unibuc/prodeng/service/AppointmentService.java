package ro.unibuc.prodeng.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.AppointmentEntity;
import ro.unibuc.prodeng.repository.AppointmentRepository;
import ro.unibuc.prodeng.request.CreateAppointmentRequest;
import ro.unibuc.prodeng.response.AppointmentResponse;

@Service
public class AppointmentService {

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentAtAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponse getAppointmentById(String id) throws EntityNotFoundException {
        return toResponse(getEntityById(id));
    }

    public List<AppointmentResponse> getAppointmentsByCustomerEmail(String customerEmail) {
        return appointmentRepository.findByCustomerEmailIgnoreCaseOrderByAppointmentAtAsc(customerEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsForDay(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        return appointmentRepository.findByAppointmentAtBetweenOrderByAppointmentAtAsc(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        validateAppointmentSlot(request.appointmentAt());
        validateDuplicateCustomerAppointment(request.customerEmail(), request.appointmentAt());

        AppointmentEntity appointment = new AppointmentEntity(
                null,
                request.customerName(),
                request.customerEmail(),
                request.customerPhone(),
                request.vehicleBrand(),
                request.vehicleModel(),
                request.vehicleRegistrationNumber(),
                request.serviceType(),
                request.appointmentAt(),
                "REQUESTED",
                request.notes());

        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse updateStatus(String id, String newStatus) throws EntityNotFoundException {
        AppointmentEntity existing = getEntityById(id);
        validateStatusTransition(existing.status(), newStatus);

        AppointmentEntity updated = new AppointmentEntity(
                existing.id(),
                existing.customerName(),
                existing.customerEmail(),
                existing.customerPhone(),
                existing.vehicleBrand(),
                existing.vehicleModel(),
                existing.vehicleRegistrationNumber(),
                existing.serviceType(),
                existing.appointmentAt(),
                newStatus,
                existing.notes());

        return toResponse(appointmentRepository.save(updated));
    }

    public AppointmentResponse updateNotes(String id, String notes) throws EntityNotFoundException {
        AppointmentEntity existing = getEntityById(id);
        AppointmentEntity updated = new AppointmentEntity(
                existing.id(),
                existing.customerName(),
                existing.customerEmail(),
                existing.customerPhone(),
                existing.vehicleBrand(),
                existing.vehicleModel(),
                existing.vehicleRegistrationNumber(),
                existing.serviceType(),
                existing.appointmentAt(),
                existing.status(),
                notes);

        return toResponse(appointmentRepository.save(updated));
    }

    public void deleteAppointment(String id) throws EntityNotFoundException {
        if (!appointmentRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }
        appointmentRepository.deleteById(id);
    }

    private AppointmentEntity getEntityById(String id) throws EntityNotFoundException {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    private void validateAppointmentSlot(LocalDateTime appointmentAt) {
        LocalTime appointmentTime = appointmentAt.toLocalTime();
        if (appointmentTime.isBefore(OPENING_TIME) || appointmentTime.isAfter(CLOSING_TIME)) {
            throw new IllegalArgumentException("Appointments can only be scheduled between 08:00 and 18:00");
        }
    }

    private void validateDuplicateCustomerAppointment(String customerEmail, LocalDateTime appointmentAt) {
        if (appointmentRepository.existsByCustomerEmailIgnoreCaseAndAppointmentAt(customerEmail, appointmentAt)) {
            throw new IllegalArgumentException("The customer already has an appointment at this date and time");
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            throw new IllegalArgumentException("Appointment already has status " + newStatus);
        }

        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            throw new IllegalArgumentException("Cannot change status for a " + currentStatus.toLowerCase() + " appointment");
        }

        if ("COMPLETED".equals(newStatus) && !"CONFIRMED".equals(currentStatus)) {
            throw new IllegalArgumentException("Only confirmed appointments can be marked as completed");
        }
    }

    private AppointmentResponse toResponse(AppointmentEntity appointment) {
        return new AppointmentResponse(
                appointment.id(),
                appointment.customerName(),
                appointment.customerEmail(),
                appointment.customerPhone(),
                appointment.vehicleBrand(),
                appointment.vehicleModel(),
                appointment.vehicleRegistrationNumber(),
                appointment.serviceType(),
                appointment.appointmentAt(),
                appointment.status(),
                appointment.notes());
    }
}
