package crypto.dcop.dsa.secure.sequences;

import crypto.utils.shamir.Shared;

public class ResolvedSharedsSeq implements ISequence {
	public Shared[] shareds;
	public int counter;
	public int index;
	
	public  ResolvedSharedsSeq(int expectedShares) {
		this.counter = expectedShares;
		this.shareds = new Shared[expectedShares];
		this.index = 0;
		
	}	
}
