package common.framework.nodes;

public interface SyncPoint {
	boolean tickSync(String key, int waitTil);
}
