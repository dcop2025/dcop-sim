package crypto.dcop.dsa.vanilla.messages;

import sinalgo.nodes.messages.Message;

public class NotifySelectedValueMsg extends Message {
	public int index;
	
	public NotifySelectedValueMsg(int index) {
		this.index = index;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
}
