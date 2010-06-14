package framework;

import framework.exceptions.RequestNotParsedException;

public interface IMarshaller {
		public String marshall(Object object);
		public abstract Object unmarshall(String serialized) throws RequestNotParsedException;
}
