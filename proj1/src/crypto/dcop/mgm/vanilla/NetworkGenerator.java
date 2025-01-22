package crypto.dcop.mgm.vanilla;

import crypto.dcop.*;
import crypto.dcop.Problem.ConstraintsMatrix;
import sinalgo.runtime.Global;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

public class NetworkGenerator {
	
	public static Network gererate(Problem problem, long seed) {
		Network network = new Network();
		
		
		BigInteger p = getGlobalPrime();
		Global.log.logln(true, "Selected P:" + p);
		
		Iterator<ConstraintsMatrix> constraintsIter = problem.iterator();
		Map<Integer, Agent> agents = new HashMap<Integer, Agent>();
			
		while (constraintsIter.hasNext()) {
			ConstraintsMatrix constraints = constraintsIter.next();
/*
			if (constraints.zero()) {
				continue;
			}*/
			Agent agentA = agents.get(constraints.a);			
			if (agentA == null) {				
				agentA = new Agent(constraints.a, constraints.domainPowerAgentA(),seed);
				agents.put(constraints.a, agentA);
				network.agnets.add(agentA);
			}
			
			Agent agentB = agents.get(constraints.b);			
			if (agentB == null) {				
				agentB = new Agent(constraints.b, constraints.domainPowerAgentB(), seed);
				agents.put(constraints.b, agentB);
				network.agnets.add(agentB);
			}

			agentA.addConnectionConstraints(agentB, constraints.matrix);
			agentB.addConnectionConstraints(agentA, constraints.revMatrix());
		}

		/*
		for (Integer i : agents.keySet()) {
			for (Integer j : agents.keySet()) {
				if (i == j) {
					continue;
				}
				agents.get(i).addSupportConnection(agents.get(j));
				agents.get(j).addSupportConnection(agents.get(i));
			}
			
		}*/		

		return network;
	}
	
	private static BigInteger getGlobalPrime() {
		return BigInteger.valueOf(97);
		//SecureRandom sr = new SecureRandom();
		//BigInteger prime = BigInteger.probablePrime(1000, sr);
		//return prime;
	}

}
