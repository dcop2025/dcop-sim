package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class InjectMultiplyResultMsg extends Message {
	public int ownerID;
	public String seqKey;
	public long c_2t_r;
	public String rKey;
	public String cKey;

	public InjectMultiplyResultMsg(int oewrnID, String seqKey, String cKey, long c_2t_r) {
		this.ownerID = oewrnID;
		this.seqKey = seqKey;
		this.cKey = cKey;
		this.c_2t_r = c_2t_r;
		this.rKey = null;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
