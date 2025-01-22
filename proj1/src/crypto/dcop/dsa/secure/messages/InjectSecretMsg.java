package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;
import crypto.utils.shamir.Shared;

public class InjectSecretMsg extends Message {
	public String seqKey;
	public String key;
	public Shared shared;

	
	public InjectSecretMsg(String seqKey, String key, Shared shared) {
		this.seqKey = seqKey;
		this.key = key;
		this.shared = shared;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
