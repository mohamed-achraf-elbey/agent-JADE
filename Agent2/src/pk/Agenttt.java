package pk;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

public class Agenttt extends Agent {
    private Double capacity;
    private boolean status;
    private Double cosumCap = 0.0 ;
    
    private final int onlineStart = 8;
    private final int onlineEnd = 16;
    private int numSiteOnline = 0 ;
    private int numSiteOffline = 0 ;

    private int time ;
    
    // Only for local agents
    private List<House> houses = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " is starting.");
        initializeAgent();
        
    }
    
    private void initializeAgent() {
    	if (getLocalName().equals("p")) {
        	status = true ;
            capacity = setRandomCapacity(80000, 90000);  // Assign the random capacity to the field
            System.out.println("Principal agent " + getLocalName() + " capacity: " + String.format("%.2f", capacity)  + " W");
            sendCapacityToLocalAgents();
            System.out.println("Principal agent " + getLocalName() + " capacity after send to local agents : " + String.format("%.2f", capacity)  + " W");
            
        } else {
        	status = true ;
        	initializeHouses();
        	displayAllHousesComponents();
            receiveCapacity();
        }
    }
    
    private void updateStatusP(int time) { // for simulation purposes
        if (time >= 8 && time <= 16)
            status = false ; 
    }
    
    private void receiveMsgP() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    ACLMessage reply = msg.createReply();
                    if (status && Math.random() > 0.5) {
                        reply.setContent("Accepted");
                    } else {
                        reply.setContent("Rejected");
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }

    // Method to send capacity to local agents
    private void sendCapacityToLocalAgents() {
        if (getLocalName().equals("p")) {
            ACLMessage msgL1 = new ACLMessage(ACLMessage.REQUEST);
            ACLMessage msgL2 = new ACLMessage(ACLMessage.REQUEST);

            double capacityL1 = setRandomCapacity(20000, 30000);
            if (capacity >= capacityL1) {
                msgL1.setContent("" + capacityL1);
                capacity -= capacityL1;
            } else {
                msgL1.setContent("reject");
            }
            msgL1.addReceiver(getAID("l1"));
            
            double capacityL2 = setRandomCapacity(20000, 30000);
            if (capacity >= capacityL2) {
                msgL2.setContent("" + capacityL2);
                capacity -= capacityL2;
            } else {
                msgL2.setContent("reject");
            }
            msgL2.addReceiver(getAID("l2"));
            
            send(msgL1);
            send(msgL2);  
            System.out.println(getLocalName() + " sent capacity requests to local agents.");
        }
    }

    // Method for local agents to receive capacity messages
    private void receiveCapacity() {
    	if (!getLocalName().equals("p"))
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if (content.equals("reject")) {
                        System.out.println(getLocalName() + " received: Insufficient capacity from principal.");
                    } else {
                        capacity = Double.parseDouble(content);
                        System.out.println(getLocalName() + " received capacity: " + String.format("%.2f", capacity)  + " W.");
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    // Helper method to generate a random capacity
    private double setRandomCapacity(int minCapacity, int maxCapacity) {
        Random random = new Random();
        return minCapacity + (maxCapacity - minCapacity) * random.nextDouble();
    }
    
    
    
    public void initializeHouses() {
        Random random = new Random();
        int numHouses = 4 + random.nextInt(3); 

        for (int i = 0; i < numHouses; i++) {
            House newHouse = House.initializeHouse();
            houses.add(newHouse);
        }

        System.out.println("Agent " + getLocalName() + " initialized " + numHouses + " houses.");
    }
    
    public void displayAllHousesComponents() {
        if (houses.isEmpty()) {
            System.out.println("No houses to display for Agent " + getLocalName());
            return;
        }

        System.out.println("Displaying components for each house managed by Agent " + getLocalName() + ":");

        int houseNumber = 1;
        for (House house : houses) {
            System.out.println("House " + houseNumber + " components:");
            //house.displayComponents();  
            System.out.println("Total consumption for House " + houseNumber + ": " + String.format("%.2f", house.getConsumePower()) + " W in 1H for agent "+ getLocalName());
            cosumCap += house.getConsumePower();
            
            System.out.println("----------------------------------");
            houseNumber++;
        }
        System.err.println("Total consumption for  " + getLocalName() + " : " + String.format("%.2f", cosumCap) + " W in 1H ");
    }


}
