package company.mas;

// import company.mas.Salaries;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ManagerAgent extends EmployeePaidMonthlyAgent {
	Map<String, Integer> salaries = Salaries.salaries;
	
    protected void setup() {
        super.setup("Manager");

        // Listen for requests from the ClientAgents
        addBehaviour(new RequestHandler());
    }
    
    private List<AID> getEmployeesForProject(String employeeRole) {
    	List<AID> employeesForProject = new ArrayList<AID>();
    	
    	// Create a DFAgentDescription object to search for specific employees
    	DFAgentDescription dfad = new DFAgentDescription();
    	ServiceDescription sd = new ServiceDescription();
    	
    	sd.setType(employeeRole);
    	dfad.addServices(sd);

    	try {
    	    // Search the DF for specific employees and add their AIDs to the list
    	    DFAgentDescription[] result = DFService.search(this, dfad);
    	    
    	    for (DFAgentDescription agent : result) {
    	    	employeesForProject.add(agent.getName());
    	    }
    	} catch (FIPAException e) {
    	    e.printStackTrace();
    	}
    	
    	return employeesForProject;
    }
    
    public void sendProjectAndMoneyToEmployees(String projectName, int workload, int nrOfEmployeesNeeded,  String employeeRole) {
        ACLMessage projectRequest = new ACLMessage(ACLMessage.REQUEST);
        
        // Search for available employees (for one specific role)
        List<AID> employeesForProject = getEmployeesForProject(employeeRole);
        
        for (int i = 0; i < nrOfEmployeesNeeded; i++) {
            projectRequest.addReceiver(employeesForProject.get(i));
        }

        projectRequest.setContent(projectName + "," + workload + "," +  Integer.toString(salaries.get(employeeRole) * workload));

        send(projectRequest);
    }
    
    public int getTheMissingNrOfEmployeesForProject(String employeeRole, int nrOfEmployeesNeeded) {
        int nrOfEmployeesAvailable = 0;
        
        DFAgentDescription dfad = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(employeeRole);
        dfad.addServices(sd);

        // Search for available employees (for one specific role)
        try {
            DFAgentDescription[] result = DFService.search(this, dfad);

            nrOfEmployeesAvailable = result.length;
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }
        
        int missingNrOfEmployees = nrOfEmployeesNeeded - nrOfEmployeesAvailable;

        if (missingNrOfEmployees > 0) {
        	return missingNrOfEmployees;
        }
        
        // If there are more employees than needed the missing number of employees is 0
        return 0;
    }
    
    public void findEmployeesForProject(Project project, ACLMessage msg, int nrOfUiUxNeeded, int nrOfDevelopersNeeded, int nrOfQaNeeded) {
    	String projectName = project.getProjectName();
    	int workload = project.getWorkload();
    	int missingNrOfUiUx = getTheMissingNrOfEmployeesForProject("UiUx", nrOfUiUxNeeded);
    	int missingNrOfDevelopers = getTheMissingNrOfEmployeesForProject("Developer", nrOfDevelopersNeeded);
    	int missingNrOfQa = getTheMissingNrOfEmployeesForProject("Qa", nrOfQaNeeded);
    	
    	// If there are not enough UiUxAgents, DeveloperAgents or QaAgents send a request to the HrAgent to hire people
    	if (missingNrOfUiUx != 0 || missingNrOfDevelopers != 0 || missingNrOfQa != 0) {
    	    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
    	    
    	    message.addReceiver(new AID("Hr", AID.ISLOCALNAME));
    	    message.setContent(missingNrOfUiUx + "," + missingNrOfDevelopers + "," + missingNrOfQa);
    	    
    	    send(message);

    	    // Listen for messages which are: ACCEPT_PROPOSAL and from the HrAgent
    	    MessageTemplate mt = MessageTemplate.and(
    	    	    MessageTemplate.MatchSender(new AID("Hr", AID.ISLOCALNAME)),
    	    	    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)
    	    	);

	    	boolean receivedResponse = false;
	    	
	    	// Wait for the message from the HrAgent
	    	while (!receivedResponse) {
	    	    ACLMessage response = receive(mt);
	    	    
	    	    if (response != null) {
	    	        receivedResponse = true;
	    	    }
	    	}
	    	
	    	// In order to see the hiring process nicer (the logs) we add 3 seconds delay (can be removed if we aim for performance)
        	try {
        	    Thread.sleep(3000);
        	} catch (InterruptedException e) {
        	    System.out.print("Interrupted exception!");
        	}
    	}
    	
    	sendProjectAndMoneyToEmployees(projectName, workload, nrOfUiUxNeeded, "UiUx");
    	sendProjectAndMoneyToEmployees(projectName, workload, nrOfDevelopersNeeded, "Developer");
    	sendProjectAndMoneyToEmployees(projectName, workload, nrOfQaNeeded, "Qa");
    }
    
    public void checkIfClientHasEnoughBudget(Project project, ACLMessage msg, int nrOfUiUxNeeded, int nrOfDevelopersNeeded, int nrOfQaNeeded) {
    	int workload = project.getWorkload();
        int totalCost = (nrOfUiUxNeeded * salaries.get("UiUx") * workload) +
                        (nrOfDevelopersNeeded * salaries.get("Developer") * workload) +
                        (nrOfQaNeeded * salaries.get("Qa") * workload);
        int remainedBudget = project.getBudget() - totalCost;

        // If there was enough budget (to pay the employees which will work on the project) the project is accepted 
        if (remainedBudget >= 0) {
        	// Tell the ClientAgent that the project was accepted
        	ACLMessage acceptMsg = msg.createReply();
        	
        	acceptMsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        	acceptMsg.setContent("The project has been accepted by our company.");
        	
        	send(acceptMsg);
        	
        	// If there is any money left after paying the employees who will work on the project, send the rest of the money to the CeoAgent
            if (remainedBudget > 0) {
            	ACLMessage remainedBudgetMsg = new ACLMessage(ACLMessage.INFORM);
            	
            	remainedBudgetMsg.addReceiver(new AID("Ceo", AID.ISLOCALNAME));
            	remainedBudgetMsg.setContent(Integer.toString(remainedBudget));
            	
            	send(remainedBudgetMsg);	
            }
            
            findEmployeesForProject(project, msg, nrOfUiUxNeeded, nrOfDevelopersNeeded, nrOfQaNeeded);
        } else {
        	// Tell the ClientAgent that the project was refused
            ACLMessage reply = msg.createReply();
            
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Budget not sufficient for this project.");
            
            send(reply);
        }
    }
    
    public void handleClientRequest(ACLMessage msg) {
        try {
            // Parse the message content as a Project object
            Object contentObj = msg.getContentObject();

            if (contentObj instanceof Project) {
                Project project = (Project) contentObj;

                // Based on the type of the project check if the client has enough budget to pay for the project
                switch (project.getProjectType()) {
                    case "PresentationSite":
                    	checkIfClientHasEnoughBudget(project, msg, 2, 1, 1);
                    	
                        break;
                    case "ECommerceSite":
                    	checkIfClientHasEnoughBudget(project, msg, 1, 3, 2);
                    	
                        break;
                    case "BankingSystem":
                    	checkIfClientHasEnoughBudget(project, msg, 1, 3, 5);
                    	
                        break;
                    default:
                        // Unknown project type, send REFUSE message to the ClientAgent
                        ACLMessage refuseMsg = msg.createReply();
                        
                        refuseMsg.setPerformative(ACLMessage.REFUSE);
                        refuseMsg.setContent("Unknown project type! The ManagerAgent can not accept it.");
                        
                        send(refuseMsg);

                        break;
                }
            }
        } catch (UnreadableException e) {
        	System.out.println("The message from the " + msg.getSender().getName()  + " could not be parsed!");
        }
    }

    private class RequestHandler extends CyclicBehaviour {
        public void action() {
        	// Listen for messages which are: REQUEST, not null and from the ClientAgents
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);

            if (msg != null) {
        		String senderAgentName =  msg.getSender().getName();

            	if (senderAgentName.startsWith("Client")) {
            		handleClientRequest(msg);
            	}
            } else {
            	// If no message is received, block this behaviour until a message is received
                block();
            }
        }
    }
}
