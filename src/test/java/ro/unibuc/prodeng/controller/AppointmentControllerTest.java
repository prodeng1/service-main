package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasKey;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.CreateAppointmentRequest;
import ro.unibuc.prodeng.request.UpdateAppointmentNotesRequest;
import ro.unibuc.prodeng.request.UpdateAppointmentStatusRequest;
import ro.unibuc.prodeng.response.AppointmentResponse;
import ro.unibuc.prodeng.service.AppointmentService;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final AppointmentResponse appointment = new AppointmentResponse(
            "appt-1",
            "Ana Popescu",
            "ana@example.com",
            "+40712345678",
            "Dacia",
            "Duster",
            "B-123-XYZ",
            "Test drive",
            LocalDateTime.of(2026, 4, 15, 10, 0),
            "REQUESTED",
            "Needs financing details");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllAppointments_returnsAppointments() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerEmail", is("ana@example.com")));
    }

    @Test
    void testCreateAppointment_whenPayloadIsValid_returnsCreatedAppointment() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Ana Popescu",
                "ana@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Test drive",
                LocalDateTime.of(2026, 4, 15, 10, 0),
                "Needs financing details");

        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(appointment);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("appt-1")))
                .andExpect(jsonPath("$.status", is("REQUESTED")));
    }

    @Test
    void testCreateAppointment_whenPayloadIsInvalid_returnsBadRequest() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "",
                "invalid",
                "123",
                "",
                "",
                "",
                "",
                null,
                null);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasKey("customerName")))
                .andExpect(jsonPath("$", hasKey("customerEmail")))
                .andExpect(jsonPath("$", hasKey("appointmentAt")));
    }

    @Test
    void testGetAppointmentsByCustomerEmail_returnsAppointments() throws Exception {
        when(appointmentService.getAppointmentsByCustomerEmail("ana@example.com")).thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointments/by-email").param("email", "ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleBrand", is("Dacia")));
    }

    @Test
    void testUpdateStatus_returnsUpdatedAppointment() throws Exception {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest("CONFIRMED");
        AppointmentResponse confirmed = new AppointmentResponse(
                appointment.id(),
                appointment.customerName(),
                appointment.customerEmail(),
                appointment.customerPhone(),
                appointment.vehicleBrand(),
                appointment.vehicleModel(),
                appointment.vehicleRegistrationNumber(),
                appointment.serviceType(),
                appointment.appointmentAt(),
                "CONFIRMED",
                appointment.notes());

        when(appointmentService.updateStatus("appt-1", "CONFIRMED")).thenReturn(confirmed);

        mockMvc.perform(patch("/api/appointments/appt-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void testUpdateNotes_returnsUpdatedAppointment() throws Exception {
        UpdateAppointmentNotesRequest request = new UpdateAppointmentNotesRequest("Client asked for evening reminder");
        AppointmentResponse updated = new AppointmentResponse(
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
                "Client asked for evening reminder");

        when(appointmentService.updateNotes("appt-1", "Client asked for evening reminder")).thenReturn(updated);

        mockMvc.perform(patch("/api/appointments/appt-1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes", is("Client asked for evening reminder")));
    }

    @Test
    void testDeleteAppointment_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/appointments/appt-1"))
                .andExpect(status().isNoContent());

        verify(appointmentService, times(1)).deleteAppointment("appt-1");
    }

    @Test
    void testGetAppointmentById_whenMissing_returnsNotFound() throws Exception {
        when(appointmentService.getAppointmentById("missing")).thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(get("/api/appointments/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Entity: missing was not found")));
    }
}
