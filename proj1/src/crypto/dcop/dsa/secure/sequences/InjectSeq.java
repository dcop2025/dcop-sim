package crypto.dcop.dsa.secure.sequences;

import sinalgo.nodes.messages.Message;

public class InjectSeq implements ISequence {
	public int expectedAck;
	public Message postMsg;
	public String key;
	private int ackCount;
	public int value;
			
	public InjectSeq(String key, int value, int expectedAck, Message postMsg) {
		this.postMsg = postMsg;	
		this.expectedAck = expectedAck;
		this.ackCount = 0;
		this.key = key;
		this.value = value;
	}
	
	public boolean ackCount() {
		ackCount++;
		return (ackCount == expectedAck);
	}

}
