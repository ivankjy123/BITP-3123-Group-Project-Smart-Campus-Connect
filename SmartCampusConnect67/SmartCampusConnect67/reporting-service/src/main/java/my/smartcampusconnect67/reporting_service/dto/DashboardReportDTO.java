package my.smartcampusconnect67.reporting_service.dto;

public class DashboardReportDTO {

    private int totalStudents;
    private int totalEnrolments;
    private int totalCourses;
    private int totalBooksBorrowed;
    private int totalActiveStudents;

    public DashboardReportDTO() {
    }

    public DashboardReportDTO(int totalStudents,
                              int totalEnrolments,
                              int totalCourses,
                              int totalBooksBorrowed,
                              int totalActiveStudents) {
        this.totalStudents = totalStudents;
        this.totalEnrolments = totalEnrolments;
        this.totalCourses = totalCourses;
        this.totalBooksBorrowed = totalBooksBorrowed;
        this.totalActiveStudents = totalActiveStudents;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalEnrolments() {
        return totalEnrolments;
    }

    public void setTotalEnrolments(int totalEnrolments) {
        this.totalEnrolments = totalEnrolments;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        this.totalCourses = totalCourses;
    }

    public int getTotalBooksBorrowed() {
        return totalBooksBorrowed;
    }

    public void setTotalBooksBorrowed(int totalBooksBorrowed) {
        this.totalBooksBorrowed = totalBooksBorrowed;
    }

    public int getTotalActiveStudents() {
        return totalActiveStudents;
    }

    public void setTotalActiveStudents(int totalActiveStudents) {
        this.totalActiveStudents = totalActiveStudents;
    }
}