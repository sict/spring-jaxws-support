package nl.sict.springjws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;

public abstract class AnnotationUtilsExt {

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T findParameterAnnotation(Method method, int parameterIndex, Class<T> annotationClass) {
		Annotation[] annotations = method.getParameterAnnotations()[parameterIndex];
		T result= null;
		for (Annotation ann : annotations) {
			if (annotationClass.isAssignableFrom(ann.getClass())) {
				result = (T) ann;
			}
		}
		if (result == null) {
			for (Class<?> iface : method.getDeclaringClass().getInterfaces()) {
				try {
	    			Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
	    			result = findParameterAnnotation(ifaceMethod, parameterIndex, annotationClass);
	    			if (result != null) {
	    				break;
	    			}
				} catch (NoSuchMethodException e) {
					// dont care
				}
			}
		}
		if (result == null && method.getDeclaringClass().getSuperclass() != null) {
			try {
				Method superMethod = method.getDeclaringClass().getSuperclass().getMethod(method.getName(), method.getParameterTypes());
				result = findParameterAnnotation(superMethod, parameterIndex, annotationClass);
			} catch (NoSuchMethodException e) {
				// dont care
			}
		}
		
		return result;
	}

	/**
	 * AnnotationUtils.findAnnotation does not scan all superclasses and interfaces.
	 * 
	 */
	public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
		A annotation = AnnotationUtils.findAnnotation(method, annotationType);
		for (Class<?> cl = method.getDeclaringClass(); annotation == null && cl != null && cl != Object.class; cl = cl.getSuperclass()) {
			for (Class<?> iface : cl.getInterfaces()) {
				try {
					Method ifaceMethod = iface.getDeclaredMethod(method.getName(), method.getParameterTypes());
					annotation = ifaceMethod.getAnnotation(annotationType);
					if (annotation != null) {
						break;
					}
				} catch (NoSuchMethodException e) {
					// dont care
				}
			}
		}
		return annotation;
	}
	
}
