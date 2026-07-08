package my.smartcampusconnect67.student_service.repository;

import my.smartcampusconnect67.student_service.model.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {
    List<AcademicRecord> findByStudentMatricNo(String studentMatricNo);
    //for PUT mapping
    Optional<AcademicRecord> findByStudentMatricNoAndCourseCode(String studentMatricNo, String courseCode);
    //check no duplicate
    Optional<AcademicRecord> findByStudentMatricNoAndCourseCodeAndSemester(String studentMatricNo, String courseCode, String semester);
}
