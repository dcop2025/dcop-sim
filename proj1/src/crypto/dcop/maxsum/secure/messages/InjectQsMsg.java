package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;

public class InjectQsMsg extends DoubleVectorMsg {
	public InjectQsMsg(int round,int source, int target, BigInteger[] v1, BigInteger[] v2) {
		super(round,source, target, v1, v2);
	}
	
	public String toString(boolean full) {
		return super.toString(full);
	}
}
