package framework.annotations;

import java.lang.annotation.*;

/**
 * This annotation defines if the annotated service has to use encryption
 * or not. The mode in which access is allowed or forbidden can be
 * configured using the mode parameter which can be WHITELIST or BLACKLIST
 * 
 * @author ma
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Encryption {
	String netmask() default "192.168.0.0/24";
	SecurityMode mode() default SecurityMode.WHITELIST;
}
