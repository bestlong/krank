package org.crank.crud.criteria;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Example extends Group {
    protected final Logger log = LogManager.getLogger( this.getClass() );
	private Object example;
	private Set<String> excludedProperties = new HashSet<String>();
	{
		excludedProperties.add("class");
	}
	private boolean generated = false;
	private boolean excludeNulls = true;
	private Operator operator = Operator.EQ;
	private boolean excludeZeroes = false;
	private String qualifier = "";

	private Example (Object object) {
		this.example = object;
	}
	private Example (Object object, String name) {
		this.example = object;
		this.qualifier  = name + ".";
	}

	public Object getExample() {
		return example;
	}

	public void setExample(Object example) {
		this.example = example;
	}
	
	public Example excludeProperty(final String name) {
		excludedProperties.add(name);
		return this;
	}
	public Example excludeNone() {
		excludeNulls = false;
		return this;
	}
	public Example enableLike() {
		operator = Operator.LIKE;
		return this;
	}
	public Example excludeZeroes() {
		excludeZeroes = true;
		return this;
	}
	public Example enableContainsLike() {
		operator = Operator.LIKE_CONTAINS;
		return this;
	}
	public Example enableEndsLike() {
		operator = Operator.LIKE_END;
		return this;
	}
	public Example enableStartsLike() {
		operator = Operator.LIKE_START;
		return this;
	}
	public static Example createExample(Object example) {
		return new Example(example);
	}
	public static Example like(Object object) {
		Example example = new Example(object);
		example.enableContainsLike();
		example.excludeZeroes();
		return example;
	}

	
	@Override
	public Iterator<Criterion> iterator() {
		if (!generated) {
			generated = true;
			generate();
		}
		return super.iterator();
	}
	private void generate() {
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(example.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for(PropertyDescriptor pd : propertyDescriptors) {
				String name = pd.getName();
			
				
				if (excludedProperties.contains(name)) {
					log.debug("Excluding: " + name + " from the list of considered properties.");
					continue;
				}
				Object value = pd.getReadMethod().invoke(example, (Object[]) null);
				boolean primitive = pd.getPropertyType().isPrimitive();
				String className = pd.getPropertyType().getName();

				if(excludeNulls && value != null) {
					log.debug("Including: " + name + " of class: " + className + " with value: " + value + " is primitive: " + primitive);
				} else {
					log.debug("Excluding: " + name + " of class: " + className + " becaues of null value");
				}

				boolean isNull = value == null;
				boolean isString = value instanceof String;
				if (!primitive) {
					handleNonPrimitives(qualifier + name, value, className, isNull, isString);
				} else {
					handlePrimitives(qualifier + name, value, className);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException ("Unable to use example object " + example, ex);
		}
	}
	
	private void handlePrimitives(String name, Object value, String className) {
		if (!"boolean".equals(className)) {
			if (excludeZeroes) {
				Number number = (Number) value;
				if (number.intValue()!=0) {
					this.eq(name, value);	
				} else {
					log.debug("Excluding primitive value: " + value + " for property named: " + name + " of class: " + className);
				}
			} else {
				this.eq(name, value);
			}
		} else {
			this.eq(name, value);
		}
	}
	private void handleNonPrimitives(String name, Object value, String className, boolean isNull, boolean isString) {
		if (className.startsWith("java")) {
			handleBasicJavaTypes(name, value, isNull, isString);
		} else {
			handleCustomJavaBeans(name, value, isNull);
		}
	}
	private void handleCustomJavaBeans(String name, Object value, boolean isNull) {
		if (!isNull) {
			Example example = new Example(value, name);
			example.excludedProperties = this.excludedProperties;
			example.excludeNulls = this.excludeNulls;
			example.junction = this.junction;
			example.excludeZeroes = this.excludeZeroes;
			example.operator = this.operator;
			this.criteria.add(example);
		} else {
			if (!excludeNulls) {
				this.eq(name, value);
			}
		}
	}
	private void handleBasicJavaTypes(String name, Object value, boolean isNull, boolean isString) {
		if (!isNull && !isString) {
			this.eq(name, value);
		} else if (isString && !isNull) {
			this.add(name, this.operator, value);
		} else {
			if (!this.excludeNulls) {
				if (isString) {
					this.add(name, this.operator, value);
				} else {
					this.eq(name, value);
				}
			}
		}
	}
	@Override
	public String toString() {
		if (!generated) {
			generated = true;
			generate();
		}
		return super.toString();
	}
	@Override
	public int size() {
		if (!generated) {
			generated = true;
			generate();
		}
		return super.size();
	}	
}
