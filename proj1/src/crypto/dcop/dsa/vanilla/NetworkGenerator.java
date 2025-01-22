package crypto.dcop.dsa.vanilla;

import crypto.dcop.*;
import crypto.dcop.Problem.ConstraintsMatrix;
import crypto.dcop.dsa.vanilla.barins.AgentBarin;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

public class NetworkGenerator {
	
	public static Network gererate(Problem problem, boolean fullyConnected, long seed) {
		Network network = new Network();
		
		
		Iterator<ConstraintsMatrix> constraintsIter = problem.iterator();
		Map<Integer, DcopAgent> agents = new HashMap<Integer, DcopAgent>();
		Map<Integer, AgentBarin> agentsBrains = new HashMap<Integer, AgentBarin>();
			
		while (constraintsIter.hasNext()) {
			ConstraintsMatrix constraints = constraintsIter.next();
			if (constraints.zero()) {
				continue;
			}
			
			// Add constraints to agent A
			AgentBarin brainA;
			DcopAgent agentA = agents.get(constraints.a);			
			if (agentA == null) {				
				agentA = new DcopAgent(constraints.a);
				brainA = new AgentBarin(agentA, constraints.domainPowerAgentA(), seed);
				agentA.setAlgo(brainA);
				agents.put(constraints.a, agentA);
				network.agnets.add(agentA);
				agentsBrains.put(constraints.a, brainA);
			} else {
				brainA = agentsBrains.get(constraints.a);
			}
			brainA.installConstraintsMatrix(constraints);
			
			// Add constraints to agent B
			AgentBarin brainB;
			DcopAgent agentB = agents.get(constraints.b);
			if (agentB == null) {
				agentB = new DcopAgent(constraints.b);				
				brainB = new AgentBarin(agentB, constraints.domainPowerAgentA(), seed);
				agentB.setAlgo(brainB);
				agents.put(constraints.b, agentB);
				network.agnets.add(agentB);
				agentsBrains.put(constraints.b, brainB);
			} else {
				brainB = agentsBrains.get(constraints.b);
			}
			brainB.installConstraintsMatrix(constraints);
			
			
			agentA.addBidirectionalConnectionTo(agentB);
			brainA.addNeighbor(agentB);
			brainB.addNeighbor(agentA);
			
		}
				
		if (fullyConnected) {
			for (int i = 0; i < network.agnets.size() -1 ; i++) {
				for (int j = i + 1; j < network.agnets.size(); j++) {
					// no need connect agents that already agent
					if (network.agnets.get(i).outgoingConnections.contains(network.agnets.get(i), network.agnets.get(j))) {
						continue;
					}
					network.agnets.get(i).addBidirectionalConnectionTo(network.agnets.get(j));
				}
			}			
		}

		return network;
	}
}
