package common.framework.nodes;

import java.math.BigInteger;

public interface Variabler {
	void Set(String key, BigInteger value);
	
	BigInteger Get(String key);
	
	void clear();
}
