package ro.unibuc.prodeng.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateAppointmentRequest;
import ro.unibuc.prodeng.request.UpdateAppointmentNotesRequest;
import ro.unibuc.prodeng.request.UpdateAppointmentStatusRequest;
import ro.unibuc.prodeng.response.AppointmentResponse;
import ro.unibuc.prodeng.service.AppointmentService;

@RestController
@Validated
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable String id)
            throws EntityNotFoundException {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByCustomerEmail(
            @RequestParam @Email(message = "Invalid email format") String email) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByCustomerEmail(email));
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDay(date));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request) throws EntityNotFoundException {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request.status()));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<AppointmentResponse> updateNotes(
            @PathVariable String id,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) throws EntityNotFoundException {
        return ResponseEntity.ok(appointmentService.updateNotes(id, request.notes()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) throws EntityNotFoundException {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
