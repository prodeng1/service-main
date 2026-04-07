package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.AppointmentEntity;
import ro.unibuc.prodeng.repository.AppointmentRepository;
import ro.unibuc.prodeng.request.CreateAppointmentRequest;
import ro.unibuc.prodeng.response.AppointmentResponse;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private final LocalDateTime appointmentAt = LocalDateTime.of(2026, 4, 15, 10, 0);

    @Test
    void testCreateAppointment_returnsSavedAppointment() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Test drive",
                appointmentAt,
                "Client interested in automatic transmission");

        when(appointmentRepository.existsByCustomerEmailIgnoreCaseAndAppointmentAt("ana@example.com", appointmentAt))
                .thenReturn(false);
        when(appointmentRepository.save(any(AppointmentEntity.class)))
                .thenAnswer(invocation -> {
                    AppointmentEntity appointment = invocation.getArgument(0);
                    return new AppointmentEntity(
                            "appt-1",
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
                });

        AppointmentResponse result = appointmentService.createAppointment(request);

        assertEquals("appt-1", result.id());
        assertEquals("REQUESTED", result.status());
        assertEquals("Dacia", result.vehicleBrand());
    }

    @Test
    void testCreateAppointment_whenOutsideWorkingHours_throwsException() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Service",
                LocalDateTime.of(2026, 4, 15, 19, 0),
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(request));

        assertEquals("Appointments can only be scheduled between 08:00 and 18:00", exception.getMessage());
    }

    @Test
    void testCreateAppointment_whenDuplicateExists_throwsException() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Service",
                appointmentAt,
                null);

        when(appointmentRepository.existsByCustomerEmailIgnoreCaseAndAppointmentAt("ana@example.com", appointmentAt))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(request));

        assertEquals("The customer already has an appointment at this date and time", exception.getMessage());
    }

    @Test
    void testUpdateStatus_whenTransitionIsValid_returnsUpdatedAppointment() throws EntityNotFoundException {
        AppointmentEntity existing = new AppointmentEntity(
                "appt-1",
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Test drive",
                appointmentAt,
                "REQUESTED",
                "Initial notes");

        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any(AppointmentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse result = appointmentService.updateStatus("appt-1", "CONFIRMED");

        assertEquals("CONFIRMED", result.status());
    }

    @Test
    void testUpdateStatus_whenCompletingWithoutConfirmation_throwsException() {
        AppointmentEntity existing = new AppointmentEntity(
                "appt-1",
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Test drive",
                appointmentAt,
                "REQUESTED",
                "Initial notes");

        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.updateStatus("appt-1", "COMPLETED"));

        assertEquals("Only confirmed appointments can be marked as completed", exception.getMessage());
    }

    @Test
    void testGetAppointmentsForDay_returnsSortedAppointments() {
        when(appointmentRepository.findByAppointmentAtBetweenOrderByAppointmentAtAsc(
                LocalDate.of(2026, 4, 15).atStartOfDay(),
                LocalDate.of(2026, 4, 16).atStartOfDay().minusNanos(1)))
                .thenReturn(List.of(
                        new AppointmentEntity(
                                "appt-1",
                                "Ana Popescu",
                                "ana@example.com",
                                "+40712345678",
                                "Dacia",
                                "Duster",
                                "B-123-XYZ",
                                "Test drive",
                                appointmentAt,
                                "REQUESTED",
                                null)));

        List<AppointmentResponse> result = appointmentService.getAppointmentsForDay(LocalDate.of(2026, 4, 15));

        assertEquals(1, result.size());
        assertEquals("appt-1", result.get(0).id());
    }

    @Test
    void testDeleteAppointment_delegatesToRepository() throws EntityNotFoundException {
        when(appointmentRepository.existsById("appt-1")).thenReturn(true);

        appointmentService.deleteAppointment("appt-1");

        verify(appointmentRepository, times(1)).deleteById("appt-1");
    }
}
