package crypto.dcop.maxsum.vanilla;

import crypto.dcop.*;
import crypto.dcop.Problem.ConstraintsMatrix;
import crypto.dcop.Network;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

public class NetworkGenerator {
	
	public static Network gererate(Problem problem, boolean fullyConnected) {
		Network network = new Network();
		
		
		Iterator<ConstraintsMatrix> constraintsIter = problem.iterator();
		Map<Integer, DcopAgent> agents = new HashMap<Integer, DcopAgent>();
		Map<Integer, AlgoAgent> agentsBrains = new HashMap<Integer, AlgoAgent>();
		
		
		while (constraintsIter.hasNext()) {
			ConstraintsMatrix constraints = constraintsIter.next();
			if (constraints.zero()) {
				continue;
			}
			// Add constraints to agent A
			DcopAgent agentA = agents.get(constraints.a);
			if (agentA == null) {				
				agentA = new DcopAgent(constraints.a);
				AlgoAgent brain = new AlgoAgent(agentA, agentA.variabler(), agentA.syncPoint(), constraints.domainPowerAgentA());
				agentA.setAlgo(brain);
				agents.put(constraints.a, agentA);
				network.agnets.add(agentA);
				agentsBrains.put(constraints.a, brain);
			}
			// Add constraints to agent B
			DcopAgent agentB = agents.get(constraints.b);
			if (agentB == null) {
				agentB = new DcopAgent(constraints.b);				
				AlgoAgent brain = new AlgoAgent(agentB, agentB.variabler(), agentB.syncPoint(), constraints.domainPowerAgentB());
				agentB.setAlgo(brain);
				agents.put(constraints.b, agentB);
				network.agnets.add(agentB);
				agentsBrains.put(constraints.b, brain);
			}
			agentA.addBidirectionalConnectionTo(agentB);
			
			DcopAgent functionNode = new DcopAgent(constraints.b);
			AlgoFunction brain = new AlgoFunction(functionNode, functionNode.variabler(), agentA, agentB, constraints);
			functionNode.setAlgo(brain);
			agentsBrains.get(constraints.a).addFunctionNode(agentB.ID, functionNode);
			agentsBrains.get(constraints.b).addFunctionNode(agentA.ID, functionNode);
			functionNode.addBidirectionalConnectionTo(agentA);
			functionNode.addBidirectionalConnectionTo(agentB);
			//network.functionNodes.add(functionNode);
			network.agnets.add(functionNode);
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
