package crypto.dcop.mgm.vanilla;

import sinalgo.nodes.messages.Message;

public class SmallGiMsg extends Message{
	public int smallGi;
	
	public SmallGiMsg(int smallGi) {
		this.smallGi = smallGi;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}