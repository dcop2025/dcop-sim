package crypto.dcop.dsa.secure.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class PostBetaCompareMsg extends Message  {

	public Shared left;
	public Shared rigth;
	public String seqKey;
	public int u;
	public long expected;
	
	public PostBetaCompareMsg(String seqKey, int u, long expected) {
		this.seqKey = seqKey;
		this.u = u;
		this.expected = expected;
	}

	public void withShared(Shared left, Shared right) {
		this.left = left;
		this.rigth = right;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}