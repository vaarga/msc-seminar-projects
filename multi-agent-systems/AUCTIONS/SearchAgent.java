package auctions.mas;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class SearchAgent extends Agent {
    private int iteratorNumber;
    private String item;
    private boolean itemFound = false;

    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length == 2) {
            iteratorNumber = Integer.parseInt((String) args[0]);
            item = (String) args[1];
        } else {
            System.out.println("Invalid arguments for SearchAgent.");
            doDelete();
            return;
        }
        
        System.out.println("Search Agent " + getAID().getName() + " is ready.");

        addBehaviour(new SearchBehaviour());
    }

    private class SearchBehaviour extends CyclicBehaviour {
        private int count = 0;

        public void action() {
            if (itemFound || count >= 60) {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                if (itemFound) {
                    message.setContent("found");
                } else {
                    message.setContent("not-found");
                }
                
                message.addReceiver(new AID("Gui-" + iteratorNumber, AID.ISLOCALNAME));
                send(message);

                doDelete();
                return;
            }

            if (count % 10 == 0) {
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setContent(item);
                message.addReceiver(new AID("Main", AID.ISLOCALNAME));
                send(message);
            }

            ACLMessage receivedMessage = receive();
            if (receivedMessage != null) {
                if (receivedMessage.getPerformative() == ACLMessage.CONFIRM) {
                    itemFound = true;
                }
            }

            count++;
            block(1000);
        }
    }
}
