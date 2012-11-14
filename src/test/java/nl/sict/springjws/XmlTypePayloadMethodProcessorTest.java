package nl.sict.springjws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;
import javax.xml.ws.Holder;

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
	public void testUnAnnotatedReturnPayloadSupported() {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(String.class).when(returnType).getParameterType();
		assertTrue("Basically, all classes should be supported", instance.supportsResponsePayloadReturnType(returnType));
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
		returnValue.setContent("content");
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
		
		@SuppressWarnings("unchecked")
		JAXBElement<XmlType> resultElement = (JAXBElement<XmlType>) result.getResult();
		assertEquals("content", resultElement.getValue().getContent());
		assertEquals("XmlTypeResponse", resultElement.getName().getLocalPart());
		assertEquals("http://www.sict.nl/springjws/Webservice", resultElement.getName().getNamespaceURI());
	}
	
	@Test
	public void testSimpleReturnValue() throws Exception {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		Object returnValue = "SOME STRING. DONT REALLY CARE, ACTUALLY. COULD EVEN BE NULL";
		MethodParameter returnType = mock(MethodParameter.class);
		Method method = getClass().getDeclaredMethod("methodThatReturnsString", (Class[]) null);
		when(returnType.getMethod()).thenReturn(method);
		doReturn(String.class).when(returnType).getParameterType();
		
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage responseMessage = mock(WebServiceMessage.class);
		JAXBResult result = new JAXBResult(JAXBContext.newInstance(XmlType.class.getPackage().getName()));
		when(responseMessage.getPayloadResult()).thenReturn(result);
		when(messageContext.getResponse()).thenReturn(responseMessage);
		
		instance.handleReturnValue(messageContext, returnType, returnValue);
		@SuppressWarnings("unchecked")
		JAXBElement<String> resultElement = (JAXBElement<String>) result.getResult();
		assertEquals("SOME STRING. DONT REALLY CARE, ACTUALLY. COULD EVEN BE NULL", resultElement.getValue());
		assertEquals("StringResponse", resultElement.getName().getLocalPart());
		assertEquals("http://www.sict.nl/springjws/Webservice", resultElement.getName().getNamespaceURI());
	}
	
	@Test
	public void testMethodWithInOutParameter() throws Exception {
		XmlTypePayloadMethodProcessor instance = new XmlTypePayloadMethodProcessor();
		Object returnValue = "SOME STRING. DONT REALLY CARE, ACTUALLY. COULD EVEN BE NULL";
		MethodParameter returnType = mock(MethodParameter.class);
		doReturn(String.class).when(returnType).getParameterType();
		Method method = getClass().getDeclaredMethod("methodWithInOutParam", Holder.class);
		when(returnType.getMethod()).thenReturn(method);
		
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage responseMessage = mock(WebServiceMessage.class);
		JAXBResult result = new JAXBResult(JAXBContext.newInstance(XmlType.class.getPackage().getName()));
		when(responseMessage.getPayloadResult()).thenReturn(result);
		when(messageContext.getResponse()).thenReturn(responseMessage);
		
		instance.handleReturnValue(messageContext, returnType, returnValue);
		
		@SuppressWarnings("unchecked")
		JAXBElement<XmlType> resultElement = (JAXBElement<XmlType>) result.getResult();
		assertEquals("SOME STRING. DONT REALLY CARE, ACTUALLY. COULD EVEN BE NULL", resultElement.getValue());
		assertEquals("StringRequest", resultElement.getName().getLocalPart());
		assertEquals("http://www.sict.nl/springjws/Webservice", resultElement.getName().getNamespaceURI());
	}
	
	@WebResult
	private XmlType methodThatReturnsXmlTypeWithoutProperAnnotation() {
		return null;
	}
	
	@WebResult(name = "XmlTypeResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters")
	private XmlType methodThatReturnsXmlTypeWithProperAnnotation() {
		return null;
	}
	
	@WebResult(name = "StringResponse", targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters")
	private String methodThatReturnsString() {
		return null;
	}
	
	@SuppressWarnings("unused")
	private void methodWithInOutParam(@WebParam(targetNamespace = "http://www.sict.nl/springjws/Webservice", partName = "parameters", mode = Mode.INOUT, name = "StringRequest") Holder<String> arg) {
		
	}
}
