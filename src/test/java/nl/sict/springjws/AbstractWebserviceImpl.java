package nl.sict.springjws;

import nl.sict.springjws.webservice.OneWayRequest;
import nl.sict.springjws.webservice.Webservice;

public abstract class AbstractWebserviceImpl  implements Webservice {

	// Deliberately left out annotations, so the mapper and adapter will
	// need to scan the interface.
	public void oneWayOperation(OneWayRequest parameters) {
		
	}

}