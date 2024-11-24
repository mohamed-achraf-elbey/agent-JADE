package pk2;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Agents extends Agent{
	 private Double capacity = 0.0;
	 private boolean status;
	 
	 @Override
	    protected void setup() {
	        System.out.println("Agent " + getLocalName() + " is starting ");
	        initializeAgent();
	        
	    }
	 
	 private void initializeAgent() {
	        addBehaviour(new OneShotBehaviour() {
	            public void action() {
	                if (getLocalName().equals("p")) {
	                    status = true;
	                    capacity = setRandomCapacity(80000, 90000);
	                    System.out.println("Principal agent " + getLocalName() + " initial capacity: " + String.format("%.2f", capacity) + " W");
	                    sendMsgCapa();
	                } else {
	                    status = true;
	                    receiveCapacity();	 
	                    }
	            }
	        });
	    }
	 
	 private double setRandomCapacity(int minCap, int maxCap) {
	        Random random = new Random();
	        return minCap + (maxCap - minCap) * random.nextDouble();
	    }
	 
	 private void sendMsgCapa() {
		    if (getLocalName().equals("p")) {
		        double capacityL1 = setRandomCapacity(20000, 30000);
		        double capacityL2 = setRandomCapacity(20000, 30000);

		        if (capacity < capacityL1 + capacityL2) {
		            status = false; // Principal agent goes offline if not enough capacity
		            System.out.println("Principal agent " + getLocalName() + " is offline due to insufficient capacity.");
		        } else {
		            sendCapacity("l1", capacityL1);
		            sendCapacity("l2", capacityL2);
		            System.out.println("P agent sent capacity requests to local agents.");
		        }
		    }
		}

		private void sendCapacity(String localAgentName, double requestedCapacity) {
		    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		    msg.addReceiver(getAID(localAgentName));
		    if (capacity >= requestedCapacity) {
		        capacity -= requestedCapacity;
		        msg.setContent("" + requestedCapacity);
		    } else {
		        msg.setContent("reject");
		    }
		    send(msg);
		}
		
		private void receiveCapacity() {
	        addBehaviour(new CyclicBehaviour() {
	            public void action() {
	                ACLMessage msg = receive();
	                if (msg != null) {
	                    String content = msg.getContent();
	                    if ("reject".equals(content)) {
	                        status = false;
	                        System.out.println(getLocalName() + " received: Insufficient capacity from principal.");
	                    } else {
	                        capacity = Double.parseDouble(content);
	                        status = true;
	                        System.out.println(getLocalName() + " received updated capacity: " + String.format("%.2f", capacity) + " W.");
	                    }
	                } else {
	                    block();
	                }
	            }
	        });
	    }

	 
	 
}
