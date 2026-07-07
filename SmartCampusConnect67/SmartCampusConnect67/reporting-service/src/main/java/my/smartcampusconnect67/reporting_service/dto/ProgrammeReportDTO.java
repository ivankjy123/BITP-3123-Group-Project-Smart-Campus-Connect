package my.smartcampusconnect67.reporting_service.dto;

public class ProgrammeReportDTO {

    private String programme;
    private int totalStudents;

    public ProgrammeReportDTO() {
    }

    public ProgrammeReportDTO(String programme, int totalStudents) {
        this.programme = programme;
        this.totalStudents = totalStudents;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
}