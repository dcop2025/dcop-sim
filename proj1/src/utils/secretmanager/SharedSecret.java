package utils.secretmanager;

import java.math.BigInteger;

// SharedSecret is a shared secret hold the secret index and value info
public class SharedSecret{
	private int _index;
	private BigInteger _value;
	
	public SharedSecret(int index, BigInteger value) {
		this._index = index;
		this._value= value;
	}
	
	public int GetIndex()  {
		return _index;
	}
	
	public BigInteger GetValue() {
		return _value;
	}
}
