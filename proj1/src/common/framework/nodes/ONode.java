package common.framework.nodes;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import common.framework.messages.*;

// GNode in an extended of the sinalgo Node with basic function
// that can be used in different applications, like debug, and message handling 
public class ONode extends Node implements NodeService {

	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}

	private Variabler varBar;
	private SyncPoint syncPoint;
	
	public ONode() {
		varBar = new Storage();
		syncPoint = new SyncMgr();
		
		messageHandlers = new HashMap<Class<? extends Message>, MessageHandler>();
	}
	
	public Variabler variabler() {
		return varBar;
	}
	
	public SyncPoint syncPoint() {
		return syncPoint;
	}
	
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}
		
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		// we could remove routing-table entries that use this neighbor
	}

	@Override
	public void preStep() {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message m = inbox.next();
			handleMsg(inbox.getSender(), m);
		}
	}
	
	public void handleMsg(Node sender, Message m) {
		//debug(false, "got " + m.getClass() + " from: " + inbox.getSender().ID);
		debug(false, "got " + m.getClass() + " from: " + sender.ID);
		MessageHandler handler = messageHandlers.get(m.getClass());
		handler.handle(sender, m);		
	}

	public void registerMsgHandlers() {
		this.registerMsgHandlers(MultiMsgMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				multiMsgMsgHandler(sender, (MultiMsgMsg) msg);
			}
		});
	}
	@Override
	public void postStep() {
		/*
		if (_triggerDSA) {
			KickStartDSA();
			return;
		}
		if (_triggerPDSA) {
			KickStartPDSA();
		}*/
	}

	public int ID() {
		return this.ID;
	}

	public void sendMsgToNode(Message msg, Node node) {
		send(msg, node);
	}
	
	////////////////////////////////////////////////////
	/*
	private Map<Class<? extends Message>, BiConsumer<Node, ? extends Message>> messageHandlers;
	
	public void registerMsgHandlers(Class<? extends Message> messageClass, BiConsumer<Node, ? extends Message> handler) {
		messageHandlers.put(messageClass, handler);
	}
	*/
	protected Map<Class<? extends Message>, MessageHandler> messageHandlers;
	
    public void registerMsgHandlers(Class<? extends Message> messageType, MessageHandler handler) {
        messageHandlers.put(messageType, handler);
    }
	
	private void multiMsgMsgHandler(Node sender, MultiMsgMsg msg) {
		Iterator<Message> iterator  = msg.messages.iterator();
        while (iterator.hasNext()) {
        	Message interMsg = iterator.next();
        	
    		MessageHandler handler = this.messageHandlers.get(interMsg.getClass());
    		handler.handle(sender, interMsg);        	
        }	
	}
	
	public void triggerEvent(String event) {
		
	}

}