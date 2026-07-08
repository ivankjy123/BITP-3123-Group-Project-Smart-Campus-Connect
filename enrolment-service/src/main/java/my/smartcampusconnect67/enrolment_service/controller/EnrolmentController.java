package my.smartcampusconnect67.enrolment_service.controller;

import my.smartcampusconnect67.enrolment_service.error.ErrorResponse;
import my.smartcampusconnect67.enrolment_service.client.NotificationTcpClient;
import my.smartcampusconnect67.enrolment_service.dto.NotificationMessage;
import my.smartcampusconnect67.enrolment_service.model.Course;
import my.smartcampusconnect67.enrolment_service.model.Enrolment;
import my.smartcampusconnect67.enrolment_service.repository.CourseRepository;
import my.smartcampusconnect67.enrolment_service.repository.EnrolmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/api/enrolments")
@CrossOrigin(origins = "*")
public class EnrolmentController {
    private final EnrolmentRepository enrolRepo;
    private final CourseRepository courseRepo;

    private final ReentrantLock lock = new ReentrantLock();
    private final ObjectMapper objectMapper;

    //use this whenever need data from other service
    private final String STUDENT_PROFILE_URL = "http://localhost:8081/api/students";
    private final String ACADEMIC_RECORD_URL = "http://localhost:8081/api/academic-records/students/";

    @Autowired
    private NotificationTcpClient notificationClient;

    public EnrolmentController(EnrolmentRepository enrolRepo, CourseRepository courseRepo, ObjectMapper objectMapper) {
        this.enrolRepo = enrolRepo;
        this.courseRepo = courseRepo;
        this.objectMapper = objectMapper;
    }

    //get enrolment by student (matricNo)
    @GetMapping("/students/{matricNo}")
    public ResponseEntity<?> getByStudent(@PathVariable String matricNo) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest studentRequest = HttpRequest.newBuilder()
                .uri(URI.create(STUDENT_PROFILE_URL + "/" + matricNo))
                .GET().build();
        HttpResponse<String> studentResponse = client.send(studentRequest, HttpResponse.BodyHandlers.ofString());
        //check the response code, if match any then show error
        if (studentResponse.statusCode() != 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Bad Request",
                            HttpStatus.BAD_REQUEST.value(),
                            "Student: " + matricNo + " not found.",
                            "/api/enrolments"
                    ));
        }
        List<Enrolment> enrolments = enrolRepo.findByStudentMatricNo(matricNo);
        if (!enrolments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(enrolments);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Not Found",
                        HttpStatus.NOT_FOUND.value(),
                        "Student: " + matricNo + " does not have enrolment data.",
                        "/api/enrolments/students/" + matricNo
                ));
    }

    //get all enrolments
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Enrolment> allEnrolments = enrolRepo.findAll();
        if (!allEnrolments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(allEnrolments);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ErrorResponse(
                        "No Content",
                        HttpStatus.NO_CONTENT.value(),
                        "No enrolments found.",
                        "/api/enrolments"
                ));
    }

    //post new enrolment {studentMatricNo, courseCode}
    @PostMapping
    public ResponseEntity<?> createEnrolment(@RequestBody Enrolment enrolment) {
        try (HttpClient client = HttpClient.newHttpClient()) { // client put here for resource management
            //check student existence
            HttpRequest studentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(STUDENT_PROFILE_URL + "/" + enrolment.getStudentMatricNo()))
                    .GET().build();
            HttpResponse<String> studentResponse = client.send(studentRequest, HttpResponse.BodyHandlers.ofString());
            //check the response code, if match any then show error
            if (studentResponse.statusCode() == 404) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Student: " + enrolment.getStudentMatricNo() + " not found.",
                                "/api/enrolments"
                        ));
            } else if (studentResponse.statusCode() != 200 && studentResponse.statusCode() != 204) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse(
                                "Internal Server Error",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error communicating with the Academic Record Service",
                                "/api/enrolments"
                        ));
            }
            //get all of the enrolments record for that student
            List<Enrolment> existingRecords = enrolRepo.findByStudentMatricNo(enrolment.getStudentMatricNo());
            if (existingRecords != null && !existingRecords.isEmpty()) {
                for (Enrolment record : existingRecords) {
                    if (record.getCourseCode() != null && record.getCourseCode().equalsIgnoreCase(enrolment.getCourseCode())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse(
                                        "Conflict",
                                        HttpStatus.CONFLICT.value(),
                                        "Duplicated: Student already enrolled in this course upcoming semester.",
                                        "/api/enrolments"
                                ));
                    }
                }
            }
            String targetSemester = "";
            String currentActiveSemester = calculateCurrentSemester(LocalDate.now());
            targetSemester = semesterIncrement(currentActiveSemester);
            //lock here, prevent concurrent capacity bypass
            lock.lock();
            try {
                //check whether the course got space (Capacity)
                Optional<Course> courseOptional = courseRepo.findByCourseCode(enrolment.getCourseCode());
                //if empty course not found
                if (courseOptional.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse(
                                    "Not Found",
                                    HttpStatus.NOT_FOUND.value(),
                                    "Course " + enrolment.getCourseCode() + " Not Found",
                                    "/api/enrolments"
                            ));
                }
                //map to course
                Course course = courseOptional.get();
                //check capacity, if fulled badrequest, also check race condition
                if (course.getCurrentEnrolled() >= course.getMaxCapacity()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(
                                    "Bad Request",
                                    HttpStatus.BAD_REQUEST.value(),
                                    "Course is fulled. Unable to process enrolment.",
                                    "/api/enrolments"
                            ));
                }
                //save enrolment, change enrolment table [status]
                if (enrolment.getStatus() == null) {
                    enrolment.setStatus("ENROLLED");        //defaule enrolled value
                }
                //set semester field and date into enrolment
                enrolment.setSemester(targetSemester);
                enrolment.setEnrolmentDate(Timestamp.valueOf(LocalDateTime.now()));
                Enrolment newEnrol = enrolRepo.save(enrolment);
                //course capacity ++
                course.setCurrentEnrolled(course.getCurrentEnrolled() + 1);
                //save course
                courseRepo.save(course);

                // Send async notification
                // obtain Enrolling Student's email
                studentRequest = HttpRequest.newBuilder()
                        .uri(URI.create(STUDENT_PROFILE_URL+ "/" + enrolment.getStudentMatricNo()))
                        .GET()
                        .build()
                ;
                studentResponse = client.send(
                        studentRequest,
                        HttpResponse.BodyHandlers.ofString()
                );
                if (studentResponse.statusCode() != 200) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ErrorResponse(
                                    "Internal Server Error",
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Cannot retrieve student profile.",
                                    "/api/enrolments"
                            ));
                }
                String studentEmail = objectMapper
                        .readTree(studentResponse.body())
                        .get("email")
                        .asString()
                ;

                // Send Message
                NotificationMessage msg = new NotificationMessage(
                        "ENROLMENT_SUCCESS",
                        studentEmail,
                        "Successfully enrolled in " + enrolment.getCourseCode(),
                        System.currentTimeMillis()
                );
                try {
                    notificationClient.send(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //return created
                return ResponseEntity.status(HttpStatus.CREATED).body(newEnrol);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred when creating enrolment.",
                            "/api/enrolments"
                    ));
        }
    }

    //delete enrolment (/students/matricNo/courses/courseCode)
    @DeleteMapping("/students/{matricNo}/courses/{courseCode}")
    public ResponseEntity<?> deleteEnrolment(@PathVariable String matricNo, @PathVariable String courseCode) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest recordRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ACADEMIC_RECORD_URL + matricNo))
                    .GET().build();
            HttpResponse<String> recordResponse = client.send(recordRequest, HttpResponse.BodyHandlers.ofString());
            if (recordResponse.statusCode() == 200 || recordResponse.statusCode() == 204) {
                JsonNode records = objectMapper.readTree(recordResponse.body());
                if (records.isArray() && !records.isEmpty()) {
                    for (JsonNode record : records) {
                        if (record.has("courseCode") && record.get("courseCode").asString().equalsIgnoreCase(courseCode)) {
                            return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(new ErrorResponse(
                                            "Conflict",
                                            HttpStatus.CONFLICT.value(),
                                            "Enrolled course has an active academic records. Deleting enrolment is prohibited.",
                                            "/api/enrolments/students/" + matricNo + "/courses/" + courseCode
                                    ));
                        }
                    }
                }
            } else if (recordResponse.statusCode() != 200 && recordResponse.statusCode() != 204 && recordResponse.statusCode() != 404) {
                //handle failure
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse(
                                "Internal Server Error",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error communicating with the Academic Record Service - DELETE",
                                "/api/enrolments/students/" + matricNo + "/courses/" + courseCode
                        ));
            }
            lock.lock();
            try {
                List<Enrolment> studentEnrolments = enrolRepo.findByStudentMatricNo(matricNo);
                Optional<Enrolment> targetEnrolment = studentEnrolments.stream()
                        .filter(enrolment -> enrolment.getCourseCode().equalsIgnoreCase(courseCode))
                        .findFirst();
                if (targetEnrolment.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse(
                                    "Not Found",
                                    HttpStatus.NOT_FOUND.value(),
                                    "Enrolment for course: " + courseCode + " not found for student:" + matricNo + ".",
                                    "/api/enrolments/students/" + matricNo + "/courses/" + courseCode
                            ));
                }
                Enrolment enrolmentToDelete = targetEnrolment.get();
                Course course = courseRepo.findByCourseCode(courseCode).get();
                course.setCurrentEnrolled(course.getCurrentEnrolled() - 1);
                courseRepo.save(course);
                enrolRepo.delete(enrolmentToDelete);
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ErrorResponse(
                                "No Content",
                                HttpStatus.NO_CONTENT.value(),
                                "Enrolment deleted successfully.",
                                "/api/enrolments/students/" + matricNo + "/courses/" + courseCode
                        ));
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to handle DELETE enrolment.",
                            "/api/enrolments/students/" + matricNo + "/courses/" + courseCode
                    ));
        }
    }

    //get all courses
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourse() {
        List<Course> allCourses = courseRepo.findAll();
        if (!allCourses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(allCourses);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ErrorResponse(
                        "No Content",
                        HttpStatus.NO_CONTENT.value(),
                        "No courses found.",
                        "/api/enrolments/courses"
                ));
    }

    @GetMapping("/courses/{courseCode}")
    public ResponseEntity<?> getCourseByCourseCode(@PathVariable String courseCode) {
        Optional<Course> existingCourse = courseRepo.findByCourseCode(courseCode);
        if (existingCourse.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(existingCourse);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ErrorResponse(
                        "No Content",
                        HttpStatus.NO_CONTENT.value(),
                        "Course with course code: " + courseCode + " not found.",
                        "/api/enrolments/courses/" + courseCode
                ));
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course courseDetail) {
        try {
            Optional<Course> existingCourse = courseRepo.findByCourseCode(courseDetail.getCourseCode());
            if (existingCourse.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(
                                "Conflict",
                                HttpStatus.CONFLICT.value(),
                                "Failed to add course. Course with course code: " + courseDetail.getCourseCode() + " existed.",
                                "/api/enrolments/courses"
                        ));
            }
            if (courseDetail.getMaxCapacity() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Max capacity cannot less than 1.",
                                "/api/enrolments/courses"
                        ));
            }
            if (courseDetail.getCurrentEnrolled() != null) {
                courseDetail.setCurrentEnrolled(0);
            }
            Course savedCourse = courseRepo.save(courseDetail);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Internal server processing error: create new course failed.",
                            "/api/enrolments/courses"
                    ));
        }
    }

    @PutMapping("/courses/{courseCode}")
    public ResponseEntity<?> updateCourse(@RequestBody Course updateDetail, @PathVariable String courseCode) {
        try {
            Optional<Course> existingCourse = courseRepo.findByCourseCode(courseCode);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Course code: " + courseCode + " not found.",
                                "/api/enrolments/courses/" + courseCode
                        ));
            }
            Course updatedCourse = existingCourse.get();
            if (updateDetail.getMaxCapacity() < existingCourse.get().getCurrentEnrolled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Max capacity cannot less than current enrolled amount.",
                                "/api/enrolments/course/" + courseCode
                        ));
            }
            if (updateDetail.getCurrentEnrolled() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Enrolled amount cannot be updated.",
                                "/api/enrolments/course/" + courseCode
                        ));
            }
            if (updateDetail.getCourseName() != null) {
                updatedCourse.setCourseName(updateDetail.getCourseName());
            }
            if (updateDetail.getMaxCapacity() != null) {
                updatedCourse.setMaxCapacity(updateDetail.getMaxCapacity());
            }
            Course savedCourse = courseRepo.save(updatedCourse);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(savedCourse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred while updating course.",
                            "/api/enrolments/courses/" + courseCode
                    ));
        }
    }

    @DeleteMapping("/courses/{courseCode}")
    public ResponseEntity<?> deleteCourse(@PathVariable String courseCode) {
        try {
            Optional<Course> existingCourse = courseRepo.findByCourseCode(courseCode);
            if (existingCourse.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Not Found",
                                HttpStatus.NOT_FOUND.value(),
                                "Course with course code: " + courseCode + " not found.",
                                "/api/enrolments/course/" + courseCode
                        ));
            }
            if (existingCourse.get().getCurrentEnrolled() != 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(
                                "Bad Request",
                                HttpStatus.BAD_REQUEST.value(),
                                "Course is still enrolled by student. Deleting is prohibited.",
                                "/api/enrolments/course/" + courseCode
                        ));
            }
            courseRepo.delete(existingCourse.get());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ErrorResponse(
                            "No Content",
                            HttpStatus.NO_CONTENT.value(),
                            "Course deleted successfully.",
                            "/api/enrolments/course/" + courseCode
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Internal Server Error",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error occurred while deleting course.",
                            "/api/enrolments/course/" + courseCode
                    ));
        }
    }
    //helper function to calculate semester and increase semester
    public String semesterIncrement(String currentSemester) {
        String nextSemester = "";
        Pattern pattern = Pattern.compile("^(\\d{4})/(\\d{4})-(\\d)$");
        Matcher matcher = pattern.matcher(currentSemester);
        if (matcher.matches()) {
            int startYear = Integer.parseInt(matcher.group(1));
            int endYear = Integer.parseInt(matcher.group(2));
            int semester = Integer.parseInt(matcher.group(3));
            if (semester == 1) {
                semester = 2;
            } else if (semester == 2) {
                startYear++;
                endYear++;
                semester = 1;
            }
            nextSemester = String.format("%d/%d-%d", startYear, endYear, semester);
        }
        return nextSemester;
    }

    public String calculateCurrentSemester(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        // Semester 2: March 10 to Sept 30
        if ((month > 3 || (month == 3 && day >= 10)) && month <= 9) {
            return (year - 1) + "/" + year + "-2";
        }
        // Semester 1: Oct 1 to Dec 31
        else if (month >= 10) {
            return year + "/" + (year + 1) + "-1";
        }
        // Semester 1: Jan 1 to March 9 (belongs to the previous calendar year's intake)
        else {
            return (year - 1) + "/" + year + "-1";
        }
    }
}
