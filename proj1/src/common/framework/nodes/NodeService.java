package common.framework.nodes;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface NodeService {
	int ID();

	void registerMsgHandlers(Class<? extends Message> messageClass, MessageHandler handler);

	void sendMsgToNode(Message msg, Node node);
	
	void handleMsg(Node ndoe, Message msg);
}
