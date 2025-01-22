package crypto.dcop.dsa.secure.sequences;

import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class MultiplySeq implements ISequence {
	public Shared[] shareds;
	public int expectedShares;
	public Message postMsg;
	public String aKey;
	public String bKey;
	public String cKey;
	private int head;
	private int ackCount;
	public int injected;
	
		
	public MultiplySeq(String aKey, String bKey, String cKey, int expectedShares, Message postMsg) {
		this.postMsg = postMsg;	
		this.expectedShares = expectedShares;
		this.shareds = new Shared[expectedShares];
		this.head = 0;
		this.ackCount = 0;
		this.aKey = aKey;
		this.bKey = bKey;
		this.cKey = cKey;
		this.injected = 0;
	}
		
	public boolean addShare(Shared shared) {
		this.shareds[this.head] = shared;
		this.head++;
		return (this.head == this.expectedShares);
	}
		
	public long resloved(long t, long prime) {
		return ShamirSharedGen.reconstruct(shareds, prime);
	}
	
	public boolean countAck() {
		this.ackCount++;
		return (this.ackCount == this.expectedShares); 
	}
	
	public void done() {
		return;
	}
}
