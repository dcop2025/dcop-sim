package common.framework.nodes;

import java.util.HashMap;
import java.util.Map;

public class SyncMgr implements SyncPoint {
	private Map<String, Integer> waiters;
	
	public SyncMgr() {
		waiters = new HashMap<String, Integer>();		
	}
	
	public boolean tickSync(String key, int waitTil) {
		// Also ack back that the compare result is ready
		Integer wait = waiters.get(key);
		if (wait == null) {
			wait = 0;
		} 		
		wait++;
		waiters.put(key, wait);
		if (wait != waitTil) {
			return false;			
		}
		waiters.remove(key);
		return true;
	}

}
