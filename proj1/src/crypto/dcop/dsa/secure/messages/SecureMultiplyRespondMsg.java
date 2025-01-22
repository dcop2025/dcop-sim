package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

/*
 *  SecureMultiplyRespondMsg s the message sent as a respond to secure compare
 *  This means the the multiplication operation is finish and the result is stored in the requested key 
 *  
 *  TODO: rename it as this trigger more then a simple multiplication request 
 */
public class SecureMultiplyRespondMsg extends Message {
	
	// The id of the key owner
	public int ownerID;
	// The index of the key to use (wb-<<ownerID>>-<<index>>)
	public int targetIndex;
	
    /**
     * Constructs a secure multiplication respond message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public SecureMultiplyRespondMsg(int owenrID, int targetIndex) {
		this.ownerID = owenrID;
		this.targetIndex = targetIndex;
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
}
