package framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import framework.annotations.*;
import framework.exceptions.*;

public class ServiceInvoker {

	private static final String ENCRYPTION_ID_HEADER_NAME = "ENCRYPTIONID";
	private HttpServletRequest request;
	private Object response;
	private URL url;
	private String resourceName;
	private RESTService service;
	private Method method;
	private String methodParameters;
	private static Logger logger = Logger.getLogger(ServiceInvoker.class);

	public ServiceInvoker() {
	}
	
	public void setRequest(HttpServletRequest request) throws RenderException, ServiceNotFoundException, MethodNotFoundException, SecurityException {
		this.request = request;
		
		// 1.1. Parse URL
		try {
			url = new URL(request.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new RenderException("PARSE ERROR: URL could not be parsed!");
		}
		
		// 1.2. Find requirements to identify which service to take and
		// which method
		try {
			readContext(request);
		} catch (IOException e) {
			throw new RenderException("PARSE ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		
		// 2. try to find an identity from the url (this can't be done
		// before, becaue of composite keys, which must be read
		// reflectively from the model)
		readIdentityFromURL();
		
		// 3. get service
		service = findService(request);
		
		// 4. read body
		String body;
		try {
			body = readBody(request);
		} catch (RequestNotParsedException e) {
			throw new RenderException("READ ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		
		// 4. check security
		try {
			body = checkEncryption(service, body, request);
		} catch (EncryptionRequiredException e1) {
			throw new SecurityException("Encryption required!");
		} catch (AddressNotAllowedException e1) {
			throw new SecurityException("Address not allowed!");
		} catch (CryptException e1) {
			throw new SecurityException("En/Decryptiong did not work!");
		}
		
		// 5. Parse body
		try {
			String contentType = request.getContentType();
			if (contentType == null) contentType = "";
			logger.debug("interpreting body ...");
			Marshaller.readBody(body, contentType);
			logger.debug("DONE");
			
		} catch (RequestNotParsedException e) {
			throw new RenderException("PARSE ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		
		// 6. get method
		method = findMethod(service.getClass(), request.getMethod());
		
		// 7. Parse Accept-Header
		detectRequestedResponseType();
		
		// 8. let user invoke and store response
	}

	private String readBody(HttpServletRequest request) throws RequestNotParsedException {
		// read data
		String body = "";
		try {
			BufferedReader bodyReader = request.getReader();
			String line;
			while ((line = bodyReader.readLine() ) != null) {
				body += line+"\n";
			}
		} catch (IOException e) {
			throw new RequestNotParsedException("Unknown request could not be parsed! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		
		logger.debug("The client sent the following data:\n"+body+"\n");
		
		return body;
	}

	private String checkEncryption(RESTService service, String body, HttpServletRequest request) throws EncryptionRequiredException, AddressNotAllowedException, CryptException {
		if (service.getClass().isAnnotationPresent(Encryption.class)) {
			Encryption crypt = service.getClass().getAnnotation(Encryption.class);
			
			// TODO: check if request-ip-address is black/white-listed by annotation
			AccessRestrictor acl = new AccessRestrictor(crypt.mode(), crypt.netmask());
			if (!acl.checkAddress(request.getRemoteAddr())) {
				throw new AddressNotAllowedException();
			}
			
			Crypt cryptor = new Crypt();
			cryptor.setIdentity(request.getHeader(ENCRYPTION_ID_HEADER_NAME));
			cryptor.setBody(body);
			if (cryptor.isBodyEncrypted()) {
				throw new EncryptionRequiredException("Service requires encryption, but request was not encrypted!");
			}
			// TODO: decrypt payload, save it in payload 
			return cryptor.decrypt();
		}
		
		return body;
	}

	private Method findMethod(Class<? extends RESTService> clazz, String httpMethod) throws MethodNotFoundException {
		
		logger.debug("method: "+httpMethod);
		logger.debug("class to search on: "+clazz);
		
		HashMap<Method,String> ret = new HashMap<Method,String>();
		for (Method method : clazz.getMethods()) {
			logger.debug("checking method "+method.getName());
			for (Annotation anno : method.getAnnotations()) {
				String annoClassName = anno.toString().split("\\(")[0].substring(1);
				logger.debug("trying to equal on method '"+method.getName()+"': "+annoClassName);
				if (httpMethod.equals("GET") && annoClassName.equals(Get.class.getName())) {
					ret.put(method, ((Get)anno).regexp());
				} else if (httpMethod.equals("POST") && annoClassName.equals(Post.class.getName())) {
					ret.put(method, ((Post)anno).regexp());
				} else if (httpMethod.equals("PUT") && annoClassName.equals(Put.class.getName())) {
					ret.put(method, ((Put)anno).regexp());
				} else if (httpMethod.equals("DELETE") && annoClassName.equals(Delete.class.getName())) {
					ret.put(method, ((Delete)anno).regexp());
				}
			}
		}
		
		for (Map.Entry<Method, String> entry : ret.entrySet()) {
			
			if (entry.getValue().equals("")) {
				return entry.getKey();
			}
			
			logger.debug("trying to match |"+methodParameters+"| on #"+entry.getValue()+"#");
			
			Pattern p = Pattern.compile(entry.getValue());
			Matcher matcher = p.matcher(methodParameters);
			
			if (matcher.matches() || matcher.find(0)) {
				Context.setMatcher(matcher);
				
				logger.debug("found a method!");
				
				return entry.getKey();
			}
		}
		
		throw new MethodNotFoundException("There were "+ret.size()+" possible methods, but none of them matched "+methodParameters);
	}

	private void detectRequestedResponseType() {
		if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("text/xml")) {
			Context.setToXML();
		} else /* if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("application/json")) */ {
			Context.setToJSON();
		}
	}

	public Object getResponse() {
		return response;
	}

	/**
	 * finds the service, and invokes the method on it
	 * 
	 * @param request
	 * @param out
	 * @param annotation
	 * @throws IOException 
	 * @throws MethodCouldNotBeInvokedException 
	 * @throws ServiceNotFoundException 
	 */
	public Object invoke() throws IOException, MethodCouldNotBeInvokedException, ServiceNotFoundException {
		
		// before a method can be invoked, we have to read the context
		
		try {
			if (service == null) {
				throw new MethodCouldNotBeInvokedException("Service not found!");
			}
			
			if (method == null) {
				throw new MethodCouldNotBeInvokedException("Method not found!");
			}
			
			response = method.invoke(service);
			return response;
			
		} catch (IllegalArgumentException e) {
			throw new MethodCouldNotBeInvokedException("Illegal arguments! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (IllegalAccessException e) {
			throw new MethodCouldNotBeInvokedException("Illegal access! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (InvocationTargetException e) {
			Throwable e2 = e.getTargetException();
			if (e2 instanceof NumberFormatException) {
				throw new MethodCouldNotBeInvokedException("A given parameter was not numeric!");
			}
			throw new MethodCouldNotBeInvokedException("Invocation failed!\n"+
					e2.getMessage()+"\n"+e2.getCause()+"\n"+StringUtils.join(e2.getStackTrace(), "\n")+
					"\n\nROOT EXCEPTION:\n\n\n"+
					e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(), "\n"));
		}
	}
	
	private void readContext(HttpServletRequest request) throws IOException {
		logger.debug("checking context: "+url.getPath());
		Vector<String> urlparts = new Vector<String>(Arrays.asList(url.getPath().split("/")));
		if (urlparts.size() == 0) return;
		// remove empty start
		urlparts.remove(0);
		
		if (urlparts.size() == 0) return;
		// get resource name
		resourceName = urlparts.remove(0);
		
		Context.setServiceName(resourceName);
		
		methodParameters = StringUtils.join(urlparts.toArray(), "/");
		
	}
	
	private void readIdentityFromURL() {
		
		Context.setId(null);
		Class<?> modelClass = null;
		
		try {
			modelClass = Class.forName(ServiceAndModelMapper.getModelsPackageName()+"."+resourceName);
		} catch (ClassNotFoundException e1) {
			// we don't have a model, so we can savely return
			return;
		}
		
		Vector<String> urlparts = new Vector<String>(Arrays.asList(methodParameters.split("/")));
		
		// now try to find an identity
		if (urlparts.size() == 0) {
			return; // no id given
		}
		
		logger.debug("readIdentity: 0: |"+urlparts.get(0)+"| size: "+urlparts.size());
		
		if (urlparts.get(0).equals("composite") && urlparts.size() > 1) {
			// the client tries to retrieve a composite-key
			Class<?> pk;
			try {
				Method idgetter = modelClass.getMethod("getId");
				
				pk = idgetter.getReturnType();
			} catch (Exception e) {
				// error finding the fitting field
				logger.debug("readIdentity: type not found: "+e+" in "+modelClass);
				return;
			}
			Object id;
			try {
				id = pk.newInstance();
			} catch (Exception e) {
				// error instanciating the type
				logger.debug("readIdentity: no instance created");
				return;
			}
			
			String[] parameters = urlparts.get(1).split("&");
			
			for (String parameter : parameters) {
				String paramName = parameter.split("=")[0];
				String paramValue = parameter.split("=")[1];
				
				Method setter;
				try {
					Method getter = pk.getMethod("get"+paramName);
					setter = pk.getMethod("set"+paramName, getter.getReturnType());
				} catch (Exception e) {
					// no setter found for this id-field
					logger.debug("readIdentity: no setter found for "+paramName+" in "+pk);
					return;
				}
				
				if (setter == null) return;
				
				try {
					if (setter.getParameterTypes()[0] == int.class) {
						setter.invoke(id, new Integer(paramValue));
					} else {
						setter.invoke(id, paramValue);
					}
				} catch (Exception e) {
					logger.debug("readIdentity: parameter-type could not be identified (currently only int and String are supported)");
					return;
				}
			}
			
			Context.setId(id);
		} else {
			try {
				int id = new Integer(urlparts.get(0));
				Context.setId(id);
			} catch (NumberFormatException e) {
				// this is no normal id, this must be a search-request
				return;
			}
		}
	}

	/**
	 * finds a service out of the request
	 * 
	 * @param request
	 * @return
	 * @throws ServiceNotFoundException
	 * @throws MethodCouldNotBeInvokedException 
	 */
	private RESTService findService(HttpServletRequest request) throws ServiceNotFoundException, RenderException {
		
		logger.debug("finding service ...");
		
		ArrayList<Class<? extends RESTService>> services = ServiceAndModelMapper.getInstance().getServiceList(resourceName);
		
		if (services == null || services.size() == 0) {
			throw new ServiceNotFoundException("Service not found!", resourceName);
		}
		logger.debug("DONE ("+resourceName+")");
		
		try {
			logger.debug("creating and returning service instance");
			Class<?> clazz = services.get(0);
			RESTService ret;
			
			// Interceptor before instanciation
			Method beforeInstanciation = clazz.getMethod("beforeInstanciation");
			if (beforeInstanciation != null) {
				beforeInstanciation.invoke(clazz);
			}
			
			ret = (RESTService)clazz.newInstance();
			if (clazz == DefaultResourceService.class || clazz.getSuperclass() == DefaultResourceService.class) {
				((DefaultResourceService)ret).setModelClass(ServiceAndModelMapper.getModelsPackageName()+"."+resourceName);
			}
			
			// Interceptor after instanciation
			ret.afterInstanciation();
			
			return ret;
		} catch (InstantiationException e) {
			throw new ServiceNotFoundException("Service could not be instantiated!", resourceName);
		} catch (IllegalAccessException e) {
			throw new ServiceNotFoundException("Service could not be instantiated!", resourceName);
		} catch (Exception e) {
			throw new ServiceNotFoundException("Service could not be instantiated: "+e.getMessage(), resourceName);
		}
	}
}
