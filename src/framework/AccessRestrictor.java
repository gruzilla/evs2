package framework;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map.Entry;

import framework.annotations.SecurityMode;

public class AccessRestrictor {

	private SecurityMode mode;
	private HashMap<InetAddress, Integer> networks = new HashMap<InetAddress, Integer>();

	public AccessRestrictor(SecurityMode mode, String[] networks) {
		this.mode = mode;
		for (String network : networks) {
			if (network.indexOf('/') == -1) {
				throw new InvalidParameterException("netmask must contain a slash!");
			}
			try {
				this.networks.put(
					InetAddress.getByName(network.substring(0, network.indexOf('/')))
				,	Integer.parseInt(network.substring(network.indexOf('/')+1))
				);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
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
		
		for (Entry<InetAddress, Integer> entry : networks.entrySet()) {
			if (inNetMask(ipToInt(remote), ipToInt(entry.getKey()), entry.getValue())) {
				switch (mode) {
				case WHITELIST:
					return true;
				case BLACKLIST:
					return false;
				}
			}
		}
		return false; // if no addresses are given, we block everything
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
