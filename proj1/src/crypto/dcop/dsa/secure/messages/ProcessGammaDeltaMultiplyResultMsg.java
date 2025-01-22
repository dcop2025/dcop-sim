package crypto.dcop.dsa.secure.messages;


import sinalgo.nodes.messages.Message;

public class ProcessGammaDeltaMultiplyResultMsg extends Message{

	// The id of the key owner
	public int ownerID;
	
	public long gamma;
	public long delta;
	public int targetIndex;

	
    /**
     * Constructs a secure multiplication request message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public ProcessGammaDeltaMultiplyResultMsg(int ownerID, int targetIndex, long gamma, long delta) {
		this.ownerID = ownerID;
		this.gamma = gamma;
		this.delta = delta;
		this.targetIndex = targetIndex;
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
	
	public int ownerID() {
		return this.ownerID;
	}

}
