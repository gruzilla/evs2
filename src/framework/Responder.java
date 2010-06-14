package framework;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import framework.exceptions.RenderException;

public class Responder {
	private Object response;
	private DataFormatContext marshaller;
	private String mimeType;

	public Responder(Object response, String responseMimeType) {
		this.response = response;
		if (responseMimeType == null) responseMimeType = "";
		mimeType = responseMimeType;
		if (responseMimeType.endsWith("/xml")) {
			marshaller = new DataFormatContext(new XMLMarshaller());
		} else {
			marshaller = new DataFormatContext(new JSONMarshaller());
		}
	}
	
	public String render() throws RenderException {
		if (response == null || response.equals("")) return "";
		
		if (response.getClass().isArray()) {
			return renderList();
		} else {
			return renderPojo();
		}
	}
	
	private String renderList() {
		return marshaller.marshallStrategy(response);
	}
	
	private String renderPojo() {
		return marshaller.marshallStrategy(response);
	}

	public void respond(HttpServletResponse response2) throws RenderException {
		response2.setCharacterEncoding("UTF-8");
		response2.setContentType(mimeType);
		
		try {
			response2.getWriter().print(render());
		} catch (IOException e) {
			throw new RenderException("IOException in Responder");
		}
	}
}
