package crypto.dcop;


import common.framework.nodes.*;

public class DcopAgent extends ONode {
	
	private ONodeAlgo brain;
	private boolean triggerAlgo;
	public int agentId;
		
	public DcopAgent(int agnetId) {
		super();
		this.agentId = agnetId;
		triggerAlgo = false;
	}

	public void setAlgo(ONodeAlgo algo) {
		this.brain = algo;
		algo.registerMsgHandlers();
	}
	
	public void triggerAlgo() {
		triggerAlgo = true;
	}
	
	@Override
	public void postStep() {
		if (triggerAlgo) {
			kickStartAlgo();
			return;
		}
	}

	public void kickStartAlgo() {
		triggerAlgo = false;
		brain.init();
		brain.start();		
	}
	
	public boolean doneStatus() {
		return brain.doneStatus();
	}

	public void logState() {
		brain.logState();
	}
	
	public int assignment() {
		return brain.assignment();
	}
	
	public int getInternal(String key) {
		return brain.getInternal(key);
	}
}
