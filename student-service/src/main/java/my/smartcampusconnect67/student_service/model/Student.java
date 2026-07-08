package my.smartcampusconnect67.student_service.model;

// this maps the json property to the java variables
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    //below are the variables. its like a mirror to the database itself
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //unique primary identifier, auto increment
    private long id;

    @Column(name = "student_matric_no", unique = true, nullable = false)
    private String studentMatricNo;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String programme;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")      //we use this when a field have default value
    private String status;

    //since we using Spring Data JPARepository, so we dont need a constructor.

    //setters and getters
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
