package framework;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import framework.exceptions.RequestNotParsedException;

public class JSONMarshaller implements IMarshaller {

	@Override
	public String marshall(Object object) {
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
		xstream.setMode(XStream.NO_REFERENCES);
		ServiceAndModelMapper.registerXStreamAliases(xstream);
		return xstream.toXML(object);
	}

	@Override
	public Object unmarshall(String serialized) throws RequestNotParsedException {
		try {
			XStream xstream = new XStream(new JettisonMappedXmlDriver());
			xstream.setMode(XStream.NO_REFERENCES);
			ServiceAndModelMapper.registerXStreamAliases(xstream);
			
			return xstream.fromXML(serialized);
		} catch (Exception e) {
			throw new RequestNotParsedException("JSON-Request could not be mapped! "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
	}

}
