package crypto.dcop.mgm.vanilla;

import sinalgo.nodes.messages.Message;

public class SmallCiMsg extends Message{
	public int smallCi;
	
	public SmallCiMsg(int smallCi) {
		this.smallCi = smallCi;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}