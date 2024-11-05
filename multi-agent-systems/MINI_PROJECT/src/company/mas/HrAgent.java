package company.mas;

import java.util.List;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HrAgent extends EmployeePaidMonthlyAgent {
    public void setup() {
        super.setup("Hr");

        // Listen for requests from the ManagerAgent
        addBehaviour(new HireRequestHandler());
    }

    private void createMultipleAgents(String agentPrefixName, int numberOfAgents, String agentClassName, List<AID> agents) {
        // Create the specified number of UiUxAgents, DeveloperAgents or QaAgents
        for (int i = 0; i < numberOfAgents; i++) {
        	String agentName = agentPrefixName + "-" + System.currentTimeMillis() + "-" + i;

            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    try {
                        AgentController controller = getContainerController().createNewAgent(agentName, agentClassName, null);
                        
                        controller.start();
                        
                        agents.add(new AID(agentName, AID.ISLOCALNAME));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }
    
    private class HireRequestHandler extends CyclicBehaviour {
    	 public void action() {
    		// Listen for messages which are: REQUEST, not null and from the ManagerAgent
             MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
             ACLMessage msg = receive(mt);
             
             if (msg != null) {
         		String senderAgentName =  msg.getSender().getName();

            	if (senderAgentName.startsWith("Manager")) {
                    // Handle the hire request
                    String[] hireRequest = msg.getContent().split(",");
                    int numberOfUiUxAgents = Integer.parseInt(hireRequest[0]);
                    int numberOfDeveloperAgents = Integer.parseInt(hireRequest[1]);
                    int numberOfQualityAssuranceAgents = Integer.parseInt(hireRequest[2]);
                    
                    System.out.println("Hr agent received a request to hire " +
                    		numberOfUiUxAgents + " UiUx, " + numberOfDeveloperAgents + " Developer(s), and " + numberOfQualityAssuranceAgents + " Qa.");

                    // Create the requested employees
                    List<AID> newAgents = new ArrayList<>();
                    createMultipleAgents("UiUx", numberOfUiUxAgents, UiUxAgent.class.getName(), newAgents);
                    createMultipleAgents("Developer", numberOfDeveloperAgents, DeveloperAgent.class.getName(), newAgents);
                    createMultipleAgents("Qa", numberOfQualityAssuranceAgents, QaAgent.class.getName(), newAgents);

                    // Inform the ManagerAgent that the new employees are ready
                    ACLMessage reply = msg.createReply();
                    
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    reply.setContent("Agents created.");
                    
                    send(reply);
            	}
             } else {
            	// If no message is received, block this behaviour until a message is received
                 block();
             }
         }
    }
}
