package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientOrderByAppointmentDateTimeDesc(User patient);

    List<Appointment> findByDoctorOrderByAppointmentDateTimeDesc(User doctor);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.status = :status " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByDoctorAndStatus(@Param("doctor") User doctor, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND a.status = :status " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByPatientAndStatus(@Param("patient") User patient, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDateTime")
    List<Appointment> findAppointmentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") AppointmentStatus status);
}