package common.framework.nodes;

public interface ONodeAlgo {
	void init();
	void start();
	boolean doneStatus();	
	void registerMsgHandlers();
	void logState();
	int assignment();
	int getInternal(String key);
}
