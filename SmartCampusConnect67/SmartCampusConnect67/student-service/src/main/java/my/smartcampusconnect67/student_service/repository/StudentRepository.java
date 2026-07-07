package my.smartcampusconnect67.student_service.repository;

import my.smartcampusconnect67.student_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//the purpose of this zis stated in the readme plsplspls.
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentMatricNo(String studentMatricNo);
}