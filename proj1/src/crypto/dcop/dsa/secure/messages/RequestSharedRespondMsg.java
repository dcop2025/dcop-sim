package crypto.dcop.dsa.secure.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class RequestSharedRespondMsg extends Message {
	public String key;
	public Shared shared;
	
	public RequestSharedRespondMsg(String key, Shared shared) {
		this.key = key;
		this.shared = shared;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
