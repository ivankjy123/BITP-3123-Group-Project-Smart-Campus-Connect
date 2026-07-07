package my.smartcampusconnect67.enrolment_service.repository;

import my.smartcampusconnect67.enrolment_service.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
}
