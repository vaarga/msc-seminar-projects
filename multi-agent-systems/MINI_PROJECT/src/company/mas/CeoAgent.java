package company.mas;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CeoAgent extends PersonAgent {
	Map<String, Integer> salaries = Salaries.salaries;
	
	protected void setup() {
		System.out.println("Ceo agent is ready.");
		
		Object[] args = getArguments();
		
		// Process the arguments
		if (args != null && args.length == 4) {
			money = Integer.parseInt(args[0].toString());
			
			int numberOfUiUxAgents = Integer.parseInt(args[1].toString());
			int numberOfDeveloperAgents = Integer.parseInt(args[2].toString());
			int numberOfQaAgents = Integer.parseInt(args[3].toString());

	        // Create HrAgent and ManagerAgent (EmployeePaidMonthlyAgents)
	        addBehaviour(new OneShotBehaviour() {
	            public void action() {
	                try {
	                    AgentController hrController = getContainerController().createNewAgent("Hr", HrAgent.class.getName(), null);
	                    hrController.start();
	                    
	                    AgentController managerController = getContainerController().createNewAgent("Manager", ManagerAgent.class.getName(), null);
	                    managerController.start();
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }
	        });
	        
	        // Create the initial UiUxAgents, DeveloperAgents and the QaAgents (EmployeePaidPerProjectAgent)
	        createMultipleEmployeesForProjects("UiUx", numberOfUiUxAgents,  UiUxAgent.class.getName());
	        createMultipleEmployeesForProjects("Developer", numberOfDeveloperAgents, DeveloperAgent.class.getName());
	        createMultipleEmployeesForProjects("Qa", numberOfQaAgents, QaAgent.class.getName());
	        
	        // Add behaviour to send salaries every 30 seconds to the agents which are payed monthly
	        addBehaviour(new TickerBehaviour(this, 30000) {
	            @Override
	            protected void onTick() {
	            	sendSalary("Hr");
	            	sendSalary("Manager");
	            }
	        });
	        
	        addBehaviour(new PaymentReceiverHandler());
		} else {
			System.out.println("Invalid arguments provided for the Ceo agent!");

			doDelete();
		}
	}
	
	private void createMultipleEmployeesForProjects(String agentPrefixName, int numberOfAgents, String agentClassName) {
		List<AID> agentsList = new ArrayList<>();
		
        // Create the specified number of a one type of agent (UiUxAgents, DeveloperAgents or the QaAgents)
        for (int i = 0; i < numberOfAgents; i++) {
            String agentName = agentPrefixName + "-" + System.currentTimeMillis() + "-" + i;
            
            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    try {
                        AgentController controller = getContainerController().createNewAgent(agentName, agentClassName, null);
                        
                        controller.start();
                        
                        agentsList.add(new AID(agentName, AID.ISLOCALNAME));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
	}
	
	private void sendSalary(String employeeRole) {
	    int salary = salaries.get(employeeRole);

	    // If the CeoAgent has enough money to pay the employee (HrAgent or ManagerAgent) send the salary
	    if (money >= salary) {
	        money -= salary;
	        
	        ACLMessage salaryMsg = new ACLMessage(ACLMessage.INFORM);

	        salaryMsg.addReceiver(new AID(employeeRole, AID.ISLOCALNAME));
	        salaryMsg.setContent(Integer.toString(salary));
	        
	        send(salaryMsg);

	        System.out.println("Ceo agent sent salary to " + employeeRole + " agent (total money remained: " + money + "$).");
	    } else {
	        System.out.println("Not enough money to pay the salary of the " + employeeRole + ".");
	    }
	}
	
    private class PaymentReceiverHandler extends CyclicBehaviour {
        public void action() {
            // Listen for messages which are: INFORM, not null and from the ManagerAgent
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

            ACLMessage msg = receive(mt);

            if (msg != null) {
            	String senderAgentName =  msg.getSender().getName();
            	
            	if (senderAgentName.startsWith("Manager")) {
            		// The CeoAgent receives money from the ManagerAgent
                    int payment = Integer.parseInt(msg.getContent());
                    
                    money += payment;
                    
                    System.out.println("Ceo agent received " + payment + "$ (total money: " + money + "$).");
            	}
            } else {
                // If no message is received, block this behaviour until a message is received
                block();
            }
        }
    };
}
