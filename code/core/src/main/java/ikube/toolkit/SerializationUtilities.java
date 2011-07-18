package ikube.toolkit;

import ikube.IConstants;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableInternet;
import ikube.model.IndexableTable;
import ikube.model.Url;

import java.beans.BeanInfo;
import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public final class SerializationUtilities {

	private static final Logger LOGGER = Logger.getLogger(SerializationUtilities.class);
	private static ExceptionListener EXCEPTION_LISTENER = new ExceptionListener() {
		@Override
		public void exceptionThrown(final Exception exception) {
			LOGGER.error("General exception : " + exception);
		}
	};

	static {
		try {
			SerializationUtilities.setTransientFields(File.class, Url.class, IndexableInternet.class, IndexableEmail.class,
					IndexableFileSystem.class, IndexableColumn.class, IndexableTable.class, IndexContext.class);
		} catch (Exception e) {
			LOGGER.error("Exception setting the transient fields : ", e);
		}
	}

	public static String serialize(final Object object) {
		try {
			SerializationUtilities.setTransientFields(object.getClass(), new ArrayList<Class<?>>());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLEncoder xmlEncoder = new XMLEncoder(byteArrayOutputStream);
			xmlEncoder.setExceptionListener(EXCEPTION_LISTENER);
			xmlEncoder.writeObject(object);
			xmlEncoder.flush();
			xmlEncoder.close();
			return byteArrayOutputStream.toString(IConstants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		}
		return null;
	}

	public static void setTransientFields(Class<?>... classes) {
		List<Class<?>> doneClasses = new ArrayList<Class<?>>();
		for (Class<?> klass : classes) {
			setTransientFields(klass, doneClasses);
		}
	}

	public static void setTransientFields(final Class<?> klass, final List<Class<?>> doneClasses) {
		if (doneClasses.contains(klass)) {
			return;
		}
		doneClasses.add(klass);
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(klass);
		} catch (IntrospectionException e) {
			LOGGER.error("Exception setting the transient fields in the serializer : ", e);
			return;
		}
		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
		for (PropertyDescriptor pd : propertyDescriptors) {
			String fieldName = pd.getName();
			try {
				Field field = SerializationUtilities.getField(klass, fieldName);
				if (field == null) {
					continue;
				}
				Transient transientAnnotation = field.getAnnotation(Transient.class);
				boolean isTransient = Modifier.isTransient(field.getModifiers());
				if (transientAnnotation != null || isTransient) {
					field.setAccessible(Boolean.TRUE);
					pd.setValue("transient", Boolean.TRUE);
				}
				if (Collection.class.isAssignableFrom(field.getType())) {
					Type parameterizedType = field.getGenericType();
					if (parameterizedType != null) {
						if (ParameterizedType.class.isAssignableFrom(parameterizedType.getClass())) {
							Type[] typeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
							for (Type typeArgument : typeArguments) {
								if (ParameterizedType.class.isAssignableFrom(typeArgument.getClass())) {
									Type rawType = ((ParameterizedType) typeArgument).getRawType();
									if (Class.class.isAssignableFrom(rawType.getClass())) {
										setTransientFields((Class<?>) rawType, doneClasses);
									}
								}
							}
						}
					}
				}
			} catch (SecurityException e) {
				LOGGER.error("Exception setting the transient fields in the serializer : ", e);
			}
		}
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Class<?> fieldClass = field.getType();
			setTransientFields(fieldClass, doneClasses);
		}
		Class<?> superKlass = klass.getSuperclass();
		if (superKlass != null && !Object.class.getName().equals(superKlass.getName())) {
			setTransientFields(superKlass, doneClasses);
		}
	}

	public static Object deserialize(final String xml) {
		byte[] bytes = null;
		try {
			bytes = xml.getBytes(IConstants.ENCODING);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			XMLDecoder xmlDecoder = new XMLDecoder(byteArrayInputStream);
			xmlDecoder.setExceptionListener(EXCEPTION_LISTENER);
			return xmlDecoder.readObject();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		} catch (Exception e) {
			LOGGER.error("Exception de-serialising object : " + xml, e);
		}
		return null;
	}

	public static Object clone(final Object object) {
		return SerializationUtilities.deserialize(SerializationUtilities.serialize(object));
	}

	/**
	 * Gets a field in the class or in the hierarchy of the class.
	 * 
	 * @param klass
	 *            the original class
	 * @param name
	 *            the name of the field
	 * @return the field in the object or super classes of the object
	 */
	public static Field getField(final Class<?> klass, final String name) {
		Field field = null;
		try {
			field = klass.getDeclaredField(name);
		} catch (Exception t) {
			t.getCause();
		}
		if (field == null) {
			Class<?> superClass = klass.getSuperclass();
			if (superClass != null) {
				field = getField(superClass, name);
			}
		}
		return field;
	}

	private SerializationUtilities() {
	}

}
