package crypto.dcop.dsa.secure.sequences;

import sinalgo.nodes.messages.Message;

public class BitwiseComapreSeq implements ISequence {
	public Message postMsg;
	public long a;
	public String bKey;
	public String resKey;
	public int index;
	public String lsbComputeSeqKey;
	private int agnetsCount;
	private int ackCounter;
			
	public BitwiseComapreSeq(long a, String bKey, String resKey, int agentCount, String lsbComputeSeqKey, Message postMsg) {
		this.postMsg = postMsg;
		this.a = a;
		this.bKey = bKey;
		this.resKey = resKey;
		this.index = 30;
		this.agnetsCount = agentCount;
		this.lsbComputeSeqKey = lsbComputeSeqKey;
		resetAckCounter();
	}
	
	public boolean tickAck() {
		this.ackCounter++;
		return (this.ackCounter == this.agnetsCount);
	}
	
	public void resetAckCounter() {
		this.ackCounter = 0;
	}
	
	
	
}
