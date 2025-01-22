package utils.secretmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// SecretManager manage secrets in an agent 
//  * Store a given shared 
//  * Store collected shared to resolve the secret
public class SecretManager {
	
	public class ResolvedSecret {
		private int _value;
		private boolean _isValid;
		
		public ResolvedSecret(int value, boolean isValid) {
			this._value = value;
			this._isValid = isValid;
		}
		
		public int Value() {
			return this._value;
		}
		
		public boolean Valid() {
			return this._isValid;
		}
	}
	
	// SharedSecret is a shared secret hold the secret index and value info
	public class SecretInfo{
		private SharedSecret _my_share;
		private List<SharedSecret> _collectedShared;
		private int _ownerId;
		private int _threshold;
		
		public SecretInfo(int ownerId, int threshold, SharedSecret shared) {
			this._ownerId = ownerId;
			this._my_share = shared;
			this._threshold = threshold;
			this._collectedShared = new ArrayList<SharedSecret>();
			this._collectedShared.add(shared);
		}
		
		public void AddShared(SharedSecret shared) {
			this._collectedShared.add(shared);
		}
		
		public ResolvedSecret RestoreSecret() {
			return new ResolvedSecret(0, false);
		}
	}
	
	private Map<String, SecretInfo>_secrets;
	
	public SecretManager() {
		this._secrets = new HashMap<String, SecretInfo>();
	}
	
	public void AddSecret(String key, int ownerId, int threshold, SharedSecret shared) {
		// TODO check that key doesn't exists
		SecretInfo info = new SecretInfo(
				ownerId,
				threshold,
				shared);
		this._secrets.put(key, info);
	}
	
	public void AddShared(String key, SharedSecret shared) {
		SecretInfo info = _secrets.get(key);
		if (info == null) {
			// TODO handle this error
			return;
		}
		info.AddShared(shared);
	}
	
	public void McpAdd(String keyA, String keyB, String keyC, int ownerId) {
		SecretInfo infoA = _secrets.get(keyA);
		if (infoA == null)  {
			// TODO handle this error
			return;
		}
		SecretInfo infoB = _secrets.get(keyB);
		if (infoB == null)  {
			// TODO handle this error
			return;
		}
		SharedSecret sharedC = new SharedSecret(infoA._my_share.GetIndex(), infoA._my_share.GetValue().add(infoB._my_share.GetValue()));
		AddSecret(keyC, ownerId, ownerId, sharedC);
		
	}
}
