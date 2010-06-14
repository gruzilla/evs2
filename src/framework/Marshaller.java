package framework;

import org.apache.log4j.Logger;

import framework.exceptions.RequestNotParsedException;

abstract class Marshaller {
	
	private static Logger logger = Logger.getLogger(Marshaller.class);
	
	/**
	 * reads the payload from the request and saves it using
	 * Context.setRawData() and Context.setData()
	 * 
	 * @param body
	 * @param contentType
	 * @throws RequestNotParsedException
	 */
	public static void readBody(String body, String contentType) throws RequestNotParsedException {
		Context.resetData();
		Context.setRawData(body);
		
		DataFormatContext marshaller = null;
		
		if (contentType.startsWith("application/json")) {
			logger.debug("marshalling json");
			marshaller = new DataFormatContext(new JSONMarshaller());
			Context.setToJSON();
		} else if (contentType.startsWith("application/xml") || contentType.startsWith("text/xml")) {
			logger.debug("marshalling xml");
			marshaller = new DataFormatContext(new XMLMarshaller());
			Context.setToXML();
		} else {
			Context.setToUnknown();
		}
		
		if (Context.getContext() != Context.UNKNOWN) {
			Context.setData(marshaller.unmarshallStrategy(body));
		}
	}
}
