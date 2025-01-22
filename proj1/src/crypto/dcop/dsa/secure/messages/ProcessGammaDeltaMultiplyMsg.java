package crypto.dcop.dsa.secure.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class ProcessGammaDeltaMultiplyMsg extends Message{

	// The id of the key owner
	public int ownerID;
	// The index of the key to use (wb-<<ownerID>>-<<index>>) 
	public int sharedIndex;
	
	public int targetIndex;
	
	public Shared gamma_2t_tilda;
	public Shared delta_2t_tilda;

	
    /**
     * Constructs a secure multiplication request message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public ProcessGammaDeltaMultiplyMsg(int ownerID, int sharedIndex, int targetIndex, Shared gamma, Shared delta) {
		this.ownerID = ownerID;
		this.sharedIndex = sharedIndex;
		this.targetIndex = targetIndex;
		this.gamma_2t_tilda = gamma;
		this.delta_2t_tilda = delta;		
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
	
}
