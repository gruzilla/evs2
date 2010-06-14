package framework.exceptions;

public class ServiceNotFoundException extends Exception {

	private static final long serialVersionUID = 5650216591219543299L;
	private String service;
	
	public ServiceNotFoundException(String msg, String resourceName) {
		super(msg);
		setService(resourceName);
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getService() {
		return service;
	}
	
}
