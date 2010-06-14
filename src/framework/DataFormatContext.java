package framework;

import framework.exceptions.RequestNotParsedException;

public class DataFormatContext {
	private IMarshaller marshaller;

	public DataFormatContext(IMarshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	public String marshallStrategy(Object object) {
		return marshaller.marshall(object);
	}
	
	public Object unmarshallStrategy(String serialized) throws RequestNotParsedException {
		return marshaller.unmarshall(serialized);
	}

}
