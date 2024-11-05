package company.mas;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ClientAgent extends PersonAgent {
	private String projectName;
	
    protected void setup() {
    	System.out.println("Client agent (" + getLocalName() + ") is ready.");

        Object[] args = getArguments();
        
        // Process the arguments
        if (args != null && args.length == 4) {
            try {
	            projectName = (String) args[0];
	            
	            String projectType = (String) args[1];
	            int workload = Integer.parseInt((String) args[2]);
	            int budget = Integer.parseInt((String) args[3]);

	            money = budget;

	            // Send a request to the ManagerAgent to accept the ClientAgent's project
	            Project request = new Project(projectName, projectType, workload, budget);
	            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

	            msg.addReceiver(new AID("Manager", AID.ISLOCALNAME));
                msg.setContentObject(request);

                send(msg);
                
                // Listen for response from the ManagerAgent
                addBehaviour(new ResponseHandler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        	System.out.println("Invalid arguments provided for the Client agent (" + getLocalName() + ")!");

            doDelete();
        }
    }

    private class ResponseHandler extends CyclicBehaviour {
        public void action() {
        	// Listen for messages which are: ACCEPT_PROPOSAL or REFUSE, not null and from the ManagerAgent
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
            );
            ACLMessage msg = receive(mt);

            if (msg != null) {
                String senderAgentName = msg.getSender().getName();

                if (senderAgentName.startsWith("Manager")) {
                	// Print out the ManagerAgent's decision
                	
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    	System.out.println("Client agent (" + getLocalName() + ") \"" + projectName + "\" project was accepted.");
                    } else {
                    	System.out.println("Client agent (" + getLocalName() + ") \"" + projectName + "\" project was refused.");
                    }
                }
            } else {
            	// If no message is received, block this behaviour until a message is received
                block();
            }
        }
    }
}
