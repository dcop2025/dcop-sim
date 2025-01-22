package common.framework.nodes;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface MessageHandler {
	void handle(Node sender, Message msg);

}
