package framework;

import java.util.regex.Matcher;

import org.hibernate.ejb.EntityManagerImpl;

abstract public class Context {
	
	public static final int XML = 0;
	public static final int JSON = 1;
	public static final int UNKNOWN = 2;
	
	private static int context;
	private static String serviceName;
	private static String rawData;
	private static Object data;
	private static EntityManagerImpl dbManager;
	private static Matcher regExpMatcher;
	private static Object modelIdentity;
	
	private Context() {}
	
	static void resetData() {
		rawData = null;
		data = null;
	}
	
	public static Matcher getMatcher() {
		return regExpMatcher;
	}
	
	public static int getContext() {
		return context;
	}
	
	public static String getServiceName() {
		return serviceName;
	}
	
	public static String getRawData() {
		return rawData;
	}
	
	public static Object getData() {
		return data;
	}
	
	public static EntityManagerImpl getManager() {
		return dbManager;
	}
	
	public static Object getIdentity() {
		return modelIdentity;
	}
	
	
	
	
	static void setServiceName(String name) {
		serviceName = name;
	}
	
	static void setToXML() {
		context = XML;
	}
	
	static void setToJSON() {
		context = JSON;
	}
	
	static void setToUnknown() {
		context = UNKNOWN;
	}
	
	static void setRawData(String value) {
		rawData = value;
	}
	
	static void setData(Object value) {
		data = value;
	}

	static void setManager(EntityManagerImpl manager) {
		dbManager = manager;
	}

	static void setMatcher(Matcher matcher) {
		regExpMatcher = matcher;
	}

	static void setId(Object identity) {
		modelIdentity = identity;
	}
}
