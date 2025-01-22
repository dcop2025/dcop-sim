package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class MultiplySharesRequestMsg extends Message {
	public int ownerID;
	public String seqKey;
	public String aKey;
	public String bKey;
	public String cKey;
	public String rKey;

	public MultiplySharesRequestMsg(int ownerID, String seqKey, String aKey, String bKey, String cKey) {
		this.ownerID = ownerID;
		this.seqKey = seqKey;
		this.aKey = aKey;
		this.bKey = bKey;
		this.cKey = cKey;
		this.rKey = null;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
