package projects.dcopproj.nodes.messages;

import utils.dcopgen.Agent;
import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

// TODO: This method relates to DSA, keeping unclean for now
// Need to move it to a different place

public class NotifySelectedValueMsg extends OMessage{
	private int _index;
	
	public NotifySelectedValueMsg(int index) {
		_index = index;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
	
	public int Index() {
		return _index;
	}
	

	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleNotifySelectedValueMsg(sendner, this);
	}

}
