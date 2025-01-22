package crypto.dcop.mgm.vanilla;

import sinalgo.nodes.messages.Message;

public class SelectedIndexMsg extends Message{
	public int index;
	
	public SelectedIndexMsg(int index) {
		this.index = index;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}