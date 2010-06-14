package framework;

import java.io.IOException;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.EntityManagerImpl;

import framework.exceptions.*;

/**
 * RequestHandler Servlet
 */
public class RequestHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PERSISTENCE_UNIT_NAME = "evs";
	private ServiceInvoker invoker;
	private EntityManagerImpl manager;
	   
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RequestHandler() {
		super();
		
		manager = (EntityManagerImpl)Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME).createEntityManager();
		Context.setManager(manager);
		
		invoker = new ServiceInvoker();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			invoker.setRequest(request);
			invoker.invoke();
			Responder responder = new Responder(invoker.getResponse(), request.getHeader("Accept"));
			responder.respond(response);
		} catch (MethodCouldNotBeInvokedException e) {
			response.getWriter().println("INVOKATION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (ServiceNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: "+e.getService()+"\n\n"+e.getMessage());
		} catch (MethodNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: \n\n"+e.getMessage());
		} catch (RenderException e) {
			response.getWriter().println("RENDER ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (SecurityException e) {
			response.getWriter().println("ENCRYPTION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			invoker.setRequest(request);
			invoker.invoke();
			Responder responder = new Responder(invoker.getResponse(), request.getHeader("Accept"));
			responder.respond(response);
		} catch (MethodCouldNotBeInvokedException e) {
			response.getWriter().println("INVOKATION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (ServiceNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: "+e.getService());
		} catch (MethodNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: \n\n"+e.getMessage());
		} catch (RenderException e) {
			response.getWriter().println("RENDER ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (SecurityException e) {
			response.getWriter().println("ENCRYPTION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
		//PrintWriter out = response.getWriter();
		//response.getWriter().println("You requested: "+request.getRequestURI()+" ("+request.getMethod()+")");
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			invoker.setRequest(request);
			invoker.invoke();
			Responder responder = new Responder(invoker.getResponse(), request.getHeader("Accept"));
			responder.respond(response);
		} catch (MethodCouldNotBeInvokedException e) {
			response.getWriter().println("INVOKATION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (ServiceNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: "+e.getService());
		} catch (MethodNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: \n\n"+e.getMessage());
		} catch (RenderException e) {
			response.getWriter().println("RENDER ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (SecurityException e) {
			response.getWriter().println("ENCRYPTION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			invoker.setRequest(request);
			invoker.invoke();
			Responder responder = new Responder(invoker.getResponse(), request.getHeader("Accept"));
			responder.respond(response);
		} catch (MethodCouldNotBeInvokedException e) {
			response.getWriter().println("INVOKATION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (ServiceNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: "+e.getService());
		} catch (MethodNotFoundException e) {
			response.getWriter().println("DISPATCH ERROR: Service not found: \n\n"+e.getMessage());
		} catch (RenderException e) {
			response.getWriter().println("RENDER ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		} catch (SecurityException e) {
			response.getWriter().println("ENCRYPTION ERROR: "+e.getMessage()+"\n"+e.getCause()+"\n"+StringUtils.join(e.getStackTrace(),"\n"));
		}
	}
	
	public void destroy() {
		manager.close();
	}
	
}
