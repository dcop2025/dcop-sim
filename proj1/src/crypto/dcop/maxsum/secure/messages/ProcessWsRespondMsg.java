package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;

public class ProcessWsRespondMsg extends BaseVectorMsg {
	public BigInteger opaque;
	public ProcessWsRespondMsg(int round, int affinity, int source, int target, BigInteger[] s, BigInteger opaque) {		
		super(round, affinity, source, target, s);
		this.opaque = opaque;
	}
	
	public String toString(boolean full) {
		return super.toString(full);
	}

}
