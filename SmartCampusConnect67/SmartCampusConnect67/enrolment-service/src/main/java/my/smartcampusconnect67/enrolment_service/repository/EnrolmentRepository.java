package my.smartcampusconnect67.enrolment_service.repository;

import my.smartcampusconnect67.enrolment_service.model.Enrolment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {
    List<Enrolment> findByStudentMatricNo(String studentMatricNo);
}
