package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class BitwiseCompareMsg extends Message {
	public String seqKey;
	public long a;
	public String bKey;
	public String resKey;
	public int index;

	public BitwiseCompareMsg(String seqKey, long a, String bKey, String resKey, int index) {
		this.seqKey = seqKey;
		this.a = a;
		this.bKey = bKey;
		this.resKey = resKey;
		this.index = index;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
