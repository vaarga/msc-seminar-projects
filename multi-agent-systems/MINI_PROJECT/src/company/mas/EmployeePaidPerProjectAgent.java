package company.mas;

import java.util.Timer;
import java.util.TimerTask;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.DFService;

public abstract class EmployeePaidPerProjectAgent extends PersonAgent {
    public String employeeRole;
    private Timer workTimer;

    protected void setup(String employeeRole) {
    	this.employeeRole = employeeRole;

    	System.out.println(employeeRole + " agent (" + getLocalName() + ") is ready.");

    	// Listen for requests from the ManagerAgent
        addBehaviour(new ProjectRequestHandler());
    }

    public void registerToDF() {
        // Register the agent to the DF (will be available to work on a project)
        DFAgentDescription dfad = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(employeeRole);
        sd.setName(getLocalName());
        
        dfad.setName(getAID());
        dfad.addServices(sd);

        try {
            DFService.register(this, dfad);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
    
    public void deregisterFromDF() {
        // Deregister the agent from the DF (will not be available to work on a project)
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class WorkTimerTask extends TimerTask {
        private String projectName;

        public WorkTimerTask(String projectName) {
            this.projectName = projectName;
        }

        public void run() {
            System.out.println(employeeRole + " agent (" + getLocalName() + ") finished working on project \"" + projectName + "\".");
            
            // Add the agent back to the DF because it is available to work again on a project
            registerToDF();
        }
    }
    
    private class ProjectRequestHandler extends CyclicBehaviour {
        public void action() {
        	// Listen for messages which are: REQUEST, not null and from the ManagerAgent
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);

            if (msg != null) {
            	String senderAgentName =  msg.getSender().getName();
            	
            	if (senderAgentName.startsWith("Manager")) {
                    String[] projectRequest = msg.getContent().split(",");
                    String projectName = projectRequest[0];
                    int workload = Integer.parseInt(projectRequest[1]);
                    int payment = Integer.parseInt(projectRequest[2]);

                    // Receive the payment (for the contribution to the project)
                    money += payment;
                    
                    System.out.println(employeeRole + " agent (" + getLocalName() + ") received " + payment + "$ (total money: " + money + "$).");
                    System.out.println(employeeRole + " agent (" + getLocalName() + ") started working on project \"" + projectName + "\".");

                    // Remove the agent from the DF because it is busy working on the project
                    deregisterFromDF();
                    
                    workTimer = new Timer();
                    workTimer.schedule(new WorkTimerTask(projectName), workload * 1000);
            	}
            } else {
            	// If no message is received, block this behaviour until a message is received
                block();
            }
        }
    }
}
