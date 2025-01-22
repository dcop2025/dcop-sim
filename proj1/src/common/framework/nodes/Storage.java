package common.framework.nodes;

import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;

public class Storage implements Variabler {
	
	private Map<String, BigInteger> storage;
	
	Storage() {
		storage = new HashMap<String, BigInteger>();
	}

	public void Set(String key, BigInteger value) {
		storage.put(key, value);
	}


	public BigInteger Get(String key) {
		return  storage.get(key);
	}

	public void clear() {
		storage.clear();
	}
}