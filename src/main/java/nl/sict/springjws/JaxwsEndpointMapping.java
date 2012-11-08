package nl.sict.springjws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationMethodEndpointMapping;
import org.springframework.ws.server.endpoint.support.PayloadRootUtils;
import org.springframework.xml.transform.TransformerHelper;

public class JaxwsEndpointMapping extends AbstractAnnotationMethodEndpointMapping<QName> {
	
    private final TransformerHelper transformerHelper;
    
    public JaxwsEndpointMapping() {
    	transformerHelper = new TransformerHelper();
    }
	
    /** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
    protected Class<? extends Annotation> getEndpointAnnotationType() {
        return WebService.class;
    }

    @Override
    protected QName getLookupKeyForMethod(Method method) {
    	WebMethod webMethodAnnotation = AnnotationUtilsExt.findAnnotation(method, WebMethod.class);
    	QName result = null;
        if (webMethodAnnotation != null && method.getParameterTypes().length == 1) {
        	WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(method, 0, WebParam.class);
        	if (webParam != null) {
				String namespace = webParam.targetNamespace();
				String name = webParam.name();
				result = new QName(namespace, name);
        	}
        }
        // Debug logging is done by org.springframework.ws.server.endpoint.mapping.AbstractMethodEndpointMapping.registerEndpoint(T, MethodEndpoint),
    	// so no need to do it here.
        
        return result;
    }

    @Override
    protected QName getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        // Debug logging is done by org.springframework.ws.server.endpoint.mapping.AbstractMethodEndpointMapping<T>.getEndpointInternal(..),
    	// so no need to do it here.
        return PayloadRootUtils.getPayloadRootQName(messageContext.getRequest().getPayloadSource(), transformerHelper);
    }

}
