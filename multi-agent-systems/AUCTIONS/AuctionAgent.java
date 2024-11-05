package auctions.mas;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class AuctionAgent extends Agent {
	private String item;
	private String auctionType;
	private List<String> registeredUsers;
	private double currentPrice = 0;
	private String winner;
	private boolean isRegistrationIsOpen = true;
	private int requiredDutchBid = 1100;

	protected void setup() {
		Object[] args = getArguments();

		if (args != null && args.length == 2) {
			item = (String) args[0];
			auctionType = (String) args[1];
		} else {
			System.out.println("Error: Auction details not specified.");
			doDelete();
			return;
		}

		System.out.println("Auction Agent " + getAID().getName() + " is ready.");

		registeredUsers = new ArrayList<>();
		currentPrice = 0;
		winner = null;

		addBehaviour(new CloseRegistrationBehaviour(this, 30000));
		addBehaviour(new ReceiveMessages());
	}

	protected void takeDown() {
		System.out.println("Auction Agent " + getAID().getName() + " is terminating.");
	}

	public void notifyRegisteredUsers(String message) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

		for (String user : registeredUsers) {
			msg.addReceiver(getAID(user));
		}

		msg.setContent(message);
		send(msg);
	}

	private class SendRequiredBidForDutchAuctionBehaviour extends WakerBehaviour {
		public SendRequiredBidForDutchAuctionBehaviour(Agent agent, long timeout) {
			super(agent, timeout);
		}

		protected void handleElapsedTimeout() {
			if (requiredDutchBid != 0) {
				requiredDutchBid -= 100;	
			}
			
			System.out.println("The required minimum bid for the dutch auction is " + requiredDutchBid);
			notifyRegisteredUsers("new-dutch-bid:" + requiredDutchBid);
			
			myAgent.addBehaviour(new SendRequiredBidForDutchAuctionBehaviour(myAgent, 10000));
		}
	}
	
	private class CloseRegistrationBehaviour extends WakerBehaviour {
		public CloseRegistrationBehaviour(Agent agent, long timeout) {
			super(agent, timeout);
		}

		protected void handleElapsedTimeout() {
			isRegistrationIsOpen = false;

			System.out.println("The registration period for the " + item + " has ended! The bidding can start!");
			
			if (auctionType.equals("English")) {
				notifyRegisteredUsers("start-bidding");
			} else {
				myAgent.addBehaviour(new SendRequiredBidForDutchAuctionBehaviour(myAgent, 0));
			}
		}
	}

	public void endAuction(String winner) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		String message = "won-the-auction";
		msg.addReceiver(getAID(winner));
		msg.setContent(message);
		send(msg);
		registeredUsers.remove(winner);
		notifyRegisteredUsers("lost-the-auction");
		System.out.println("The auction for the " + item + " has ended!");

		doDelete();
	}
	
	private class UserWonTheAuctionBehaviour extends WakerBehaviour {
		private String winnerAgent;

		public UserWonTheAuctionBehaviour(Agent agent, long timeout, String winnerAgent) {
			super(agent, timeout);
			this.winnerAgent = winnerAgent;
		}

		protected void handleElapsedTimeout() {
			if (winnerAgent.equals(winner)) {
				endAuction(winner);
			}
		}
	}

	private class ReceiveMessages extends CyclicBehaviour {
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				String content = msg.getContent();

				if (msg.getPerformative() == ACLMessage.INFORM && isRegistrationIsOpen) {
					handleRegistrationRequest(msg.getSender().getLocalName());
				} else if (msg.getPerformative() == ACLMessage.REQUEST) {
					handleBidRequest(msg.getSender().getLocalName(), Double.parseDouble(content));
				}
			}
		}

		private void handleRegistrationRequest(String user) {
			if (!registeredUsers.contains(user)) {
				registeredUsers.add(user);
				System.out.println("User " + user + " registered for the auction of the " + item + " item!");
			}
		}

		private void handleBidRequest(String user, double bid) {
			if (auctionType.equals("English")) {
				if (bid > currentPrice) {
					currentPrice = bid;
					winner = user;
					System.out.println("User " + user + " placed a bid of " + bid);
					notifyRegisteredUsers("new-bid:" + bid);
	
					addBehaviour(new UserWonTheAuctionBehaviour(getAgent(), 10000, user));
				}	
			} else {
				if (bid >= requiredDutchBid) {
					endAuction(user);
				}
			}
		}
	}
}
