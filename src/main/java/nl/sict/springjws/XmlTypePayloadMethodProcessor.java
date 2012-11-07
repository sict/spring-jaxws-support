package nl.sict.springjws;

import java.lang.reflect.Method;

import javax.jws.WebResult;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.XmlRootElementPayloadMethodProcessor;

public class XmlTypePayloadMethodProcessor extends XmlRootElementPayloadMethodProcessor {

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
    	//This method won't actually get called due to the hierarchy in Spring's sources,
    	//but is implemented anyway for clarification.
        Class<?> parameterType = returnType.getParameterType();
        return parameterType.isAnnotationPresent(XmlType.class) || parameterType.isAnnotationPresent(XmlRootElement.class);
    }

	
    public void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
            throws JAXBException {
        Class<?> parameterType = returnType.getParameterType();
        if (parameterType.isAnnotationPresent(XmlRootElement.class)) {
        	super.handleReturnValue(messageContext, returnType, returnValue);
        } else if (parameterType.isAnnotationPresent(XmlType.class)) {
	        Method method = returnType.getMethod();
	        WebResult webResult = AnnotationUtils.findAnnotation(method, WebResult.class);
	        if (StringUtils.isEmpty(webResult.targetNamespace()) || StringUtils.isEmpty(webResult.name())) {
	        	throw new IllegalArgumentException("targetNamespace or name of @WebResult annotation is empty.");
	        }
	        QName name = new QName(webResult.targetNamespace(), webResult.name());
			@SuppressWarnings({"unchecked", "rawtypes"})
			JAXBElement el = new JAXBElement(name, returnType.getParameterType(), returnValue);
	        marshalToResponsePayload(messageContext, parameterType, el);
        } else {
        	throw new IllegalArgumentException("Unsupported return value type");
        }
    }
	
}
