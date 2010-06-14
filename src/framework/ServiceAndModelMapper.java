package framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import framework.annotations.*;
import framework.exceptions.ParseServicesException;

public class ServiceAndModelMapper {
	private static Logger logger = Logger.getLogger(ServiceAndModelMapper.class);
	private String servicePackage;
	private HashMap<String, ArrayList<Class<? extends RESTService>>> serviceMap;
	private HashMap<String, Class<?>> modelMap;
	private String modelPackage;

	public static ServiceAndModelMapper getInstance() {
		try {
			return new ServiceAndModelMapper();
		} catch (ParseServicesException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * creates a new service-parser. this class reads the classmap from the services-package
	 * 
	 * @throws ParseServicesException
	 */
	private ServiceAndModelMapper() throws ParseServicesException {
		File f = new File("framework.properties");
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(f));
		} catch (FileNotFoundException e1) {
//			throw new ParseServicesException("Configuration file not found!");
			p.setProperty("servicePackage", "services");
			p.setProperty("modelPackage", "models");
		} catch (IOException e1) {
//			throw new ParseServicesException("Configuration file could not be read!");
			p.setProperty("servicePackage", "services");
			p.setProperty("modelPackage", "models");
		}
		
		servicePackage = p.getProperty("servicePackage");
		modelPackage = p.getProperty("modelPackage");
		
		try {
			readServiceMap();
		} catch (ClassNotFoundException e) {
			throw new ParseServicesException("Services could not be found!");
		} catch (IOException e) {
			throw new ParseServicesException("Service could not be read!");
		}
		
		try {
			readModelsMap();
			
			createDefaultResourceService();
		} catch (ClassNotFoundException e) {
			throw new ParseServicesException("Models could not be found!");
		} catch (IOException e) {
			throw new ParseServicesException("Model could not be read!");
		}
	}

	/**
	 * Reads the models from the models-package and maps them into a HashMap.
	 * Afterwards it creates the default-resource-service.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readModelsMap() throws ClassNotFoundException, IOException {
		modelMap = new HashMap<String, Class<?>>();
		for (Class<?> model : getClasses(modelPackage)) {
			if (!model.isAnnotationPresent(javax.persistence.Entity.class)) continue;
			modelMap.put(model.getSimpleName(), model);
			logger.debug("Model found: "+model.getSimpleName());
		}
	}

	private void createDefaultResourceService() {
		for (Map.Entry<String, Class<?>>entry : modelMap.entrySet()) {
			if (serviceMap.get(entry.getKey().toLowerCase()) != null) continue;
			logger.debug("Registering default service: "+entry.getKey() + " ("+DefaultResourceService.class+")");
			ArrayList<Class<? extends RESTService>> classes = serviceMap.get(entry.getKey());
			if (classes == null) {
				serviceMap.put(entry.getKey(), new ArrayList<Class<? extends RESTService>>());
				classes = serviceMap.get(entry.getKey());
			}
			classes.add(DefaultResourceService.class);
		}
	}

	/**
	 * Reads the services from the service-package and maps their urls to the classes
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readServiceMap() throws ClassNotFoundException, IOException {
		
		serviceMap = new HashMap<String, ArrayList<Class<? extends RESTService>>>();
		
		Class<RESTService>[] services = (Class<RESTService>[])getClasses(servicePackage);
		
		for (Class<RESTService> service : services) {
			Class<Annotation> serviceClass = (Class<Annotation>) Class.forName("framework.annotations.Service");
			Service srv = (Service)service.getAnnotation(serviceClass);
			
			logger.debug("Registering user service: "+srv.url() + " ("+service+")");
			ArrayList<Class<? extends RESTService>> classes = serviceMap.get(srv.url());
			if (classes == null) {
				serviceMap.put(srv.url(), new ArrayList<Class<? extends RESTService>>());
				classes = serviceMap.get(srv.url());
			}
			classes.add(service);
		}
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static Class<?>[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}
	
	/**
	 * This method returns the Class corresponding to the mapped url
	 * @param url
	 * @return
	 */
	public ArrayList<Class<? extends RESTService>> getServiceList(String url) {
		return serviceMap.get(url);
	}
	
	/**
	 * This method applies all aliases on an XStream object for model serialization
	 * @param xstream
	 */
	public static void registerXStreamAliases(XStream xstream) {
		for (String className : getInstance().modelMap.keySet()) {
			xstream.alias(className.toLowerCase(), getInstance().modelMap.get(className));
		}
	}
	
	/**
	 * This method returns the models-package-name
	 * @return String
	 */
	public static String getModelsPackageName() {
		return getInstance().modelPackage;
	}
	
	public String toString() {
		String keys = "";
		for (String key : serviceMap.keySet()) {
			keys += key + ", ";
		}
		keys += "\n";
		for (String key : modelMap.keySet()) {
			keys += key + ", ";
		}
		return keys;
	}
}
