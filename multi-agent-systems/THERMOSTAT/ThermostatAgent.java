package thermo.mas;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.core.behaviours.*;

public class ThermostatAgent extends Agent {
	private int  thermostatTemperature;
	private boolean isTemperatureOk = false;
	private String agentName;

	protected void setup() {
		agentName = getAID().getName();
		
		System.out.println(agentName + " is ready.");
		
		addBehaviour(new RequestTemperatureFromEnvironment());
		
	}
	
	public class RequestTemperatureFromEnvironment extends OneShotBehaviour {
		public void action() {
			ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
			
			requestMsg.addReceiver(new AID("env", AID.ISLOCALNAME));
			requestMsg.setContent("give me the temperature");
			
			send(requestMsg);
			
			addBehaviour(new ReceiveMessageFromEnvironment());
		}
	}
	
	public class ReceiveMessageFromEnvironment extends CyclicBehaviour {
		public void action() {
			ACLMessage responseMsg = myAgent.receive();
			
			if (responseMsg != null) {
				thermostatTemperature = Integer.parseInt(responseMsg.getContent());

				addBehaviour(new IncreaseOrDecreaseTemperature());
			} else {
				block();
			}
		}
	}
	
	public class IncreaseOrDecreaseTemperature extends Behaviour {
		public void action() {
			System.out.println("Thermostat temperature: " + thermostatTemperature);
			
			if (thermostatTemperature >= 22) {
				thermostatTemperature--;
			} else if (thermostatTemperature <= 18) {
				thermostatTemperature++;
			} else {
				isTemperatureOk = true;

				doDelete();
			}
		}
		
		public boolean done() {
			System.out.println("isTemperatureOk: " + isTemperatureOk);
			
			return isTemperatureOk;
		}
 	}
	
	protected void takeDown() {
		System.out.println(agentName + " terminating.");
	}
}
