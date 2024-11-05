package thermo.mas;
import jade.core.Agent;
import jade.lang.acl.*;
import jade.core.behaviours.*;

public class EnvironmentAgent extends Agent {
	private int temperature;
	private String agentName;
	
	protected void setup() {
		agentName = getAID().getName();
		
		System.out.println(agentName + " is ready.");
		
		Object[] args = getArguments();
				
		if (args != null && args.length > 0) {
			temperature = Integer.parseInt((String) args[0]);

			System.out.println("The environment temperature is " + temperature + "!");
		} else {
			System.out.println("No environment temperature was specified.");

			doDelete();
		}
		
		addBehaviour(new ReceiveMessageFromThermostat());
	}
	
	public class ReceiveMessageFromThermostat extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = myAgent.receive();
			
			if (msg != null && msg.getContent().equals("give me the temperature")) {
				ACLMessage responseMsg = msg.createReply();
				
				responseMsg.setPerformative(ACLMessage.INFORM);
				responseMsg.setContent(String.valueOf(temperature));

				send(responseMsg);
			} else {
				block();
			}
		}
	}
	
	protected void takeDown() {
		System.out.println(agentName + " terminating.");
	}
}
