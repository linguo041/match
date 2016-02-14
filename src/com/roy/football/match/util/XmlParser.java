package com.roy.football.match.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.roy.football.match.logging.ErrorType;

public class XmlParser{
	private final static XMLInputFactory _streamFactory = XMLInputFactory.newInstance();

	/**
	 * Parse the xml, and map to an object.
	 *
	 * @param inputStream    The Input Stream
	 * @param clazz          The Class
	 * @return               The Object
	 * @throws XmlParseException
	 */
	public static <T> T parseXmlToObject (InputStream inputStream, Class <T> clazz) throws XmlParseException {
		return parseXmlToObject(inputStream, clazz, null);
	}
	
	public static <T> T parseXmlToObject (Reader reader, Class <T> clazz) throws XmlParseException {
		return parseXmlToObject(reader, clazz, null);
	}

	/**
	 * Parse the xml, and map to an object.
	 *
	 * @param inputStream    The Input Stream
	 * @param clazz          The Class
	 * @param root           The root element matched object in the XML tree
	 * @return               The Object
	 * @throws XmlParseException
	 */
	public static <T> T parseXmlToObject (InputStream inputStream, Class <T> clazz, String root) throws XmlParseException {
		XMLStreamReader reader = null;
		T obj = null;

		try {
			reader = _streamFactory.createXMLStreamReader(inputStream);

			while (reader.hasNext()) {
				reader.next();
				int event = reader.getEventType();

				// parse staring from the root element.
				if (XMLStreamConstants.START_ELEMENT == event) {
					String startElement = reader.getLocalName();

					if (!StringUtil.isEmpty(root)) {
						if (root.equalsIgnoreCase(startElement)) {
							obj = parseElementWithChildren(reader, startElement, clazz);
							break;
						} else {
							continue;
						}
					} else {
						obj = parseElementWithChildren(reader, startElement, clazz);
						break;
					}
				}
			}
		} catch (XMLStreamException | IllegalAccessException
				| InstantiationException | IllegalArgumentException
				| InvocationTargetException | IntrospectionException
				| ClassNotFoundException e) {
			throw new XmlParseException(ErrorType.UnableParseXMLToObject, e,
					clazz.getClass().getSimpleName());
		}

		return obj;
	}
	
	public static <T> T parseXmlToObject (Reader r, Class <T> clazz, String root) throws XmlParseException {
		XMLStreamReader reader = null;
		T obj = null;

		try {
			reader = _streamFactory.createXMLStreamReader(r);

			while (reader.hasNext()) {
				reader.next();
				int event = reader.getEventType();

				// parse staring from the root element.
				if (XMLStreamConstants.START_ELEMENT == event) {
					String startElement = reader.getLocalName();

					if (!StringUtil.isEmpty(root)) {
						if (root.equalsIgnoreCase(startElement)) {
							obj = parseElementWithChildren(reader, startElement, clazz);
							break;
						} else {
							continue;
						}
					} else {
						obj = parseElementWithChildren(reader, startElement, clazz);
						break;
					}
				}
			}
		} catch (XMLStreamException | IllegalAccessException
				| InstantiationException | IllegalArgumentException
				| InvocationTargetException | IntrospectionException
				| ClassNotFoundException e) {
			throw new XmlParseException(ErrorType.UnableParseXMLToObject, e,
					clazz.getClass().getSimpleName());
		}

		return obj;
	}
	
	private static <T> T parseElementWithChildren(XMLStreamReader reader,
			String element, Class<T> clazz) throws XMLStreamException,
			IllegalAccessException, InstantiationException,
			IntrospectionException, IllegalArgumentException,
			InvocationTargetException, ClassNotFoundException {
		Stack<String> stack = new Stack<String>();
		Map<Method, List<Object>> listMap = new HashMap <Method, List<Object>> ();
		String temValue = null;
		T obj = clazz.newInstance();
		
		// parse attribute
		if (element != null && !element.isEmpty()) {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

			for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
				String attrName = property.getDisplayName();
				String attrValue = reader.getAttributeValue(null, attrName);

				if (attrValue != null && !attrValue.isEmpty()) {
					Method setter = property.getWriteMethod();
					Class<?> type = property.getPropertyType();

					if (String.class.isAssignableFrom(type)) {
						setter.invoke(obj, attrValue);
					} else if (Integer.class.isAssignableFrom(type)) {
						setter.invoke(obj, Integer.parseInt(attrValue));
					} else if (Long.class.isAssignableFrom(type)) {
						setter.invoke(obj, Long.parseLong(attrValue));
					} else if (Float.class.isAssignableFrom(type)) {
						setter.invoke(obj, Float.parseFloat(attrValue));
					} else if (Double.class.isAssignableFrom(type)) {
						setter.invoke(obj, Double.parseDouble(attrValue));
					} else if (Boolean.class.isAssignableFrom(type)) {
						setter.invoke(obj, Boolean.parseBoolean(attrValue));
					} else if (Date.class.isAssignableFrom(type)) {
						try {
							setter.invoke(obj, DateUtil.parseDateWithDataBase(attrValue));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		while (reader.hasNext()) {
			reader.next();
			int event = reader.getEventType();

			if (XMLStreamConstants.START_ELEMENT == event) {
				String startElement = reader.getLocalName();
				boolean isInstanceType = false;

				// from xml, check if the element has children elements;
				// to the object, check if the related property is user defined type.
				for (PropertyDescriptor property : getInstanceTypeProperties(clazz)) {
					if (startElement.equalsIgnoreCase(property.getDisplayName())) {
						Class <?> type = property.getPropertyType();
						Method setter = property.getWriteMethod();

						isInstanceType = true;
						Object child = parseElementWithChildren(reader, startElement, type);
						setter.invoke(obj, child);
						break;
					}
				}

				// generic type should not be the primary type!
				for (PropertyDescriptor property : getListTypeProperties(clazz)) {
					// support the plural as well
					String propertyName = property.getDisplayName();
					if (startElement.equalsIgnoreCase(propertyName)
							|| (startElement + "s").equalsIgnoreCase(propertyName)
							|| (startElement + "es").equalsIgnoreCase(propertyName)) {
						Method setter = property.getWriteMethod();
						ParameterizedType genericType = (ParameterizedType) property.getReadMethod().getGenericReturnType();
						Type [] types = genericType.getActualTypeArguments();

						if (types.length == 1) {
							List <Object> list = listMap.get(setter);
							if (list == null) {
								list = new ArrayList<Object>();
								listMap.put(setter, list);
							}

							Class<?> listGenericClass = (Class<?>)types[0];
							Object child = parseElementWithChildren(reader, startElement, listGenericClass);
							list.add(child);
						}

						isInstanceType = true;
						break;
					}
				}

				// the root element and the elements with children won't be put into stack.
				if (!startElement.equals(element) && !isInstanceType) {
					stack.push(startElement);
				}
			} else if (XMLStreamConstants.CHARACTERS == event) {
				temValue = reader.getText();
			} else if (XMLStreamConstants.END_ELEMENT == event) {
				String endElement = reader.getLocalName();
				// stop parsing when meeting the end of the root element
				if (endElement.equals(element)) {
					break;
				}

				// retrieve the element with value and match the object property.
				if (endElement.equals(stack.pop()) && stack.isEmpty()) {
					for (PropertyDescriptor property : getStringTypeProperties(clazz)) {
						if (endElement.equalsIgnoreCase(property.getDisplayName())) {
							Method setter = property.getWriteMethod();
							setter.invoke(obj, temValue);
							break;
						}
					}
				}
			}
		}

		if (listMap.size() > 0) {
			for (Map.Entry<Method, List<Object>> entry : listMap.entrySet()) {
				Method setter = entry.getKey();
				List<Object> li = entry.getValue();
				setter.invoke(obj, li);
			}
		}

		return obj;
	}

	private static <T> List<PropertyDescriptor> getStringTypeProperties(
			Class<T> clazz) throws IntrospectionException {
		List<PropertyDescriptor> stringProperties = new ArrayList<PropertyDescriptor>();
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			Class<?> type = property.getPropertyType();
			if (String.class.isAssignableFrom(type)) {
				stringProperties.add(property);
			}
		}
		return stringProperties;
	}
	
	private static <T> List<PropertyDescriptor> getListTypeProperties(
			Class<T> clazz) throws IntrospectionException {
		List<PropertyDescriptor> collProperties = new ArrayList<PropertyDescriptor>();
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			Class<?> type = property.getPropertyType();
			if (List.class.isAssignableFrom(type)) {
				collProperties.add(property);
			}
		}
		return collProperties;
	}

	private static <T> List<PropertyDescriptor> getInstanceTypeProperties(
			Class<T> clazz) throws IntrospectionException {
		List<PropertyDescriptor> insProperties = new ArrayList<PropertyDescriptor>();
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			Class<?> type = property.getPropertyType();
			if (!String.class.isAssignableFrom(type)
					&& !Number.class.isAssignableFrom(type)
					&& !Boolean.class.isAssignableFrom(type)
					&& !Collection.class.isAssignableFrom(type)
					&& !type.isPrimitive()) {
				insProperties.add(property);
			}
		}

		return insProperties;
	}
}
