package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;
import java.util.Arrays;

import sinalgo.nodes.messages.Message;
 
public class BaseVectorMsg extends Message {
	public int round;
	public int affinity;
	public int source;	
	public int target;
	public BigInteger[] s;
	
	public BaseVectorMsg(int round, int affinity, int source, int target, BigInteger[] s) {
		this.round = round;
		this.affinity = affinity;
		this.source = source;
		this.target = target;
		this.s = Arrays.copyOf(s, s.length);		
	}
	
	public String toString(boolean full) {
        StringBuilder sb = new StringBuilder();
        if (full) {
        	sb.append(String.format("round: %d affinity: %d source: %d target: %d s: ", round, affinity, source, target));
        }
        for (BigInteger bigInt : s) {
            sb.append(bigInt.toString()).append(", ");
        }
        
        // Remove the last comma and space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
	}

		
	@Override
	public Message clone() {
		return this;
	}

}
