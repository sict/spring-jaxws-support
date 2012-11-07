package nl.sict.springjws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import nl.sict.springjws.JaxwsEndpointMapping;
import nl.sict.springjws.webservice.ObjectFactory;
import nl.sict.springjws.webservice.SameForRequestAndResponse;
import nl.sict.springjws.webservice.XmlRootElementRequest;
import nl.sict.springjws.webservice.XmlType;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;

public class JaxwsEndpointMappingTest {

	private JaxwsEndpointMapping instance;

	private MessageContext createMessageContext(Object requestPayload) throws Exception {
		MessageContext messageContext = mock(MessageContext.class);
		WebServiceMessage requestMessage = mock(WebServiceMessage.class);
		when(messageContext.getRequest()).thenReturn(requestMessage);
		JAXBContext jaxbContext = requestPayload.getClass().getAnnotation(XmlRootElement.class) != null ? 
				JAXBContext.newInstance(requestPayload.getClass()) : JAXBContext.newInstance("nl.sict.springjws.webservice"); 
		Source requestPayloadSource = new JAXBSource(jaxbContext, requestPayload);
		when(requestMessage.getPayloadSource()).thenReturn(requestPayloadSource);
		
		return messageContext;
	}
	
	@Before
	public void setup() {
		instance = new JaxwsEndpointMapping();
		ApplicationContext context = mock(ApplicationContext.class);
		when(context.getBeanNamesForType((Class<?>) any())).thenReturn(new String[] { "webservice" });
		doReturn(WebserviceImpl.class).when(context).getType("webservice");
		when(context.containsBean("webservice")).thenReturn(true);
		instance.setApplicationContext(context);
	}
	
	@Test
	public void testMapping() throws Exception {
		ObjectFactory of = new ObjectFactory();
		Object[] requestPayloads = { new XmlRootElementRequest(), of.createXmlTypeRequest(new XmlType()), new SameForRequestAndResponse() };
		String[] serviceMethods = { "xmlRootElementOperation", "xmlTypeOperation", "sameMessageForRequestAndResponse" };
		assertEquals(requestPayloads.length, serviceMethods.length);
		
		for (int i = 0; i < requestPayloads.length; i++) {
			Object requestPayload = requestPayloads[i];
			String serviceMethod = serviceMethods[i];
			
			MessageContext messageContext = createMessageContext(requestPayload);
			EndpointInvocationChain endpoint = instance.getEndpoint(messageContext);
			assertNotNull("Did not find expected endpoint", endpoint);
			assertTrue("Expect endpoint to be of type MethodEndpoint", endpoint.getEndpoint() instanceof MethodEndpoint);
			MethodEndpoint mep = (MethodEndpoint) endpoint.getEndpoint();
			assertEquals(serviceMethod, mep.getMethod().getName());
			assertEquals(WebserviceImpl.class, mep.getMethod().getDeclaringClass());
		}
		
		Field field = ReflectionUtils.findField(JaxwsEndpointMapping.class, "endpointMap");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<QName, Endpoint> endpointMap = (Map<QName, Endpoint>) ReflectionUtils.getField(field, instance);
		assertEquals(4, endpointMap.size());
	}
}
