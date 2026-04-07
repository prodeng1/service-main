package ro.unibuc.prodeng.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.AppointmentRepository;
import ro.unibuc.prodeng.request.CreateAppointmentRequest;

@DisplayName("AppointmentController Integration Tests")
class AppointmentControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void cleanUp() {
        appointmentRepository.deleteAll();
    }

    @Test
    void testCreateAndQueryAppointments_persistsAndFiltersSuccessfully() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Ana Popescu",
                "ana.popescu@example.com",
                "+40712345678",
                "Dacia",
                "Duster",
                "B-123-XYZ",
                "Test drive",
                LocalDateTime.of(2027, 4, 15, 10, 0),
                "Client interesat de versiunea automata");

        String response = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("ana.popescu@example.com"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String appointmentId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/appointments/" + appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleBrand").value("Dacia"))
                .andExpect(jsonPath("$.serviceType").value("Test drive"));

        mockMvc.perform(get("/api/appointments/by-email").param("email", "ana.popescu@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(appointmentId));

        mockMvc.perform(get("/api/appointments/by-date").param("date", "2027-04-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Ana Popescu"));
    }

    @Test
    void testStatusFlow_requestedAppointmentCanBeConfirmedThenCompleted() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Mihai Ionescu",
                "mihai.ionescu@example.com",
                "+40722333444",
                "Renault",
                "Clio",
                "B-456-ABC",
                "Revizie",
                LocalDateTime.of(2027, 4, 16, 11, 30),
                "Masina pentru revizia de 30.000 km");

        String response = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String appointmentId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testCreateAppointment_duplicateCustomerAndTime_returnsBadRequest() throws Exception {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Elena Marin",
                "elena.marin@example.com",
                "+40798765432",
                "Skoda",
                "Octavia",
                "IF-99-AUTO",
                "Consultanta vanzare",
                LocalDateTime.of(2027, 4, 17, 14, 0),
                "Interes pentru finantare leasing");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The customer already has an appointment at this date and time"));
    }
}
