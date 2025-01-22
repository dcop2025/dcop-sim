package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

public class InjectSecretRespondMsg  extends Message  {

	public String key;
	public String seqKey;

	public InjectSecretRespondMsg(String seqKey, String key) {
		this.seqKey = seqKey;
		this.key = key;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
