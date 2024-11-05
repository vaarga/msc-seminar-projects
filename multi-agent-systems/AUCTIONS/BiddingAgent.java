package auctions.mas;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BiddingAgent extends Agent {
    private String iteratorNumber;
    private String item;

    protected void setup() {
        Object[] arguments = getArguments();

        if (arguments != null && arguments.length == 2) {
            iteratorNumber = (String) arguments[0];
            item = (String) arguments[1];
            System.out.println("Bidding Agent " + getAID().getName() + " is ready.");
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("Auction-" + item, AID.ISLOCALNAME));
            send(message);

            addBehaviour(new ReceiveMessages());
        } else {
            System.out.println("Invalid arguments. Bidding Agent terminated.");
            doDelete();
        }
    }

    private class ReceiveMessages extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String senderAgentName = msg.getSender().getName();
                String content = msg.getContent();
                
                if (senderAgentName.startsWith("Auction-" + item)) {
                    ACLMessage forwardMessage = new ACLMessage(ACLMessage.INFORM);
                    forwardMessage.setContent(msg.getContent());
                    forwardMessage.addReceiver(new AID("Gui-" + iteratorNumber, AID.ISLOCALNAME));
                    send(forwardMessage);
                } else if (senderAgentName.startsWith("Gui-" + iteratorNumber)) {
                    ACLMessage forwardMessage = new ACLMessage(ACLMessage.REQUEST);
                    forwardMessage.setContent(msg.getContent());
                    forwardMessage.addReceiver(new AID("Auction-" + item, AID.ISLOCALNAME));
                    send(forwardMessage);
                }
            }
        }
    }

    protected void takeDown() {
        System.out.println("Bidding Agent " + iteratorNumber + " is terminating.");
    }
}
