package crypto.dcop.dsa.secure.sequences;

import sinalgo.nodes.messages.Message;

public class SubSharedsSeq implements ISequence {
	public String aKey;
	public String bKey;
	public String resKey;
	public String cmpSeqKey;
	public Message postMsg;
	private int ackCounter;
			
	public SubSharedsSeq(String aKey, String bKey, String resKey, String cmpSeqKey, Message postMsg) {
		this.aKey = aKey;
		this.bKey = bKey;
		this.resKey = resKey;
		this.cmpSeqKey = cmpSeqKey; 
		this.postMsg = postMsg;
		this.ackCounter = 0;
	}
	
	public boolean ackAndDone(int expectedAck) {
		this.ackCounter++;
		return (this.ackCounter == expectedAck);
	}
		
}
