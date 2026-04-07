package ro.unibuc.prodeng.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ro.unibuc.prodeng.model.AppointmentEntity;

@Repository
public interface AppointmentRepository extends MongoRepository<AppointmentEntity, String> {

    List<AppointmentEntity> findAllByOrderByAppointmentAtAsc();

    List<AppointmentEntity> findByCustomerEmailIgnoreCaseOrderByAppointmentAtAsc(String customerEmail);

    List<AppointmentEntity> findByAppointmentAtBetweenOrderByAppointmentAtAsc(
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByCustomerEmailIgnoreCaseAndAppointmentAt(String customerEmail, LocalDateTime appointmentAt);
}
