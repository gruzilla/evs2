package framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.management.ServiceNotFoundException;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Property;

import framework.annotations.*;

@Service
public class DefaultResourceService extends RESTService {

	private Class<?> modelClass = null;
	
	public DefaultResourceService() {
		// nothing
	}
	
	void setModelClass(String modelClassName) throws ServiceNotFoundException {
		try {
			modelClass = Class.forName(modelClassName);
		} catch (ClassNotFoundException e) {
			throw new ServiceNotFoundException("Could not create a default-service for model "+Context.getServiceName());
		}
	}
	
	Class<?> getModelClass() {
		return modelClass;
	}
	
	@Get(regexp="/?([^/]+)?")
	public Object read() {
		String field = "";
		String value = "";
		int offset = 0;
		
		if (Context.getMatcher().find(offset) && Context.getMatcher().group(1) != null) {
			field = Context.getMatcher().group(1);
			
			offset += Context.getMatcher().end();
			if (Context.getMatcher().find(offset) && Context.getMatcher().group(1) != null) {
				value = Context.getMatcher().group(1);
			}
		}
		
		logger.debug("identified value: "+value+" and field: "+field);
		
		if (Context.getIdentity() != null || value.length() == 0) {
			
			if (Context.getIdentity() == null) {
				beforeSelect();
				List<Object> list = Context.getManager().getSession().createCriteria(modelClass).list();
				afterSelect();
				return list;
			} else {
				beforeSelect();
				Object ret = Context.getManager().find(modelClass, Context.getIdentity());
				afterSelect();
				return ret;
			}
		} else {
			value = value.replace('*', '%');
			
			beforeSelect();
			Criteria criteria = Context.getManager().getSession().createCriteria(modelClass);
			
			criteria.add(Property.forName(field).like(getCorrectType(modelClass, field, value)));
			
			List<Object> list = criteria.list();
			afterSelect();
			return list;
		}
	}
	
	@Post
	public Object create() {
		Object newObject = Context.getData();
		if (newObject == null) return "NO OBJECT!";
		
		try {
			beforeInsert();
			Context.getManager().getTransaction().begin();
			Context.getManager().persist(newObject);
			Context.getManager().getTransaction().commit();
			afterInsert();
		} catch(Exception e) {
			Context.getManager().getTransaction().rollback();
			afterFailedInsert();
		}
		return newObject;
	}
	
	@Put
	public Object update() {
		Object newObject = Context.getData();
		Object originalObject;
		if (newObject == null) return "NO OBJECT!";
		
		try {
			Method idgetter = newObject.getClass().getMethod("getId");
			
			originalObject = Context.getManager().find(modelClass, idgetter.invoke(newObject));
			
			if (originalObject == null) return "NO OBJECT";
			
			Method[] methods = newObject.getClass().getMethods();
			String[] forbidden = {"getClass"};
			for (Method m : methods) {
				if (!ArrayUtils.contains(forbidden, m.getName())
						&& m.getName().startsWith("get")) {
					
					Method s;
					try {
						s = originalObject.getClass().getMethod(
							m.getName().replaceFirst("get", "set"),
							m.getReturnType());
					} catch (NoSuchMethodException e) {
						return "NO MATCHING SETTER FOR "+m.getName();
					}
					Object data = m.invoke(newObject);
					if (data != null) {
						s.invoke(originalObject, data);
					}
				}
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! sec";
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! nsm";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! ill";
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! illa";
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! invo";
		} catch (NullPointerException e) {
			e.printStackTrace();
			return "OBJECT COULD NOT BE IDENTIFIED! null";
		}
		
		try {
			beforeUpdate();
			Context.getManager().getTransaction().begin();
			Context.getManager().merge(originalObject);
			Context.getManager().getTransaction().commit();
			afterUpdate();
		} catch(Exception e) {
			Context.getManager().getTransaction().rollback();
			afterFailedUpdate();
		}
		return originalObject;
	}
	
	@Delete(regexp="/?([^/]+)?")
	public String delete() {
		
		if (Context.getIdentity() == null) return "NO IDENTITY!";
		
		Object originalObject = Context.getManager().find(modelClass, Context.getIdentity());
		if (originalObject == null) return "NOT FOUND";
		
		try {
			Context.getManager().getTransaction().begin();
			Context.getManager().remove(originalObject);
			Context.getManager().getTransaction().commit();
			afterDelete();
		} catch(Exception e) {
			Context.getManager().getTransaction().rollback();
			afterFailedDelete();
			return "ERROR";
		}
		
		return "OK";
	}
	
	protected Object getCorrectType(Class<?> modelClass, String fieldName, String value) {
		try {
			if (modelClass.getMethod("get"+
					fieldName.substring(0,1).toUpperCase()+
					fieldName.substring(1)
			).getReturnType() == int.class) {
				return new Integer(value);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return value;
	}
}
