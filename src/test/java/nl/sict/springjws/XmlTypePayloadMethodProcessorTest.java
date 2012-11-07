package nl.sict.springjws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;

import nl.sict.springjws.XmlTypePayloadMethodProcessor;
import nl.sict.springjws.webservice.XmlRootElementResponse;
import nl.sict.springjws.webservice.XmlType;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

public class XmlTypePayloadMethodProcessorTest {
	
	@Test
	public void testXmlTypeReturnPayloadSupported() {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(XmlType.class).when(returnType).getParameterType();
		assertTrue("@XmlType annotated classes should be supported", instance.supportsResponsePayloadReturnType(returnType));
	}
	
	@Test
	public void testXmlRootElementReturnPayloadSupported() {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(XmlRootElementResponse.class).when(returnType).getParameterType();
		assertTrue("@XmlRootElement annotated classes should be supported", instance.supportsResponsePayloadReturnType(returnType));
	}

	@Test
	public void testUnsupportedReturnPayloadSupported() {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(String.class).when(returnType).getParameterType();
		assertFalse("Only @XmlType and @XmlRootElement annotated classes should be supported", instance.supportsResponsePayloadReturnType(returnType));
	}

	
	@Test
	public void testHandleXmlRootElementReturnValue() throws JAXBException {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		Object returnValue = new XmlRootElementResponse();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(XmlRootElementResponse.class).when(returnType).getParameterType();
		
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage responseMessage = mock(WebServiceMessage.class);
		JAXBResult result = new JAXBResult(JAXBContext.newInstance(XmlRootElementResponse.class));
		when(responseMessage.getPayloadResult()).thenReturn(result);
		when(messageContext.getResponse()).thenReturn(responseMessage);
		instance.handleReturnValue(messageContext, returnType, returnValue);
		Object responseRoot = result.getResult();
		assertTrue(responseRoot instanceof XmlRootElementResponse);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testHandleXmlTypeReturnValueWithoutProperAnnontation() throws Exception {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		XmlType returnValue = new XmlType();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(XmlType.class).when(returnType).getParameterType();
		Method method = getClass().getDeclaredMethod("methodThatReturnsXmlTypeWithoutProperAnnotation", (Class[]) null);
		when(returnType.getMethod()).thenReturn(method);
		
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage responseMessage = mock(WebServiceMessage.class);
		JAXBResult result = new JAXBResult(JAXBContext.newInstance(XmlType.class.getPackage().getName()));
		when(responseMessage.getPayloadResult()).thenReturn(result);
		when(messageContext.getResponse()).thenReturn(responseMessage);
		
		instance.handleReturnValue(messageContext, returnType, returnValue);
	}
	
	@Test
	public void testHandleXmlTypeReturnValueWithProperAnnontation() throws Exception {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		XmlType returnValue = new XmlType();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(XmlType.class).when(returnType).getParameterType();
		Method method = getClass().getDeclaredMethod("methodThatReturnsXmlTypeWithProperAnnotation", (Class[]) null);
		when(returnType.getMethod()).thenReturn(method);
		
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage responseMessage = mock(WebServiceMessage.class);
		JAXBResult result = new JAXBResult(JAXBContext.newInstance(XmlType.class.getPackage().getName()));
		when(responseMessage.getPayloadResult()).thenReturn(result);
		when(messageContext.getResponse()).thenReturn(responseMessage);
		
		instance.handleReturnValue(messageContext, returnType, returnValue);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testUnsupportedReturnValue() throws Exception {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		Object returnValue = "SOME STRING. DONT REALLY CARE, ACTUALLY. COULD EVEN BE NULL";
		MethodParameter returnType = mock(MethodParameter.class);
		Method method = getClass().getDeclaredMethod("methodThatReturnsString", (Class[]) null);
		when(returnType.getMethod()).thenReturn(method);
		doReturn(String.class).when(returnType).getParameterType();
		MessageContext messageContext = null;
		
		instance.handleReturnValue(messageContext, returnType, returnValue);
	}
	
	@WebResult
	private XmlType methodThatReturnsXmlTypeWithoutProperAnnotation() {
		return null;
	}
	
	@WebResult(name = "XmlTypeResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters")
	private XmlType methodThatReturnsXmlTypeWithProperAnnotation() {
		return null;
	}
	
	@WebResult
	private String methodThatReturnsString() {
		return null;
	}
	
}
