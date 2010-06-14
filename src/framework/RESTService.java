package framework;

import org.apache.log4j.Logger;

abstract public class RESTService {
	protected static Logger logger = Logger.getLogger(RESTService.class);
	
	public static void beforeInstanciation() {
		logger.info("Before instanciation");
	}
	
	public void afterInstanciation() {
		logger.info("After instanciation");
	}
	
	public void beforeInvokation() {
		logger.info("Before invokation");
	}
	
	public void afterInvokation() {
		logger.info("After invokation");
	}
	
	public void beforeInsert() {
		logger.info("Before insert");
	}
	
	public void afterInsert() {
		logger.info("After insert");
	}
	
	public void afterFailedInsert() {
		logger.info("After failed insert");
	}
	
	public void beforeDelete() {
		logger.info("Before delete");
	}

	public void afterDelete() {
		logger.info("After delete");
	}
	
	public void afterFailedDelete() {
		logger.info("After failed delete");
	}
	
	public void beforeUpdate() {
		logger.info("Before update");
	}
	
	public void afterUpdate() {
		logger.info("After update");
	}
	
	public void afterFailedUpdate() {
		logger.info("After failed update");
	}
	
	public void beforeSelect() {
		logger.info("Before select");
	}
	
	public void afterSelect() {
		logger.info("After select");
	}
}
