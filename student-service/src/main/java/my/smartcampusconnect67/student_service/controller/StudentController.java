package my.smartcampusconnect67.student_service.controller;

import my.smartcampusconnect67.student_service.client.NotificationTcpClient;
import my.smartcampusconnect67.student_service.dto.NotificationMessage;
import my.smartcampusconnect67.student_service.error.ErrorResponse;
import my.smartcampusconnect67.student_service.repository.StudentRepository;
import my.smartcampusconnect67.student_service.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
//we need this because we are using frontend app to test, bypasses the security
//of making http request on different port
@CrossOrigin(origins = "*")
public class StudentController {
    //autowired injects the dependencies due to the StudentRepository is an interface class.
    //we cannot create instance in an interface class.
    @Autowired
    private StudentRepository repo;

    @Autowired
    private NotificationTcpClient notificationClient;

    //get all student
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Student> allStudents = repo.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(allStudents);
    }

    //get one student (studentMatricNo)
    @GetMapping("/{matricNo}")
    public ResponseEntity<?> getByMatricNo(@PathVariable String matricNo) {
        Optional<Student> student = repo.findByStudentMatricNo(matricNo);
        if (student.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Not Found",
                            HttpStatus.NOT_FOUND.value(),
                            "Student: " + matricNo + " not found.",
                            "/api/students/" + matricNo
                    ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(student);
    }

    //post new student {email,fullName,programme,studentMatricNo}
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Student s) {
        try {
            //check existing student with the input matric no
            Optional<Student> existStudent = repo.findByStudentMatricNo(s.getStudentMatricNo());
            if (existStudent.isPresent()) {
                //return conflict
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                "Conflict",
                                HttpStatus.CONFLICT.value(),
                                "Failed to register student. Student with Matric No: " + s.getStudentMatricNo() + " existed.",
                                "/api/students"
                        ));
            }

            //set default status
            if (s.getStatus() == null || s.getStatus().isEmpty()) {
                s.setStatus("ACTIVE");
            }
            Student savedStudent = repo.save(s);

            // Send Message
            NotificationMessage msg = new NotificationMessage(
                    "STUDENT_CREATE_SUCCESS",
                    savedStudent.getEmail(),
                    "Your student profile has been created successfully.",
                    System.currentTimeMillis()
            );
            try {
                notificationClient.send(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal server processing error: post new student failed.",
                            "/api/students"
                    ));
        }
    }

    //for PUT and DELETE, we use optional as a container. It can prevent the app crashes.
    //if we search a student by matric no that doesnt exist, it returns null. So if we interact with the null, the app crashes.
    //using Optional, we can safely 'peek' inside the database, ensure it dont interact or return null.

    //put - update student details (matricNo) {#fullName,#email,#programme,#status}
    @PutMapping("/{matricNo}")
    public ResponseEntity<?> update(@PathVariable String matricNo, @RequestBody Student details) {
        try {
            Optional<Student> existingStudent = repo.findByStudentMatricNo(matricNo);
            // if that particular field exists, update the field based on the input body.
            if (existingStudent.isPresent()) {
                Student s = existingStudent.get();
                if (details.getFullName() != null) {
                    s.setFullName(details.getFullName());
                }
                if (details.getEmail() != null) {
                    s.setEmail(details.getEmail());
                }
                if (details.getProgramme() != null) {
                    s.setProgramme(details.getProgramme());
                }
                if (details.getStatus() != null) {
                    s.setStatus(details.getStatus());
                }
                repo.save(s);

                // Send Message
                NotificationMessage msg = new NotificationMessage(
                        "STUDENT_UPDATE_SUCCESS",
                        s.getEmail(),
                        "Your student profile has been updated successfully.",
                        System.currentTimeMillis()
                );
                try {
                    notificationClient.send(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(s);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Student: " + matricNo + " not found.",
                                "/api/students/" + matricNo
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred while updating student.",
                            "/api/students/" + matricNo
                    ));
        }
    }

    //same thing as above (Optional)

    //delete student (matricNo)
    @DeleteMapping("/{matricNo}")
    public ResponseEntity<?> delete(@PathVariable String matricNo) {
        try {
            Optional<Student> existingStudent = repo.findByStudentMatricNo(matricNo);
            //check student exist onot
            if (existingStudent.isPresent()) {
                // Send Message
                NotificationMessage msg = new NotificationMessage(
                        "STUDENT_DELETE_SUCCESS",
                        existingStudent.get().getEmail(),
                        "Your student profile has been deleted.",
                        System.currentTimeMillis()
                );
                try {
                    notificationClient.send(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                repo.delete(existingStudent.get());
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ErrorResponse(
                                "No Content",
                                HttpStatus.NO_CONTENT.value(),
                                "Student: " + matricNo + " deleted successfully.",
                                "/api/students/" + matricNo
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Student: " + matricNo + " not found.",
                                "/api/students/" + matricNo
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred while deleting student.",
                            "/api/students/" + matricNo
                    ));
        }
    }
}