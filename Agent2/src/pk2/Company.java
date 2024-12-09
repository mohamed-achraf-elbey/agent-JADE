package pk2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Company extends Agent {
    private double capacity = 0.0;
    private double consumePower = 0.0;
    private boolean status = false;
    private String activity = "CLOSED";
    public HashMap<String, Double> components = new HashMap<>();
    public HashMap<String, Boolean> componentStates = new HashMap<>();
    public HashMap<String, Double> componentsCompany = new HashMap<>();
    private boolean reponse = false ;
    private double activityProbability;
    private LocalTime currentTime ;

    protected void setup() {
    	 setEnabledO2ACommunication(true, 1); 
    	 
         loadComponents("PowerComponents.txt");
         Random random = new Random();
         int randomIndex = 10 + random.nextInt(3); 
         addRandomComponents(randomIndex);

         System.out.println("Agent " + getLocalName() + " is starting");
         initialCapacityRequest();
         receiveResponse();

         addBehaviour(new CyclicBehaviour() {
             public void action() {
                 Object obj = getO2AObject();
                 if (obj != null && obj instanceof LocalTime) {
                     
                     updateAgentStatus((LocalTime) obj);
                     
                 }
                 block();
             }
         });
    }

  /*  private void requestInitialCapacity() {
        String agentId = getLocalName().equals("c1") ? "l1" : "l2";
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(agentId, AID.ISLOCALNAME));
        msg.setContent(String.format("POWER_REQUEST:%.2f", consumePower));
        msg.setConversationId("initial-request-" + System.currentTimeMillis());
        send(msg);
        System.out.println(getLocalName() + " is requesting initial capacity: " + 
                         String.format("%.2f", consumePower) + " W from " + agentId);
    }*/

    private void receiveResponse() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                ));
                
                if (msg != null) {
                    try {
                        String content = msg.getContent();
                        if (content.startsWith("POWER_ALLOCATED:")) {
                            double receivedCapacity = Double.parseDouble(content.split(":")[1].replace(",", "."));
                            capacity += receivedCapacity;
                            
                            status = true;
                            //*****************
                            System.out.println(getLocalName() + " received " + String.format("%.2f", receivedCapacity) + " W and is ONLINE And new cap = "+capacity);
                            
                            if (reponse) {
                                displayPowerStatus(currentTime);
                                reponse = false ;
                            }

                           

                        } else if (content.startsWith("POWER_DENIED:")) {
                            status = false;
                            System.out.println(getLocalName() + " is now OFF: " + content.split(":")[1]);
                        } else {
                            System.err.println("Unknown message format received by " + getLocalName() + ": " + content);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing message in " + getLocalName() + ": " + e.getMessage());
                        System.err.println("Message content: " + msg.getContent());
                    }
                } else {
                    block();
                }
            }
        });
    }
//*******************************************************************************************
    public void updateAgentStatus(LocalTime currentTime) {
    	this.currentTime = currentTime ;
        int hour = currentTime.getHour();
        double requiredPower = 0.0;

        activity = "CLOSED";
        activityProbability = 0.0;
        status = false;

        if (hour >= 8 && hour <= 12) {
            activity = "WORKING HOURS";
            activityProbability = 1.0;
        } else if (hour >= 13 && hour <= 16) {
            activity = "AFTERNOON WORK";
            activityProbability = 0.7;
        }

        calculateConsumePowerDynamic();
        requiredPower = 0;

        if (hour < 8 || hour > 16) {
            System.out.println("Company " + getLocalName() + " at " +
                currentTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                (status ? "ONLINE" : "OFFLINE") + ":");
            System.out.println("\tActivity Status: " + activity);
            return;
        }

        if (consumePower <= capacity) {
            status = true;
            System.out.println(getLocalName() + " consumed power: " + String.format("%.2f", consumePower) + " W");
            displayPowerStatus(currentTime);
        } else {
            status = false;
            reponse = true;
            requiredPower = consumePower - capacity;

            System.err.println(getLocalName() + " does not have enough capacity. Remaining capacity: " +
                String.format("%.2f", capacity) + " W.");
            requestAdditionalPower(requiredPower);

            /*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt(); 
				e.printStackTrace();
			}
            if (reponse) {
                status = true;
                System.out.println(getLocalName() + " consumed power: " + String.format("%.2f", consumePower) + " W.");
                displayPowerStatus(currentTime);
            } else {
                System.out.println(getLocalName() + " could not consume power due to insufficient capacity.");
            }*/

            
        }
    }



   
  /*  private void displayPowerStatus(LocalTime currentTime) {
        System.out.println(String.format("Company %s at %s - %s:" +
                         "\n\tTotal Capacity: %.2f W" +
                         "\n\tCurrent Consumption: %.2f W" +
                         "\n\tActivity Status: %s",
                         getLocalName(),
                         currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                         status ? "ONLINE" : "OFFLINE",
                         capacity,
                         consumePower,
                         activity));
    }*/
    public void loadComponents(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String component = parts[0].trim();
                    Double power = Double.parseDouble(parts[1].trim());
                    components.put(component, power);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addRandomComponents(int numComponents) {
        Random random = new Random();
        for (int i = 0; i < numComponents; i++) {
            int randomIndex = random.nextInt(components.size());
            String randomKey = (String) components.keySet().toArray()[randomIndex];
            componentsCompany.put(randomKey, components.get(randomKey));
            componentStates.put(randomKey, true);
            components.remove(randomKey);
        }
    }
    
    private void initialCapacityRequest() {
        String agentId = getLocalName().equals("c1") ? "l1" : "l2";
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(agentId, AID.ISLOCALNAME));
        msg.setContent(String.format("POWER_REQUEST:%.2f", calculateInitialConsumePower()));
        msg.setConversationId("initial-request-" + System.currentTimeMillis());
        send(msg);
        
    }
    
    public Double calculateInitialConsumePower() {
        consumePower = 0;
        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            consumePower += entry.getValue();
        }
        return consumePower * 0.5;
    }
    
    public void calculateConsumePowerDynamic() {
        consumePower = 0;
        Random random = new Random();

        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            boolean isActive = random.nextDouble() < activityProbability;
            componentStates.put(entry.getKey(), isActive);

            if (isActive) {
                consumePower += entry.getValue();
            }
        }
    }
    
    private void displayPowerStatus(LocalTime currentTime ) {

        System.out.println("Company " + getLocalName() + " at " + currentTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " + (status ? "ONLINE" : "OFFLINE") + ":" +
                "\n\t Capacity: " + String.format("%.2f", capacity) + " W" +
                "\n\tCurrent Consumption: " + String.format("%.2f", consumePower) + " W" +
                "\n\tActivity Level: " + activity + " (" + (activityProbability * 100) + "%)");
    }
    
    private void requestAdditionalPower(double requiredPower) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(getLocalName().equals("c1") ? "l1" : "l2", AID.ISLOCALNAME));
        System.out.println(getLocalName() + " requests capacity " + requiredPower + " to " + (getLocalName().equals("c1") ? "l1" : "l2"));
        msg.setContent("ADDITIONAL_POWER:" + String.format("%.2f", requiredPower));
        send(msg);
    }


    public double getCapacity() {
        return capacity;
    }

    public double getConsumePower() {
        return consumePower;
    }

    public boolean isActive() {
        return status;
    }
    
}