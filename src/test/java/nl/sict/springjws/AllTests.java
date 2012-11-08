package nl.sict.springjws;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AnnotationUtilsExtTest.class, JaxwsEndpointMappingTest.class, JaxwsMethodEndpointAdapterTest.class, JaxwsSupportIntegrationTest.class, XmlTypePayloadMethodProcessorTest.class })
public class AllTests {

}
