package crypto.pdsa.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

/*
 *  ReconstructRequestMsg is the message sent to all agents 
 *  with a request to reconstruct a secret
 */
public class ReconstructRequestMsg extends OMessage {

	// The id of the key owner
	private int ownerID;
	// The key to id which shared to reconstruct
	private String key;

    /**
     * Constructs a reconstruct request message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct
     */
	public ReconstructRequestMsg(int id, String key) {
		this.ownerID = id;
		this.key = key;
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
     * implements the action method, by calling the reconstruct request message handler
     *
     * @param the agnet to handle the message
     * @param the node(agent) that sent the message
     */
	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleReconstructRequestMsg(sendner, this);
	}

	public int ownerID() {
		return this.ownerID;
	}
	
	public String key() {
		return this.key;
	}

}
