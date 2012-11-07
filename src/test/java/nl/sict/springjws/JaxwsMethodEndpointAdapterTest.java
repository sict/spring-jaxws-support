package nl.sict.springjws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebParam.Mode;
import javax.xml.ws.Holder;

import nl.sict.springjws.JaxwsMethodEndpointAdapter;
import nl.sict.springjws.XmlTypePayloadMethodProcessor;
import nl.sict.springjws.webservice.SameForRequestAndResponse;
import nl.sict.springjws.webservice.XmlRootElementRequest;
import nl.sict.springjws.webservice.XmlRootElementResponse;
import nl.sict.springjws.webservice.XmlType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

public class JaxwsMethodEndpointAdapterTest {

	private JaxwsMethodEndpointAdapter instance;
	private XmlTypePayloadMethodProcessor mockProcessor;

	@Before
	public void setup() {
		instance = new JaxwsMethodEndpointAdapter();
		mockProcessor = mock(XmlTypePayloadMethodProcessor.class);
		Field f = ReflectionUtils.findField(JaxwsMethodEndpointAdapter.class, "methodProcessor");
		f.setAccessible(true);
		ReflectionUtils.setField(f, instance, mockProcessor);
		Logger.getLogger(JaxwsMethodEndpointAdapter.class).setLevel(Level.TRACE);
	}

	@Test
	public void testSupportedEndpoints() {
		String[] methodNames = { "xmlRootElementMethod", "xmlTypeMethod", "holderMethod" };
		for (String methodName : methodNames) {
			MethodEndpoint ep = createEndpoint(methodName);
			assertTrue("Expected to find support for endpoint " + ep.getMethod().getName(), instance.supportsInternal(ep));
		}
	}

	@Test
	public void testInvokeRequestResponseType() throws Exception {
		MethodEndpoint ep = createEndpoint("xmlRootElementMethod");
		MessageContext messageContext = mock(MessageContext.class);
		when(mockProcessor.resolveArgument((MessageContext) any(), (MethodParameter) any())).thenReturn(new XmlRootElementRequest());
		instance.invokeInternal(messageContext, ep);
		verify(mockProcessor).handleReturnValue((MessageContext) any(), (MethodParameter) any(), argThat(new BaseMatcher<Object>() {

			public boolean matches(Object item) {
				return item instanceof XmlRootElementResponse;
			}

			public void describeTo(Description description) {
			}

		}));
	}

	@Test
	public void testInvokeHolderType() throws Exception {
		MethodEndpoint ep = createEndpoint("holderMethod");
		MessageContext messageContext = mock(MessageContext.class);
		when(mockProcessor.resolveArgument((MessageContext) any(), (MethodParameter) any())).thenReturn(new SameForRequestAndResponse());
		instance.invokeInternal(messageContext, ep);
		verify(mockProcessor).handleReturnValue((MessageContext) any(), (MethodParameter) any(), argThat(new BaseMatcher<Object>() {

			public boolean matches(Object item) {
				return item instanceof SameForRequestAndResponse && "a real value".equals(((SameForRequestAndResponse) item).getContent());
			}

			public void describeTo(Description description) {
			}

		}));
	}

	@Test
	public void testUnsupportedEndpoints() {
		String[] methodNames = { "unexposedMethod", "unsupportedNumberOfArguments", "unsupportedMissingArgumentAnnotation", "unsupportedMissingResultAnnotation", "unsupportedHolderMethod", "unsupportedReturnType" };
		for (String methodName : methodNames) {
			MethodEndpoint ep = createEndpoint(methodName);
			assertFalse("Not expected to find support for endpoint " + ep.getMethod().getName(), instance.supportsInternal(ep));
		}
	}

	private MethodEndpoint createEndpoint(String methodName) {
		Method[] methods = getClass().getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				MethodEndpoint ep = new MethodEndpoint(this, m);
				return ep;
			}
		}

		throw new IllegalStateException();
	}

	@WebMethod
	@WebResult
	private XmlRootElementResponse xmlRootElementMethod(@WebParam XmlRootElementRequest request) {
		return new XmlRootElementResponse();
	}

	@WebMethod
	@WebResult
	private XmlType xmlTypeMethod(@WebParam XmlType request) {
		return null;
	}

	@WebMethod
	private void holderMethod(@WebParam(mode = Mode.INOUT) Holder<SameForRequestAndResponse> params) {
		params.value.setContent("a real value");
	}

	@SuppressWarnings("unused")
	private void unexposedMethod() {
	}

	@WebMethod
	private void unsupportedNumberOfArguments(String arg1, String arg2) {
	}

	@WebMethod
	private void unsupportedMissingArgumentAnnotation(String arg1) {
	}

	@WebMethod
	private XmlType unsupportedMissingResultAnnotation(@WebParam XmlType request) {
		return null;
	}

	@WebMethod
	private String unsupportedHolderMethod(@WebParam(mode = Mode.INOUT) Holder<SameForRequestAndResponse> params) {
		return null;
	}

	@WebMethod
	@WebResult
	private String unsupportedReturnType(@WebParam XmlType request) {
		return null;
	}
}
