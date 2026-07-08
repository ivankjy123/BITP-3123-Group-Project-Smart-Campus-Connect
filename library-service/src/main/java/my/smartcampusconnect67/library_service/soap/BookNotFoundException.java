package my.smartcampusconnect67.library_service.soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "BookNotFoundFault", targetNamespace = "http://library.smartcampus.edu/")
public class BookNotFoundException extends RuntimeException {

    private final FaultDetail faultInfo;

    public BookNotFoundException(String message, String detailMessage) {
        super(message);
        this.faultInfo = new FaultDetail(detailMessage);
    }

    public FaultDetail getFaultInfo() {
        return faultInfo;
    }
}
