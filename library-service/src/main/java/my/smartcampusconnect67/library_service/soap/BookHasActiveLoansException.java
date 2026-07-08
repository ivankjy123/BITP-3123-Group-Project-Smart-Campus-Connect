package my.smartcampusconnect67.library_service.soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "BookHasActiveLoansFault", targetNamespace = "http://library.smartcampus.edu/")
public class BookHasActiveLoansException extends RuntimeException {

    private final FaultDetail faultInfo;

    public BookHasActiveLoansException(String message, String detailMessage) {
        super(message);
        this.faultInfo = new FaultDetail(detailMessage);
    }

    public FaultDetail getFaultInfo() {
        return faultInfo;
    }
}
