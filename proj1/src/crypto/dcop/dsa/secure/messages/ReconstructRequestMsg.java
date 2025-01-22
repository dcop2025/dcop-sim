package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

/*
 *  ReconstructRequestMsg is the message sent to all agents 
 *  with a request to reconstruct a secret
 */
public class ReconstructRequestMsg extends Message {

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
		ownerID = id;
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

	public int ownerID() {
		return this.ownerID;
	}
	
	public String key() {
		return this.key;
	}

}
