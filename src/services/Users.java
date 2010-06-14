package services;

import framework.DefaultResourceService;
import framework.annotations.*;


@Encryption
@Service(url="User")
public class Users extends DefaultResourceService {
	
	public void afterInstanciation() {
		logger.warn("USER's implementation of after Instanciation");
	}
	
	@Get(regexp="allstars")
	public String allstars() {
		return "You tried to fetch all stars!";
	}
}
