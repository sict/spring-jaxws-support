package nl.sict.springjws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.Holder;

import nl.sict.springjws.webservice.OneWayRequest;
import nl.sict.springjws.webservice.XmlRootElementRequest;

import org.junit.Test;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

public class AnnotationUtilsExtTest {

	@Test
	public void testFindParameterAnnotationOnDeclaringClass() throws Exception {
		WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(WebserviceImpl.class.getMethod("xmlRootElementOperation", XmlRootElementRequest.class), 0, WebParam.class);
		assertNotNull("Expected to find a @WebParam annotation", webParam);
	}
	
	@Test
	public void testFindParameterAnnotationOnImplementedInterface() throws Exception {
		WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(WebserviceImpl.class.getMethod("sameMessageForRequestAndResponse", Holder.class), 0, WebParam.class);
		assertNotNull("Expected to find a @WebParam annotation", webParam);
	}

	@Test
	public void testFindParameterAnnotationOnOverriddenMethod() throws Exception {
		WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(WebserviceImpl.class.getMethod("oneWayOperation", OneWayRequest.class), 0, WebParam.class);
		assertNotNull("Expected to find a @WebParam annotation", webParam);
	}
	
	@Test
	public void testParameterAnnotationNotFound() throws Exception {
		RequestPayload requestPayload = AnnotationUtilsExt.findParameterAnnotation(WebserviceImpl.class.getMethod("sameMessageForRequestAndResponse", Holder.class), 0, RequestPayload.class);
		assertNull("Expected *not* to find a @RequestPayload annotation", requestPayload);
	}
	
	@Test
	public void testFindMethodAnnotationOnInterface() throws Exception {
		WebMethod webMethod = AnnotationUtilsExt.findAnnotation(WebserviceImpl.class.getMethod("sameMessageForRequestAndResponse", Holder.class), WebMethod.class);
		assertNotNull("Expected to find @WebMethod annotation", webMethod);
	}
	
	@Test
	public void testMethodAnnotationNotFound() throws Exception {
		PayloadRoot payloadRoot = AnnotationUtilsExt.findAnnotation(WebserviceImpl.class.getMethod("sameMessageForRequestAndResponse", Holder.class), PayloadRoot.class);
		assertNull("Expected *not* to find @PayloadRoot annotation", payloadRoot);
	}
	
}
