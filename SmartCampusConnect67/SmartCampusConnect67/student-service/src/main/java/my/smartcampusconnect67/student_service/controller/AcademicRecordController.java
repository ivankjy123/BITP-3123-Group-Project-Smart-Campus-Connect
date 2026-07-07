package my.smartcampusconnect67.student_service.controller;

import my.smartcampusconnect67.student_service.client.NotificationTcpClient;
import my.smartcampusconnect67.student_service.dto.NotificationMessage;
import my.smartcampusconnect67.student_service.error.ErrorResponse;
import my.smartcampusconnect67.student_service.repository.AcademicRecordRepository;
import my.smartcampusconnect67.student_service.repository.StudentRepository;
import my.smartcampusconnect67.student_service.model.AcademicRecord;
import my.smartcampusconnect67.student_service.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/academic-records")
@CrossOrigin(origins = "*")
public class AcademicRecordController {
    private final ObjectMapper objectMapper;
    private AcademicRecordRepository recordRepo;
    private StudentRepository studentRepo;

    @Autowired
    private NotificationTcpClient notificationClient;

    private final String ENROLMENT_SERVICE_URL = "http://localhost:8082/api/enrolments";

    public AcademicRecordController(AcademicRecordRepository recordRepo, StudentRepository studentRepo, ObjectMapper objectMapper) {
        this.recordRepo = recordRepo;
        this.studentRepo = studentRepo;
        this.objectMapper = objectMapper;
    }

    //get all academic record by one student (/students/matricNo)
    @GetMapping("/students/{matricNo}")
    public ResponseEntity<?> getRecordsByStudent(@PathVariable String matricNo) {
        Optional<Student> existingStudent = studentRepo.findByStudentMatricNo(matricNo);
        if (existingStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Not Found",
                            HttpStatus.NOT_FOUND.value(),
                            "Student with Matric No: " + matricNo + " not found in database.",
                            "/api/academic-records/students/" + matricNo
                    ));
        }
        List<AcademicRecord> records = recordRepo.findByStudentMatricNo(matricNo);
        if (records.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ErrorResponse(
                            "No Content",
                            HttpStatus.NO_CONTENT.value(),
                            "Student " + matricNo + " have no academic records.",
                            "/api/academic-records/students/" + matricNo
                    ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(records);
    }

    //TODO: check duplicate academic record (same student cannot have two same academic record for one course in same semester)
    //post - create academic record {assessmentMarks,courseCode,finalExamMarks,labTestMarks,midtermMarks,studentMatricNo}
    @PostMapping
    public ResponseEntity<?> createAcademicRecords(@RequestBody AcademicRecord inputRecord) throws InterruptedException {
        //check student existent
        Optional<Student> existingStudent = studentRepo.findByStudentMatricNo(inputRecord.getStudentMatricNo());
        if (existingStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Not Found",
                            HttpStatus.NOT_FOUND.value(),
                            "Student: " + inputRecord.getStudentMatricNo() + " not found.",
                            "/api/academic-records"
                    ));
        }
        try {
            //get the academic record from enrolment service
            HttpClient cl = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ENROLMENT_SERVICE_URL + "/students/" + inputRecord.getStudentMatricNo()))
                    .GET().build();
            HttpResponse<String> resp = cl.send(req, HttpResponse.BodyHandlers.ofString());
            //check if student has enrolment record
            if (resp.statusCode() != 200) {
                return ResponseEntity.status(resp.statusCode())
                        .body(new ErrorResponse(
                                "No Content",
                                resp.statusCode(),
                                "No enrolment record found for student: " + inputRecord.getStudentMatricNo() + ".",
                                "/api/academic-records"
                        ));
            }
            //parse the enrolment record
            JsonNode enrolments = objectMapper.readTree(resp.body());
            boolean isValidEnrolment = false;
            String foundSemester = "";
            if (enrolments.isArray() && !enrolments.isEmpty()) {
                for (JsonNode enrolment : enrolments) {
                    //check the enrollment status
                    String courseCode = enrolment.get("courseCode").asString();
                    String status = enrolment.get("status").asString();
                    if (courseCode.equalsIgnoreCase(inputRecord.getCourseCode()) && "ENROLLED".equalsIgnoreCase(status)) {
                        isValidEnrolment = true;
                        foundSemester = enrolment.get("semester").asString();
                        break;
                    }
                }
            }
            if (!isValidEnrolment) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Failed to create Academic Record. The enrolment for this course is invalid.",
                                "/api/academic-records"
                        ));
            }
            inputRecord.setSemester(foundSemester);
            Optional<AcademicRecord> duplicateRecord =
                    recordRepo.findByStudentMatricNoAndCourseCodeAndSemester(
                            inputRecord.getStudentMatricNo(),
                            inputRecord.getCourseCode(),
                            foundSemester);
            if (duplicateRecord.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                "Conflict",
                                HttpStatus.CONFLICT.value(),
                                "Duplicate Record: Student already has an academic record for course "
                                        + inputRecord.getCourseCode() + " in semester " + inputRecord.getSemester() + ".",
                                "/api/academic-records"
                        ));
            }
            AcademicRecord savedRecord = recordRepo.save(inputRecord);
            // Send Message
            NotificationMessage msg = new NotificationMessage(
                    "ACADEMIC_RECORD_CREATE_SUCCESS",
                    existingStudent.get().getEmail(),
                    "Your academic record for " + savedRecord.getCourseCode() + " (" + savedRecord.getSemester() + ") has been created.",
                    System.currentTimeMillis()
            );
            try {
                notificationClient.send(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedRecord);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred when creating academic record.",
                            "/api/academic-records"
                    ));
        }
    }

    //put - update academic record (/students/{matricNo}/course/{courseCode}) {#assessmentMarks,#finalExamMarks,#labTestMarks,#midtermMarks}
    @PutMapping("/students/{matricNo}/course/{courseCode}")
    public ResponseEntity<?> updateAcademicRecords(@RequestBody AcademicRecord inputRecord, @PathVariable String matricNo, @PathVariable String courseCode) {
        try {
            if (studentRepo.findByStudentMatricNo(matricNo).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Student: " + matricNo + " does not exist.",
                                "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                        ));
            }
            Optional<AcademicRecord> optionalAcademicRecord = recordRepo.findByStudentMatricNoAndCourseCode(matricNo, courseCode);
            if (optionalAcademicRecord.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Academic record does not exist.",
                                "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                        ));
            }
            AcademicRecord existingRecord = optionalAcademicRecord.get();
            if (inputRecord.getMidtermMarks() != null) {
                existingRecord.setMidtermMarks(inputRecord.getMidtermMarks());
            }
            if (inputRecord.getAssessmentMarks() != null) {
                existingRecord.setAssessmentMarks(inputRecord.getAssessmentMarks());
            }
            if (inputRecord.getLabTestMarks() != null) {
                existingRecord.setLabTestMarks(inputRecord.getLabTestMarks());
            }
            if (inputRecord.getFinalExamMarks() != null) {
                existingRecord.setFinalExamMarks(inputRecord.getFinalExamMarks());
            }
            existingRecord.calcFinalScore();
            AcademicRecord updatedRecord = recordRepo.save(existingRecord);

            // Send Message
            Optional<Student> existingStudent = studentRepo.findByStudentMatricNo(matricNo);
            if (existingStudent.isPresent()) {
                NotificationMessage msg = new NotificationMessage(
                        "ACADEMIC_RECORD_UPDATE_SUCCESS",
                        existingStudent.get().getEmail(),
                        "Your academic record for " + courseCode + " has been updated.",
                        System.currentTimeMillis()
                );
                try {
                    notificationClient.send(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(updatedRecord);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred while updating academic record.",
                            "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                    ));
        }
    }

    //delete academic record (/students/{matricNo}/course/{courseCode})
    @DeleteMapping("/students/{matricNo}/course/{courseCode}")
    public ResponseEntity<?> deleteAcademicRecord(@PathVariable String matricNo, @PathVariable String courseCode) {
        try {
            Optional<Student> existingStudent = studentRepo.findByStudentMatricNo(matricNo);
            if (existingStudent.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Student with matric no: " + matricNo + " not found.",
                                "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                        ));
            }
            Optional<AcademicRecord> recordOptional = recordRepo.findByStudentMatricNoAndCourseCode(matricNo, courseCode);
            if (recordOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Academic Record for student: " + matricNo + " in course: " + courseCode + " not found.",
                                "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                        ));
            }
            AcademicRecord recordToDelete = recordOptional.get();
            String studentEmail = existingStudent.get().getEmail();

            recordRepo.delete(recordToDelete);

            // Send notification
            NotificationMessage msg = new NotificationMessage(
                    "ACADEMIC_RECORD_DELETE_SUCCESS",
                    studentEmail,
                    "Your academic record for " + courseCode + " has been removed.",
                    System.currentTimeMillis()
            );
            try {
                notificationClient.send(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ErrorResponse(
                            "No Content",
                            HttpStatus.NO_CONTENT.value(),
                            "Academic Record deleted successfully.",
                            "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred when deleting academic records.",
                            "/api/academic-records/students/" + matricNo + "/course/" + courseCode
                    ));
        }
    }
}