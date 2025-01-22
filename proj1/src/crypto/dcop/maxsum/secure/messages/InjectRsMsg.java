package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;

public class InjectRsMsg extends DoubleVectorMsg {
	public boolean withRemote;
	
	public InjectRsMsg(int round,int source, int target, BigInteger[] local, BigInteger[] remote, boolean withRemote) {
		super(round,source, target, local, remote);
		this.withRemote = withRemote;
	}
}
