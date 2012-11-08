package nl.sict.springjws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import nl.sict.springjws.webservice.OneWayRequest;
import nl.sict.springjws.webservice.SameForRequestAndResponse;
import nl.sict.springjws.webservice.XmlRootElementRequest;
import nl.sict.springjws.webservice.XmlRootElementResponse;
import nl.sict.springjws.webservice.XmlType;

import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

@WebService
@Component
public class WebserviceImpl extends AbstractWebserviceImpl {

	public void notAnExposedMethod() {
		
	}
	
	@WebMethod
	public void operationWithoutArgumentsWillNotBeMapped() {
		
	}

	@WebMethod(operationName = "XmlRootElementOperation", action = "http://www.sict.nl/springjws/Webservice/XmlRootElementOperation")
	@WebResult(name = "XmlRootElementResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters")
	public XmlRootElementResponse xmlRootElementOperation(@RequestPayload
			@WebParam(name = "XmlRootElementRequest", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters") XmlRootElementRequest parameters) {
		XmlRootElementResponse result = new XmlRootElementResponse();
		result.setContent("xmlRootElementOperation");
		return result;
	}

	@WebMethod(operationName = "XmlTypeOperation", action = "http://www.sict.nl/springjws/Webservice/XmlTypeOperation")
	@WebResult(name = "XmlTypeResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters")
	public XmlType xmlTypeOperation(
			@WebParam(name = "XmlTypeRequest", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters") XmlType parameters) {
		XmlType result = new XmlType();
		result.setContent("xmlTypeOperation");
		return result;
	}

	// Removed annotations, so they should be found on the interface.
	public void sameMessageForRequestAndResponse(Holder<SameForRequestAndResponse> parameters) {
		parameters.value.setContent("sameMessageForRequestAndResponse");
	}
	
	@Override
	// Removed annotations, so they should be found on the  superclass.
	public void oneWayOperation(OneWayRequest parameters) {
		super.oneWayOperation(parameters);
	}

}
