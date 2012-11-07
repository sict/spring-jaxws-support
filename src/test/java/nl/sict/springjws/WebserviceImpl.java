package nl.sict.springjws;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import nl.sict.springjws.webservice.OneWayRequest;
import nl.sict.springjws.webservice.SameForRequestAndResponse;
import nl.sict.springjws.webservice.Webservice;
import nl.sict.springjws.webservice.XmlRootElementRequest;
import nl.sict.springjws.webservice.XmlRootElementResponse;
import nl.sict.springjws.webservice.XmlType;

import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

@WebService
@Component
public class WebserviceImpl implements Webservice {

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

	@WebMethod(operationName = "SameMessageForRequestAndResponse", action = "http://www.sict.nl/springjws/Webservice/SameMessageForRequestAndResponse")
	public void sameMessageForRequestAndResponse(
			@WebParam(name = "SameForRequestAndResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", mode = Mode.INOUT, partName = "parameters") Holder<SameForRequestAndResponse> parameters) {
		parameters.value.setContent("sameMessageForRequestAndResponse");
	}

	@WebMethod(operationName = "OneWayOperation", action = "http://www.sict.nl/springjws/Webservice/OneWayOperation")
	@Oneway
	public void oneWayOperation(@WebParam(name = "OneWayRequest", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters") OneWayRequest parameters) {
		
	}

}
