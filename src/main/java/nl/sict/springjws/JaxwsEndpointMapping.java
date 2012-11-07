package nl.sict.springjws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.springframework.core.annotation.AnnotationUtils;
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
    	WebMethod webMethodAnnotation = AnnotationUtils.findAnnotation(method, WebMethod.class);
    	QName result = null;
        if (webMethodAnnotation != null && method.getParameterTypes().length == 1) {
        	Annotation[] paramAnnotations = method.getParameterAnnotations()[0];
        	for (Annotation annotation : paramAnnotations) {
        		if (annotation instanceof WebParam) {
        			WebParam webParam = (WebParam) annotation;
					String namespace = webParam.targetNamespace();
					String name = webParam.name();
					result = new QName(namespace, name);
					
					break;
        		}
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
