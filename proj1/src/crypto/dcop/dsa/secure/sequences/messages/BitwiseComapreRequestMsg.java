package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class BitwiseComapreRequestMsg extends Message {
	
	public int ownerID;
	public long a;
	public String bKey;
	public String resKey;
	public int index;	
	public String bwcSeqKey;
	public int phase;
	
	public BitwiseComapreRequestMsg(int ownerID, long a, String bKey, String resKey, int index, int phase, String bwcSeqKey) {
		this.ownerID = ownerID;
		this.a = a;
		this.bKey = bKey;
		this.resKey = resKey;  
		this.index = index;
		this.bwcSeqKey = bwcSeqKey;
		this.phase = phase;
	}
	
	public boolean isFirst() {
		return (this.index == 30);
	}
	
	public boolean isLast() {
		return (this.index == 0);
	}
	
	@Override
	public Message clone() {
		return this;
	}


}
