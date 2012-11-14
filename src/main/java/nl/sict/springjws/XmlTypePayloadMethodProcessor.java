package nl.sict.springjws;

import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.method.jaxb.XmlRootElementPayloadMethodProcessor;

public class XmlTypePayloadMethodProcessor extends XmlRootElementPayloadMethodProcessor {

    @Override
    protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
    	//This method won't actually get called due to the hierarchy in Spring's sources.
    	return true;
    }

	
    public void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
            throws JAXBException {
        Class<?> parameterType = returnType.getParameterType();
        if (parameterType.isAnnotationPresent(XmlRootElement.class)) {
        	super.handleReturnValue(messageContext, returnType, returnValue);
        } else {
	        Method method = returnType.getMethod();
	        final QName name;
	        WebResult webResult = AnnotationUtils.findAnnotation(method, WebResult.class);
	        if (webResult != null) {
				name = new QName(webResult.targetNamespace(), webResult.name());
	        } else {
	        	WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(method, 0, WebParam.class);
	        	Assert.notNull(webParam, String.format("No @WebResult or @WebParam annotation found on %s", method.getName()));
				name = new QName(webParam.targetNamespace(), webParam.name());
	        }
	        Assert.isTrue(StringUtils.isNotEmpty(name.getLocalPart()), "At least the local (tag)name should be specified");
			@SuppressWarnings({"unchecked", "rawtypes"})
			JAXBElement el = new JAXBElement(name, returnType.getParameterType(), returnValue);
	        marshalToResponsePayload(messageContext, parameterType, el);
        }
    }
	
}
