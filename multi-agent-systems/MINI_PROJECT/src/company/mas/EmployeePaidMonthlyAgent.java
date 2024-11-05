package company.mas;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class EmployeePaidMonthlyAgent extends PersonAgent {
	public String employeeRole;
	
    protected void setup(String employeeRole) {
    	this.employeeRole = employeeRole;
    	
    	System.out.println(employeeRole + " agent is ready.");

    	// Listen for messages from the CeoAgent
        addBehaviour(new SalaryReceiverHandler());
    }

    private class SalaryReceiverHandler extends CyclicBehaviour {
        public void action() {
        	// Listen for messages which are: INFORM, not null and from the CeoAgent
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);

            if (msg != null) {
            	String senderAgentName =  msg.getSender().getName();

            	if (senderAgentName.startsWith("Ceo")) {
                    int salary = Integer.parseInt(msg.getContent());

                    // Receive the salary (every month)
                    money += salary;

                    System.out.println(employeeRole + " agent received " + salary + "$ (total money: " + money + "$).");
            	}
            } else {
            	// If no message is received, block this behaviour until a message is received
                block();
            }
        }
    }
}
