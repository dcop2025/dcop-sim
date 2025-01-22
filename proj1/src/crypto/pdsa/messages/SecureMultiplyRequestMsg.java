package crypto.pdsa.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

/*
 *  SecureMultiplyRequestMsg is the message sent to all agents 
 *  with request to do secure multiplication between two keys
 *  
 *  TODO: rename it as this trigger more then a simple multiplication request 
 */
public class SecureMultiplyRequestMsg  extends OMessage{

	// The id of the key owner
	private int ownerID;
	// The index of the key to use (wb-<<ownerID>>-<<index>>) 
	private int index;
	
    /**
     * Constructs a secure multiplication request message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public SecureMultiplyRequestMsg(int ownerID, int index) {
		this.ownerID = ownerID;
		this.index = index;
	}
		
    /**
     * implements the clone pattern
     *
     * @return a clone of the message
     */
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
    /**
     * implements the action method, by calling the multiply request message handler
     *
     * @param the agnet to handle the message
     * @param the node(agent) that sent the message
     */
	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleSecureMultiplyRequestMsg(sendner, this);
	}

	
	public int index() {
		return this.index;
	}
	
	public int ownerID() {
		return this.ownerID;
	}

}
