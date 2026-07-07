package my.smartcampusconnect67.library_service.soap;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "FaultDetail")
@XmlType(name = "FaultDetail")
public class FaultDetail {
    private String errorMessage;

    public FaultDetail() {}
    public FaultDetail(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
