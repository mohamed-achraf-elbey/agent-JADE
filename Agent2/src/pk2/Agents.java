package pk2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class Agents extends Agent {
    private double totalCapacity;
    private double availableCapacity;
    private boolean isPrincipal;
    private String ORC = "";
    private static double totalPowerDistributed = 0;
    private static int totalRequestsHandled = 0;

   

    protected void setup() {

        if (getLocalName().equals("p")) {
            setupPrincipalAgent();
        } else {
            setupLocalAgent();
        }

        addBehaviour(createMessageHandler());
    }

    private void setupPrincipalAgent() {
        Random random = new Random();
        isPrincipal = true;
        totalCapacity = 16000 ;
        availableCapacity = totalCapacity;
        System.out.printf("Principal agent %s initial capacity: %.2f W%n", getLocalName(), totalCapacity);
        displayAgentStatus();

    }

    private void setupLocalAgent() {
        Random random = new Random();
        isPrincipal = false;
        if(getLocalName().equals("l1")) {
        	totalCapacity = 1000;
        }
        else {
        	totalCapacity = 15000;
        }
        //totalCapacity = 5000 + random.nextDouble() * 8000;
        availableCapacity = 0;
        System.out.println("Starting agent " + getLocalName());
        requestPowerFromP(totalCapacity);
        displayAgentStatus();

    }

    private CyclicBehaviour createMessageHandler() {
        return new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                    )
                );

                ACLMessage msg = receive(mt);
                if (msg != null) {
                    processMessage(msg);
                } else {
                    block();
                }
            }
        };
    }

    private void processMessage(ACLMessage msg) {
        try {
            switch (msg.getPerformative()) {
                case ACLMessage.INFORM:
                    reciveCapLocal(msg);
                    break;
                case ACLMessage.REQUEST:
                    powerRequests(msg);
                    break;
                case ACLMessage.REFUSE:
                    requestRejecte(msg);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private void reciveCapLocal(ACLMessage msg) {//p -> l or l -> l inform
        if (msg.getContent().startsWith("POWER_ALLOCATED:")) {
            double receivedPower = parseDouble(msg.getContent().split(":")[1]);
            
            if (!isPrincipal) {
                availableCapacity = receivedPower;
                totalCapacity = receivedPower;
                  System.out.printf("%s received power allocation: %.2f W%n", getLocalName(), receivedPower);
                  
            }
            
        }
    }
    
    private void powerRequests(ACLMessage msg) {
        AID sender = msg.getSender();
        String content = msg.getContent();
        
        if (content.startsWith("POWER_TRANSFER:")) {
            powerTransferRequest(msg);
            return;
        }

        if (content.startsWith("POWER_REQUEST:") || content.startsWith("ADDITIONAL_POWER:")) {
            double requestedPower = parseDouble(content.split(":")[1]);
            reponseRequest(sender, requestedPower);
        }
    }

    private void requestRejecte(ACLMessage msg) {
        System.out.println("Power request rejected: " + msg.getContent());
        
        if (msg.getSender().getLocalName().equals("p")) {//la 3ad p howa li dar refuse
            if (!isPrincipal) {
                String targetAgent = getLocalName().equals("l1") ? "l2" : "l1";
                
                ACLMessage powerRequest = new ACLMessage(ACLMessage.REQUEST);
                powerRequest.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
                
                powerRequest.setContent(String.format("POWER_TRANSFER:%s", 
                    ORC.split(":")[1]));
                send(powerRequest);
                
                System.out.println("Routing power transfer request to " + targetAgent + 
                                   " after principal agent rejection");
            }
        }
    }

    private void reponseRequest(AID sender, double requestedPower) {
        String senderName = sender.getLocalName();
        String agentType = determineAgentType(senderName);

        System.out.printf("===== Power Request Processing =====%n");
        System.out.printf("%s %s requested: %.2f W%n", agentType, senderName, requestedPower);
        System.out.printf("Current agent %s available capacity: %.2f W%n", getLocalName(), availableCapacity);
        //if(!agentType.equals("Unknown"))
        if (availableCapacity >= requestedPower) {
            allocatePower(sender, requestedPower); 
            sendPowerToORC(sender, requestedPower);
        } else {
            insufficientCap(sender, requestedPower);  
        }
    }
    
    private void allocatePower(AID sender, double requestedPower) {
        availableCapacity -= requestedPower;
        totalPowerDistributed += requestedPower;
        totalRequestsHandled++;
        
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(sender);
        reply.setContent(String.format("POWER_ALLOCATED:%.2f", requestedPower).replace(".", ","));
        send(reply);
        System.out.printf("===== Power Allocation Successful =====%n");
        System.out.printf("Allocated %.2f W to %s%n", requestedPower, sender.getLocalName());
        System.out.printf("Remaining capacity for %s: %.2f W%n", getLocalName(), availableCapacity);
            displayPowerTransaction("Allocation", sender.getLocalName(), requestedPower, "SUCCESS");
    }

    private String determineAgentType(String agentName) {
        if (agentName.startsWith("h")) return "House";
        if (agentName.startsWith("c")) return "Company";
        if (agentName.startsWith("p")) return "prancipale";
        if (agentName.startsWith("l")) return "local";
        return "Unknown";
    }

    

    private void insufficientCap(AID sender, double requestedPower) {
        if (!isPrincipal) {
            requestPowerFromP(requestedPower);
        }

        if (isPrincipal) {
            refusePowerRL(sender, requestedPower);
        }
    }

   

    private void refusePowerRL(AID sender, double requestedPower) {
        ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
        reply.addReceiver(sender);
        reply.setContent("POWER_DENIED:No available capacity");
        send(reply);
        
        System.out.printf("===== POWER REQUEST DENIED =====%n");
        System.out.printf("Cannot allocate %.2f W to %s%n", requestedPower, sender.getLocalName());
        
        displayPowerTransaction("Denial", sender.getLocalName(), requestedPower, "FAILED");
    }

    private void requestPowerFromP(double requiredPower) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("p", AID.ISLOCALNAME));
        msg.setContent(String.format("ADDITIONAL_POWER:%.2f", requiredPower).replace(".", ","));
        send(msg);
        
        ORC = msg.getContent();
        
        System.out.printf(" Requesting %.2f W from principal agent%n", requiredPower);
        
    }

    
    private static double parseDouble(String value) {
        return Double.parseDouble(value.replace(",", ".").trim());
    }
    
    

    private void powerTransferRequest(ACLMessage msg) {
        AID sender = msg.getSender();
        String content = msg.getContent();
        
        double requestedPower = parseDouble(content.split(":")[1]);
        
        System.out.printf("===== Power Transfer Request Processing =====%n");
        System.out.printf("Agent %s requesting power transfer: %.2f W%n", sender.getLocalName(), requestedPower);
        System.out.printf("Current agent %s available capacity: %.2f W%n", getLocalName(), availableCapacity);

        if (availableCapacity >= requestedPower) {
            availableCapacity -= requestedPower;
            
            ACLMessage transferConfirm = new ACLMessage(ACLMessage.INFORM);
            transferConfirm.addReceiver(sender);
            transferConfirm.setContent(String.format("POWER_TRANSFERRED:%.2f", requestedPower).replace(".", ","));
            send(transferConfirm);

            System.out.printf("===== Power Transfer Successful =====%n");
            System.out.printf("Transferred %.2f W to %s%n", requestedPower, sender.getLocalName());
            System.out.printf("Remaining capacity for %s: %.2f W%n", getLocalName(), availableCapacity);
            
            displayPowerTransaction("Transfer", sender.getLocalName(), requestedPower, "SUCCESS");
            displayAgentStatus();

        } else {
            ACLMessage transferReject = new ACLMessage(ACLMessage.REFUSE);
            transferReject.addReceiver(sender);
            transferReject.setContent(String.format("TRANSFER_DENIED:Insufficient capacity. Available: %.2f W", availableCapacity).replace(".", ","));
            send(transferReject);

            System.out.printf("===== Power Transfer Denied =====%n");
            System.out.printf("Cannot transfer %.2f W from %s. Insufficient capacity%n", requestedPower, getLocalName());
            
            displayPowerTransaction("Transfer Denial", sender.getLocalName(), requestedPower, "FAILED");
        }
    }
    
    public static double getTotalPowerDistributed() {
        return totalPowerDistributed;
    }

    public static int getTotalRequestsHandled() {
        return totalRequestsHandled;
    }

    private void displayAgentStatus() {
  
        
        double utilizationPercentage = ((totalCapacity - availableCapacity) / totalCapacity) * 100;

        StringBuilder status = new StringBuilder();
        status.append("===== Agent Status =====\n");
        status.append(String.format("Agent Name: %s%n", getLocalName()));
        status.append(String.format("Type: %s%n", isPrincipal ? "Principal" : "Local"));
        status.append(String.format("Total Capacity: %.2f W%n", totalCapacity));
        status.append(String.format("Available Capacity: %.2f W%n", availableCapacity));
        status.append(String.format("Utilization: %.2f%%%n", utilizationPercentage));
        
        System.out.println(status.toString());
    }

    private void displayPowerTransaction(String operation, String targetAgent, double power, String status) {
        StringBuilder transaction = new StringBuilder();
        transaction.append(String.format("=== Power %s ===%n", operation));
        transaction.append(String.format("Source: %s%n", getLocalName()));
        transaction.append(String.format("Target: %s%n", targetAgent));
        transaction.append(String.format("Amount: %.2f W%n", power));
        transaction.append(String.format("Status: %s%n", status));
        System.out.println(transaction.toString());
    }
    
    private void sendPowerToORC(AID sender, double allocatedPower) {
        ACLMessage powerConfirmation = new ACLMessage(ACLMessage.INFORM);
        powerConfirmation.addReceiver(sender); 
        powerConfirmation.setContent(String.format("POWER_ALLOCATED:%.2f", allocatedPower).replace(".", ","));
        send(powerConfirmation);  

        System.out.printf("Sending allocated power %.2f W to ORC (%s)%n", allocatedPower, sender.getLocalName());
    }
}