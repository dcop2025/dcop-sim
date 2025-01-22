package crypto.pdsa.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

/*
 *  SecureCompareRespondMsg is the message sent as a respond to secure compare
 *  This means the the compare operation is finish and the result is stored in the requested key
 */
public class SecureCompareRespondMsg extends OMessage{

	// the given opaque to return with the respond
	private int opaque;
	
    /**
     * Constructs a secure compare respond message
     *
     * @param opaque to return with the respond 
     */
	public SecureCompareRespondMsg(int opaque) {
		this.opaque = opaque;
	}	

    /**
     * implements the clone pattern
     *
     * @return a clone of the message
     */	
	@Override
	public Message clone() {
		return this;
	}

    /**
     * implements the action method, by calling the secure compare respond message handler
     *
     * @param the agnet to handle the message
     * @param the node(agent) that sent the message
     */
	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleSecureCompareRespondMsg(sendner, this);
	}

	public int opaque() {
		return this.opaque;
	}
	
}