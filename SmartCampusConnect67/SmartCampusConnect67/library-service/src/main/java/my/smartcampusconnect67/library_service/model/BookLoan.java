package my.smartcampusconnect67.library_service.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "book_loans")
public class BookLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_matric_no", nullable = false, length = 20)
    private String studentMatricNo;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(length = 20)
    private String status = "BORROWED"; // BORROWED, RETURNED, OVERDUE

    // Constructors
    public BookLoan() {}

    public BookLoan(String studentMatricNo, String isbn, LocalDate loanDate, LocalDate dueDate, String status) {
        this.studentMatricNo = studentMatricNo;
        this.isbn = isbn;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentMatricNo() { return studentMatricNo; }
    public void setStudentMatricNo(String studentMatricNo) { this.studentMatricNo = studentMatricNo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}