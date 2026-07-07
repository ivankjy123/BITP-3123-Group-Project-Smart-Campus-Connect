package my.smartcampusconnect67.enrolment_service.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "enrolments")
public class Enrolment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "student_matric_no", unique = true, nullable = false)
    private String studentMatricNo;

    @Column(name = "course_code", unique = true, nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private String semester;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ENROLLED'")
    private String status;

    @Column(name = "enrolment_date", insertable = false, updatable = false)
    private Timestamp enrolmentDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStudentMatricNo() {
        return studentMatricNo;
    }

    public void setStudentMatricNo(String studentMatricNo) {
        this.studentMatricNo = studentMatricNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(Timestamp enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
    }
}
