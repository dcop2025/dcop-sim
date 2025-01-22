package crypto.dcop.dsa.secure.sequences;

import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class LSBComputeSeq implements ISequence {
	
	public String inputKey;
	public String outputKey;
	public String cmpSeqKey;
	public Message postMsg;
	private int agentsCount;
	private int prepAckCounter;
	private Shared[] perpShareds;
	private int calcLSBAckCounter;
	
	
	
	public LSBComputeSeq(String inputKey, String outputKey, String cmpSeqKey, int agentsCount, Message postMsg) {
		this.inputKey = inputKey;
		this.outputKey = outputKey;
		this.cmpSeqKey = cmpSeqKey;
		this.postMsg = postMsg;
		this.agentsCount = agentsCount;		
		this.prepAckCounter = 0;
		this.perpShareds = new Shared[this.agentsCount];
		this.calcLSBAckCounter = 0;
	}
	
	public boolean prepReady(Shared shared) {
		this.perpShareds[this.prepAckCounter] = shared;
		this.prepAckCounter++;
		return (this.prepAckCounter == this.agentsCount);
	}
	
	
	public boolean calcLSBAck() {
		this.calcLSBAckCounter++;
		return (this.calcLSBAckCounter == this.agentsCount);
	}
	
	public long reconstructPerp(long prime) {
		return ShamirSharedGen.reconstruct(perpShareds, prime);
	}
}
