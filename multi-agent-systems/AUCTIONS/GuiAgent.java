package auctions.mas;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GuiAgent extends Agent {
    private JFrame frame;
    private AuctionCreatorPanel auctionCreatorPanel;
    private ItemSearchingPanel itemSearchingPanel;
    private ItemBiddingPanel itemBiddingPanel;
    private String iteratorNumber;
    private String searchItem;
    private boolean isAuctionOngoing = false;
    private JComboBox<String> typeComboBox;

    protected void setup() {
        System.out.println("Gui Agent " + getAID().getName() + " is ready.");
        String agentName = getAID().getName();
        String[] nameParts = agentName.split("@");
        iteratorNumber = nameParts[0].replaceAll("[^\\d]", "");

        addBehaviour(new ReceiveMessages());
        
        createAndShowGUI();
    }

    private void updateMessageInBiddingPanel(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                itemBiddingPanel.updateMessage(message);
            }
        });
    }
    
    private class ReceiveMessages extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            
            if (msg != null) {
                String senderAgentName = msg.getSender().getName();
                String content = msg.getContent();
                if (senderAgentName.startsWith("Search")) {
                	if (content.equals("found")) {
                		createBiddingAgent();
                	} else {
                		System.out.println("The requested item was not found!");
                	}
                } else if (senderAgentName.startsWith("Bidding-" + iteratorNumber)) {
                	switch(content) {
	              	  case "start-bidding":
	              		  isAuctionOngoing = true;
	            		  updateMessageInBiddingPanel("Place bids!");
	            	    break;
                	  case "won-the-auction":
                		  isAuctionOngoing = false;
                		  updateMessageInBiddingPanel("You won the auction!");
                	    break;
                	  case "lost-the-auction":
                		  isAuctionOngoing = false;
                		  updateMessageInBiddingPanel("You lost the auction!");
                	    break;
                	  default:
                		  if (content.startsWith("new-bid:")) {
                		    String bidString = content.substring("new-bid:".length());

                          	updateMessageInBiddingPanel("The best bid: " + bidString.trim());
                		  } else if (content.startsWith("new-dutch-bid:")) {
                		    String bidString = content.substring("new-dutch-bid:".length());
                			isAuctionOngoing = true;

                        	updateMessageInBiddingPanel("The minimum required bid (dutch): " + bidString.trim());
                		  }
                	}
                }
            }
        }
    }
    
    protected void takeDown() {
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("Gui Agent " + getAID().getName() + " is terminating.");
    }

    private void createAndShowGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame = new JFrame(getAID().getName());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JTabbedPane tabbedPane = new JTabbedPane();
                frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

                auctionCreatorPanel = new AuctionCreatorPanel();
                tabbedPane.addTab("Create Auction", null, auctionCreatorPanel, "Create Auction");

                itemSearchingPanel = new ItemSearchingPanel();
                tabbedPane.addTab("Search Item", null, itemSearchingPanel, "Search Item");
                
                itemBiddingPanel = new ItemBiddingPanel();
                tabbedPane.addTab("Bid on Item", null, itemBiddingPanel, "Bid on Item");

                frame.pack();
                frame.setVisible(true);
                frame.requestFocus();
            }
        });
    }

    private class AuctionCreatorPanel extends JPanel implements ActionListener {
        private JTextField itemField;

        public AuctionCreatorPanel() {
            setLayout(new GridBagLayout());

            JLabel itemLabel = new JLabel("Item:");
            itemField = new JTextField(20);
            JButton createAuctionButton = new JButton("Create");
            createAuctionButton.addActionListener(this);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            add(itemLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(itemField, gbc);

            JLabel typeLabel = new JLabel("Auction Type:");
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            add(typeLabel, gbc);

            typeComboBox = new JComboBox<>(new String[]{"English", "Dutch"});
            typeComboBox.setSelectedIndex(0);
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(typeComboBox, gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            add(createAuctionButton, gbc);
        }

        public void actionPerformed(ActionEvent e) {
            String item = itemField.getText();
            String auctionType = (String) typeComboBox.getSelectedItem();
            String messageContent = item + ";" + auctionType; // Combine item and type with a delimiter

            sendMessageToMainAgent(messageContent);
        }

    }


    private class ItemSearchingPanel extends JPanel implements ActionListener {
        private JTextField itemField;

        public ItemSearchingPanel() {
            setLayout(new GridBagLayout());
            JLabel itemLabel = new JLabel("Item:");
            itemField = new JTextField(20);
            JButton searchButton = new JButton("Search");
            searchButton.addActionListener(this);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            add(itemLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(itemField, gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            add(searchButton, gbc);
        }

        public void actionPerformed(ActionEvent e) {
            searchItem = itemField.getText();
            createSearchAgent();
        }
    }
    
    private class ItemBiddingPanel extends JPanel implements ActionListener {
        private JTextField bidAmount;
        private JLabel messageLabel;

        public ItemBiddingPanel() {
            setLayout(new GridBagLayout());
            JLabel itemLabel = new JLabel("Bid amount:");
            bidAmount = new JTextField(20);
            JButton bidButton = new JButton("Bid");
            bidButton.addActionListener(this);
            messageLabel = new JLabel("The auction is not open yet!");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            add(itemLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(bidAmount, gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            add(bidButton, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            add(messageLabel, gbc);
        }

        public void actionPerformed(ActionEvent e) {
            if (isAuctionOngoing) {
              String amount = bidAmount.getText();
              ACLMessage message = new ACLMessage(ACLMessage.INFORM);
              message.setContent(amount);
              message.addReceiver(new AID("Bidding-" + iteratorNumber, AID.ISLOCALNAME));
              send(message);
            }
        }

        public void updateMessage(String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    messageLabel.setText(message);
                }
            });
        }
    }

    private void sendMessageToMainAgent(String content) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent(content);
        message.addReceiver(new AID("Main", AID.ISLOCALNAME));
        send(message);
    }

    
    private void createBiddingAgent() {
        try {
            Object[] arguments = new Object[]{iteratorNumber, searchItem};
            getContainerController().createNewAgent("Bidding-" + iteratorNumber, BiddingAgent.class.getName(), arguments).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSearchAgent() {
        try {
            Object[] arguments = new Object[]{iteratorNumber, searchItem};
            getContainerController().createNewAgent("Search-" + iteratorNumber, SearchAgent.class.getName(), arguments).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
