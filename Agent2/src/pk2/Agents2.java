package pk2;

import java.util.Random;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Agents2 extends Agents{
	 private double totalCapacity;
	    private double availableCapacity;
	    private boolean isPrincipal;
	    private ACLMessage ORC ;
	    private static double totalPowerDistributed = 0;
	    private static int totalRequestsHandled = 0;
	    private int inslutione = 0 ; 
	    protected void setup() {

	        if (getLocalName().equals("p")) {
	            setupPrincipalAgent();
	        } else {
	            setupLocalAgent();
	        }

	        addBehaviour(createMsg());
	    }
	    private void setupPrincipalAgent() {
	        Random random = new Random();
	        isPrincipal = true;
	        totalCapacity = 26000 ;
	        availableCapacity = totalCapacity;
	        System.out.printf("Principal agent %s initial capacity: %.2f W%n", getLocalName(), totalCapacity);
	        //displayAgentStatus();
	    }

	    private void setupLocalAgent() {
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
	       // displayAgentStatus();

	    }
	    

	    private CyclicBehaviour createMsg() {
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
	                    processMessagepP(msg);
	                    processMessagepL(msg);
	                } else {
	                    block();
	                }
	            }
	        };
	    }

	    private void processMessagepP(ACLMessage msg) {
	    	if(getLocalName().equals("p"))
	        try {
	            switch (msg.getPerformative()) {
	              
	                case ACLMessage.REQUEST:
	                    powerRequestsP(msg);
	                    break;
	               
	            }
	        } catch (Exception e) {
	            System.err.println("Error processing message: " + e.getMessage());
	        }
	    }
	    
	    private void processMessagepL(ACLMessage msg) {
	    	if(getLocalName().equals("l1") || getLocalName().equals("l2"))
	        try {
	            switch (msg.getPerformative()) {
	                case ACLMessage.INFORM:
	                    reciveCapLocal(msg);
	                    break;
	                case ACLMessage.REQUEST:
	                   powerRequestsL(msg);
	                    break;
	                case ACLMessage.REFUSE:
	                   requestRejecte(msg);//***************************************************************
	                    break;
	            }
	        } catch (Exception e) {
	            System.err.println("Error processing message: " + e.getMessage());
	        }
	    }
	    
	    public void powerRequestsP(ACLMessage msg) {
	    	AID sender = msg.getSender();
	        String content = msg.getContent();
	        if (content.startsWith("POWER_REQUEST:") || content.startsWith("ADDITIONAL_POWER:")) {
	            double requestedPower = parseDouble(content.split(":")[1]);
	            //reponseRequest(sender, requestedPower);
	            if (availableCapacity >= requestedPower) {
	                allocatePower(sender, requestedPower); 
	               
	            } else {
	                //insufficientCap(sender, requestedPower);
	                refusePowerRL(sender,requestedPower);
	                
	            }
	        }
	    }
	    public void powerRequestsL(ACLMessage msg) {
	    	AID sender = msg.getSender();
	        String content = msg.getContent();
            double requestedPower = parseDouble(content.split(":")[1]);

	        if (content.startsWith("POWER_REQUEST:") || content.startsWith("ADDITIONAL_POWER:")) {
	        	ORC = msg;
	            if (availableCapacity >= requestedPower) {
	                allocatePower(sender, requestedPower); 
	                
	            } else {
	             
	                requestPowerFromP(requestedPower);

	            }
	        }else{if(content.startsWith("POWER_TRANSFER:")){
	        	System.out.println("ffffffffffff");
	        	
                if (availableCapacity >= requestedPower) {
                    availableCapacity -= requestedPower;
                    
                    ACLMessage transferConfirm = new ACLMessage(ACLMessage.INFORM);
                    transferConfirm.addReceiver(sender);
                    transferConfirm.setContent(String.format("POWER_TRANSFERRED:%.2f", requestedPower).replace(".", ","));
                    send(transferConfirm);

                    System.out.printf("===== Power Transfer Successful =====%n");
                    System.out.printf("Transferred %.2f W to %s%n", requestedPower, sender.getLocalName());
                    System.out.printf("Remaining capacity for %s: %.2f W%n", getLocalName(), availableCapacity);
                }else {
                    ACLMessage transferReject = new ACLMessage(ACLMessage.REFUSE);
                    transferReject.addReceiver(sender);
                    transferReject.setContent(String.format("TRANSFER_DENIED:Insufficient capacity. Available: %.2f W", availableCapacity).replace(".", ","));
                    send(transferReject);

                    System.out.printf("===== Power Transfer Denied =====%n");
                    System.out.printf("Cannot transfer %.2f W from %s. Insufficient capacity%n", requestedPower, getLocalName());}
	        }
	        	
	        }
	        	
	        }
	    
	    private void allocatePower(AID sender, double requestedPower) {
	        availableCapacity -= requestedPower;
	        totalPowerDistributed += requestedPower;
	        totalRequestsHandled++;
	        
	        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
	        System.out.println("************************** "+getLocalName()+" send power for "+sender.getLocalName()+" power = "+requestedPower+" and availableCapacity = "+availableCapacity);
	        reply.addReceiver(sender);
	        reply.setContent(String.format("POWER_ALLOCATED:%.2f", requestedPower).replace(".", ","));
	        send(reply);
	        /*System.out.printf("===== Power Allocation Successful =====%n");
	        System.out.printf("Allocated %.2f W to %s%n", requestedPower, sender.getLocalName());
	        System.out.printf("Remaining capacity for %s: %.2f W%n", getLocalName(), availableCapacity);
	           // displayPowerTransaction("Allocation", sender.getLocalName(), requestedPower, "SUCCESS");*/
	    }
	    
	    private void refusePowerRL(AID sender, double requestedPower) {
	        ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
	        reply.addReceiver(sender);
	        reply.setContent("POWER_DENIED:No available capacity");
	        send(reply);
	        
	        System.out.printf("===== POWER REQUEST DENIED =====%n");
	        System.out.printf("Cannot allocate %.2f W to %s%n", requestedPower, sender.getLocalName());
	        
	        //displayPowerTransaction("Denial", sender.getLocalName(), requestedPower, "FAILED");
	    }
	    
	    private void requestPowerFromP(double requiredPower) {
	        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	        msg.addReceiver(new AID("p", AID.ISLOCALNAME));
	        msg.setContent(String.format("ADDITIONAL_POWER:%.2f", requiredPower).replace(".", ","));
	        send(msg);
	        
	      //  ORC = msg.getContent();
	        
	        System.out.printf(" *********************Requesting %.2f W from principal agent%n", requiredPower);
	        
	    }
	    
	    private static double parseDouble(String value) {
	        return Double.parseDouble(value.replace(",", ".").trim());
	    }
	    
	    
	    private void reciveCapLocal(ACLMessage msg) {
	        String content = msg.getContent();
	        
	        if (content.startsWith("POWER_ALLOCATED:") || content.startsWith("POWER_TRANSFERRED:")) {
                      if( content.startsWith("POWER_TRANSFERRED:"))System.out.println("power transver ............");
	            double receivedPower = parseDouble(content.split(":")[1]);
	            
	            if (!isPrincipal) {
	                availableCapacity = receivedPower;
	                totalCapacity = receivedPower;
	                
	                System.out.printf("****************%s received power allocation: %.2f W%n", getLocalName(), receivedPower);
	                
	                if (ORC != null) {
	                    try {
	                        AID sender = ORC.getSender();
	                        String originalContent = ORC.getContent();
	                        double requestedPower = parseDouble(originalContent.split(":")[1]);
	                        
	                        allocatePower(sender, requestedPower);
	                    } catch (Exception e) {
	                        System.err.println("Error processing original request: " + e.getMessage());
	                    }
	                }
	            }
	        }
	    }
	    private void requestRejecte(ACLMessage msg) {
	        try {
	            System.out.println("Power request rejected: " + msg.getContent());
	            
	            if (msg.getSender().getLocalName().equals("p")) {
	                if (!isPrincipal) {
	                    String currentAgent = getLocalName();
	                    String targetAgent = currentAgent.equals("l1") ? "l2" : "l1";
	                    
	                    double requestedPower = 0;
	                    try {
	                        if (ORC != null && ORC.getContent() != null) {
	                        	
	                            String content = ORC.getContent();
	                            
	                            if (content.contains(":")) {
	                                requestedPower = parseDouble(content.split(":")[1]);
	                            }
	                        }
	                    } catch (Exception e) {
	                        System.err.println("Error parsing power request: " + e.getMessage());
	                        return;
	                    }
	                    String content2 = ORC.getContent();
			            double requestedPower2 = parseDouble(content2.split(":")[1]);
	                    double transferPower = requestedPower2;
	                    
	                    ACLMessage powerRequest = new ACLMessage(ACLMessage.REQUEST);
	                    powerRequest.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
	                    powerRequest.setContent(String.format("POWER_TRANSFER:%.2f", transferPower).replace(".", ","));
	                    send(powerRequest);
	                    
	                    System.out.println("Routing power transfer request to " + targetAgent + 
	                                       " after principal agent rejection. Transfer amount: " + transferPower + " W");
	                }
	            } else {
	                if (ORC != null) {
	                    AID sender = ORC.getSender();
	                    
	                    ACLMessage powerRequest = new ACLMessage(ACLMessage.REFUSE);
	                    powerRequest.addReceiver(sender);
	                    powerRequest.setContent(String.format("POWER_DENIED:%s:Insufficient local capacity", sender).replace(".", ","));
	                    send(powerRequest);
	                    
	                    System.out.println("Power transfer request denied to " + sender.getLocalName());
	                } else {
	                    System.err.println("Original message (ORC) is null");
	                }
	            }
	        } catch (Exception e) {
	            System.err.println("Comprehensive error in requestRejecte: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }

	    

}
