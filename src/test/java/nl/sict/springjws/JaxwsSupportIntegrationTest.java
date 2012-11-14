package nl.sict.springjws;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;

import nl.sict.springjws.webservice.OneWayRequest;
import nl.sict.springjws.webservice.XmlRootElementRequest;
import nl.sict.springjws.webservice.XmlRootElementResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.ResponseMatcher;
import org.springframework.xml.transform.StringSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("spring-ws-servlet.xml")
public class JaxwsSupportIntegrationTest {
	@Autowired
	private ApplicationContext applicationContext;
	private JAXBContext jaxbContext;

	@Before
	public void setup() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(XmlRootElementRequest.class.getPackage().getName());
	}

	@Test
	public void testXmlRootElementEndpoint() throws Exception {
		XmlRootElementRequest request = new XmlRootElementRequest();
		request.setContent("Not relevant");
		Source requestPayload = new JAXBSource(jaxbContext, request);
		XmlRootElementResponse expectedResponse = new XmlRootElementResponse();
		expectedResponse.setContent("xmlRootElementOperation");
		Source responsePayload = new JAXBSource(jaxbContext, expectedResponse);
		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);
		client.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
	}

	@Test
	public void testXmlTypeEndpoint() throws Exception {
		Source requestPayload = new StringSource("<XmlTypeRequest xmlns=\"http://www.sict.nl/springjws/Webservice\"><content>goes</content></XmlTypeRequest>");
		Source responsePayload = new StringSource("<ns3:XmlTypeResponse xmlns:ns3=\"http://www.sict.nl/springjws/Webservice\"><content xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">xmlTypeOperation</content></ns3:XmlTypeResponse>");
		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);
		client.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
	}

	@Test
	public void testHolderEndPoint() {
		Source requestPayload = new StringSource("<SameForRequestAndResponse xmlns=\"http://www.sict.nl/springjws/Webservice\"><content>goes</content></SameForRequestAndResponse>");
		Source responsePayload = new StringSource("<ns:SameForRequestAndResponse xmlns:ns=\"http://www.sict.nl/springjws/Webservice\"><content>sameMessageForRequestAndResponse</content></ns:SameForRequestAndResponse>");
		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);
		client.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
	}

	@Test
	public void testOneWayEndpoint() throws Exception {
		OneWayRequest request = new OneWayRequest();
		Source requestPayload = new JAXBSource(jaxbContext, request);
		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);
		client.sendRequest(withPayload(requestPayload)).andExpect(new ResponseMatcher() {

			public void match(WebServiceMessage request, WebServiceMessage response) throws IOException, AssertionError {
				assertNull("Did not expect a response message", response.getPayloadSource());
			}
		});
	}
	
	@Test
	public void testSimpleArgumentAndReturnTypeOperation() throws Exception {
		Source requestPayload = new StringSource("<StringRequest xmlns=\"http://www.sict.nl/springjws/Webservice\">goes</StringRequest>");
		Source responsePayload = new StringSource("<ns:StringResponse xmlns:ns=\"http://www.sict.nl/springjws/Webservice\">simpleArgumentAndReturnTypeOperation</ns:StringResponse>");
		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);
		client.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
	}

}
