package framework;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

import framework.annotations.SecurityMode;

public class AccessRestrictor {

	private SecurityMode mode;
	private InetAddress network;
	private int netmask = 0;

	public AccessRestrictor(SecurityMode mode, String netmask) {
		this.mode = mode;
		if (netmask.indexOf('/') == -1) {
			throw new InvalidParameterException("netmask must contain a slash!");
		}
		try {
			network = InetAddress.getByName(netmask.substring(0, netmask.indexOf('/')));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

	public boolean checkAddress(String remoteAddr) {
		InetAddress remote = null;
		try {
			remote = InetAddress.getByName(remoteAddr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (remote == null) return false;
		
		switch (mode) {
		case WHITELIST:
			return inNetMask(ipToInt(remote), ipToInt(network), netmask);
		case BLACKLIST:
			return !inNetMask(ipToInt(remote), ipToInt(network), netmask);
		}
		return false;
	}
	
	public boolean inNetMask(int address, int network, int netmask) {
		/*
		0 = (
				cast(@address as int) ^ cast(@network as int))
				&
				~(power(2, 32 - @netmask) - 1)
			)
		 */
		return 0 == ((address ^ network) & ~((int)Math.pow(2, 32 - netmask) - 1));
	}
	
	public static int ipToInt(InetAddress ipAddr)
	{
		int compacted = 0;
		byte[] bytes = ipAddr.getAddress();
		for (int i=0 ; i<bytes.length ; i++) {
			compacted += (bytes[i] * Math.pow(256,4-i-1));
		}
		return compacted;
	}
}
