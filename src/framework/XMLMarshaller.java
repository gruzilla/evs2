package framework;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import framework.exceptions.RequestNotParsedException;

public class XMLMarshaller implements IMarshaller {

	@Override
	public String marshall(Object object) {
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		ServiceAndModelMapper.registerXStreamAliases(xstream);
		return xstream.toXML(object);
	}

	@Override
	public Object unmarshall(String serialized) throws RequestNotParsedException {
		try {
			XStream xstream = new XStream(new DomDriver());
			xstream.setMode(XStream.NO_REFERENCES);
			ServiceAndModelMapper.registerXStreamAliases(xstream);
			return xstream.fromXML(serialized);
		} catch (Exception e) {
			throw new RequestNotParsedException("JSON-Request could not be mapped! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		
		/**
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Context.setXMLData(builder.parse(new InputSource(new StringReader(serialized))));
			
		} catch (IOException e) {
			throw new RequestNotParsedException("XML-Request could not be parsed! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (ParserConfigurationException e) {
			throw new RequestNotParsedException("XML-Request could not be parsed! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (SAXException e) {
			throw new RequestNotParsedException("XML-Request could not be parsed! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		**/
	}

}
