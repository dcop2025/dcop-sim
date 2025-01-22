package crypto.dcop.dsa.secure.sequences;


import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class CompareHalfPrimeSeq implements ISequence {
	public Message postMsg;
	public String key;
	private int agentCount;
	private int ax2readyCount;
	private Shared[] c;
	public int bitwiseIndex;
			
	public CompareHalfPrimeSeq(String key, int agentCount, Message postMsg) {
		this.postMsg = postMsg;	
		this.key = key;
		this.agentCount = agentCount;
		this.ax2readyCount = 0;
		this.c = new Shared[agentCount];
		this.bitwiseIndex = 31;
	}

	public String x2Key() {
		return this.key + "x2";
	}
	
	public boolean bitwiseReadyAck(Shared c) {
		this.c[ax2readyCount] = c;
		ax2readyCount++;
		return (ax2readyCount == agentCount);
	}
	
	public long getC(long prime) {
		return ShamirSharedGen.reconstruct(c, prime);
	}
}
