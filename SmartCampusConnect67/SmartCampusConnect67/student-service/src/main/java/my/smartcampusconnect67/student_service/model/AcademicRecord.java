package my.smartcampusconnect67.student_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_records")
public class AcademicRecord {
    //typical object entity, all variables are the mirror of database.
    //same, no constructor
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_matric_no", nullable = false)
    private String studentMatricNo;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private String semester;

    @Column(name = "midterm_marks", columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double midtermMarks;

    @Column(name = "assessment_marks", columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double assessmentMarks;

    @Column(name = "lab_test_marks", columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double labTestMarks;

    @Column(name = "final_exam_marks", columnDefinition = "DOUBLE DEFAULT 0.0")
    private Double finalExamMarks;

    @Column(name = "total_marks")
    private Double totalMarks;

    @Column(length = 2)
    private String grade;

    @PrePersist
    @PreUpdate
    public void calcFinalScore() {
        this.totalMarks = this.midtermMarks + this.assessmentMarks + this.labTestMarks + this.finalExamMarks;
        if (this.totalMarks >= 80) this.grade = "A";
        else if (this.totalMarks >= 75) this.grade = "A-";
        else if (this.totalMarks >= 70) this.grade = "B+";
        else if (this.totalMarks >= 65) this.grade = "B";
        else if (this.totalMarks >= 60) this.grade = "B-";
        else if (this.totalMarks >= 55) this.grade = "C+";
        else if (this.totalMarks >= 50) this.grade = "C";
        else if (this.totalMarks >= 47) this.grade = "C-";
        else if (this.totalMarks >= 44) this.grade = "D+";
        else if (this.totalMarks >= 40) this.grade = "D";
        else this.grade = "E";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Double getMidtermMarks() {
        return midtermMarks;
    }

    public void setMidtermMarks(Double midtermMarks) {
        this.midtermMarks = midtermMarks;
    }

    public Double getAssessmentMarks() {
        return assessmentMarks;
    }

    public void setAssessmentMarks(Double assessmentMarks) {
        this.assessmentMarks = assessmentMarks;
    }

    public Double getLabTestMarks() {
        return labTestMarks;
    }

    public void setLabTestMarks(Double labTestMarks) {
        this.labTestMarks = labTestMarks;
    }

    public Double getFinalExamMarks() {
        return finalExamMarks;
    }

    public void setFinalExamMarks(Double finalExamMarks) {
        this.finalExamMarks = finalExamMarks;
    }

    public Double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
