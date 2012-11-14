package nl.sict.springjws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.Holder;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.AbstractMethodEndpointAdapter;

public class JaxwsMethodEndpointAdapter extends AbstractMethodEndpointAdapter {
	private final XmlTypePayloadMethodProcessor methodProcessor;
	
	public JaxwsMethodEndpointAdapter() {
		methodProcessor = new XmlTypePayloadMethodProcessor();
	}

	private Class<?> getActualType(MethodParameter parameter) {
		final Class<?> actualType;
		if (Holder.class.isAssignableFrom(parameter.getParameterType())) {
			Type[] typeArgs = ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments();
			actualType = (Class<?>) typeArgs[0];
		} else {
			actualType = parameter.getParameterType();
		}
		
		return actualType;
	}
	
	private boolean isWebMethod(MethodEndpoint methodEndpoint) {
		return AnnotationUtilsExt.findAnnotation(methodEndpoint.getMethod(), WebMethod.class) != null;
	}
	
	private boolean parametersAreSupported(MethodEndpoint methodEndpoint) {
		MethodParameter parameter = methodEndpoint.getMethodParameters().length != 1 ? null : methodEndpoint.getMethodParameters()[0];
		final WebParam webParam = parameter == null ? null : AnnotationUtilsExt.findParameterAnnotation(parameter.getMethod(), parameter.getParameterIndex(), WebParam.class);
		return webParam != null && StringUtils.isNotEmpty(webParam.targetNamespace()) && StringUtils.isNotEmpty(webParam.name());
	}
	
	private boolean isOneWay(MethodEndpoint methodEndpoint) {
		return Void.TYPE.equals(methodEndpoint.getReturnType().getParameterType()) 
				&& AnnotationUtilsExt.findAnnotation(methodEndpoint.getMethod(), Oneway.class) != null;
	}
	
	private boolean returnValueIsSupported(MethodEndpoint methodEndpoint) {
		final WebResult webResult = AnnotationUtilsExt.findAnnotation(methodEndpoint.getMethod(), WebResult.class);
		MethodParameter parameter = methodEndpoint.getMethodParameters().length != 1 ? null : methodEndpoint.getMethodParameters()[0];
		final WebParam webParam = parameter == null ? null : AnnotationUtilsExt.findParameterAnnotation(parameter.getMethod(), parameter.getParameterIndex(), WebParam.class);

		return (webResult != null && !Void.TYPE.equals(methodEndpoint.getReturnType().getParameterType()) && StringUtils.isNotEmpty(webResult.targetNamespace()) && StringUtils.isNotEmpty(webResult.name()))
				||
				(webParam != null && webParam.mode() == WebParam.Mode.INOUT && Holder.class.isAssignableFrom(parameter.getParameterType()) && StringUtils.isNotEmpty(webParam.targetNamespace()) && StringUtils.isNotEmpty(webParam.name()))
				||
				isOneWay(methodEndpoint);
	}
		
			
	
	@Override
	protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
		return isWebMethod(methodEndpoint)
				&& parametersAreSupported(methodEndpoint)
				&& returnValueIsSupported(methodEndpoint);
	}

	@Override
	protected void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
        Object[] args = getMethodArguments(messageContext, methodEndpoint);

        if (logger.isTraceEnabled()) {
            StringBuilder builder = new StringBuilder("Invoking [");
            builder.append(methodEndpoint).append("] with arguments ");
            builder.append(Arrays.asList(args));
            logger.trace(builder.toString());
        }

        Object returnValue = methodEndpoint.invoke(args);
        if (hasInOutParameter(methodEndpoint)) {
        	// holder-style return
        	@SuppressWarnings("rawtypes")
			Holder holder = (Holder) args[0];
        	returnValue = holder.value;
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace("Method [" + methodEndpoint + "] returned [" + returnValue + "]");
        }

        if (!isOneWay(methodEndpoint)) {
        	handleMethodReturnValue(messageContext, returnValue, methodEndpoint);
        }
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object[] getMethodArguments(MessageContext messageContext, MethodEndpoint methodEndpoint)
            throws Exception {
        MethodParameter[] parameters = methodEndpoint.getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
        	MethodParameter param = parameters[i];
    		if (param.getParameterType().equals(Holder.class)) {
				Object value = methodProcessor.resolveArgument(messageContext, wrapMethodParameter(param));
				args[i] = new Holder(value);
        	} else {
        		args[i] = methodProcessor.resolveArgument(messageContext, param);
        	}
        }
        return args;
    }

    private MethodParameter wrapMethodParameter(MethodParameter methodParameter) {
		final Class<?> holderType = getActualType(methodParameter);
		
		MethodParameter wrappedMethodParameter = new MethodParameter(methodParameter) {
			@Override
			public Class<?> getParameterType() {
				return holderType;
			}
		};
		
		return wrappedMethodParameter;
	}

    private boolean isInOutParameter(MethodParameter parameter) {
    	WebParam webParam = AnnotationUtilsExt.findParameterAnnotation(parameter.getMethod(), parameter.getParameterIndex(), WebParam.class);
		return Holder.class.isAssignableFrom(parameter.getParameterType())
    			&& webParam != null
    			&& webParam.mode() == WebParam.Mode.INOUT; 
    }
    
    private boolean hasInOutParameter(MethodEndpoint methodEndpoint) {
    	return Void.TYPE.equals(methodEndpoint.getReturnType().getParameterType()) 
    			&& methodEndpoint.getMethodParameters().length == 1
    			&& isInOutParameter(methodEndpoint.getMethodParameters()[0]);
    }
    
	protected void handleMethodReturnValue(MessageContext messageContext,
                                           Object returnValue,
                                           MethodEndpoint methodEndpoint) throws Exception {
        MethodParameter returnType;
        if (hasInOutParameter(methodEndpoint)) {
        	returnType = wrapMethodParameter(methodEndpoint.getMethodParameters()[0]);
        } else {
    		returnType = methodEndpoint.getReturnType();
        }
        methodProcessor.handleReturnValue(messageContext, returnType, returnValue);
    }	
}
