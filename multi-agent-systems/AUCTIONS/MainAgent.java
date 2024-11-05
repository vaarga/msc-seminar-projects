package auctions.mas;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;

import java.util.HashSet;
import java.util.Set;

public class MainAgent extends Agent {
    private int numOfUsers;
    private Set<String> itemsAtAuction;

    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length == 1) {
            numOfUsers = Integer.parseInt((String) args[0]);
        } else {
            System.out.println("Error: Number of users not specified.");
            doDelete();
            return;
        }

        System.out.println("Main Agent " + getAID().getName() + " is ready.");

        itemsAtAuction = new HashSet<>();

        createGUIAgents();

        addBehaviour(new ReceiveMessages());
    }

    protected void takeDown() {
        System.out.println("Main Agent " + getAID().getName() + " is terminating.");
    }

    private void createGUIAgents() {
        for (int i = 1; i <= numOfUsers; i++) {
            try {
                Object[] arguments = new Object[]{i};
                AgentController agentController = getContainerController().createNewAgent("Gui-" + i, GuiAgent.class.getName(), arguments);
                agentController.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiveMessages extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            ACLMessage msg = myAgent.receive(mt);
            
            
            if (msg != null) {
                String senderAgentName = msg.getSender().getName();
                String content = msg.getContent();
                
                if (senderAgentName.startsWith("Gui")) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        String[] parts = content.split(";");
                        String item = parts[0];
                        String auctionType = parts[1];

                        
                        createAuctionAgent(item, auctionType);
                        itemsAtAuction.add(item);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent("Auction created successfully!");
                        send(reply);
                    }
                } else if (senderAgentName.startsWith("Search")) {
                	 ACLMessage reply = msg.createReply();
                	 int perfomative = ACLMessage.REFUSE;
                	
                	if (itemsAtAuction.contains(content)) {
                		perfomative = ACLMessage.CONFIRM;
                	}
                	
                	reply.setPerformative(perfomative);
                    send(reply);
                } else if (senderAgentName.startsWith("Auction")) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        System.out.println("Main Agent received a message: " + content);

                        itemsAtAuction.remove(content);
                    }
                }
            }
        }

        private void createAuctionAgent(String item, String auctionType) {
            try {

                Object[] arguments = new Object[]{item, auctionType};

                AgentController agentController = getContainerController().createNewAgent("Auction-" + item, AuctionAgent.class.getName(), arguments);
                agentController.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
