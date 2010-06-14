package framework.exceptions;

public class RequestNotParsedException extends Exception {

	private static final long serialVersionUID = -6906176444689636226L;

	public RequestNotParsedException(String msg) {
		super(msg);
	}

}
