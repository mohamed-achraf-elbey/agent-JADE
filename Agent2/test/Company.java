package pk2;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Company extends Agent {
    public Double capacity = 0.0;
    public boolean status = false;
    public double consumePower = 0.0;
    public HashMap<String, Double> components = new HashMap<>();
    public HashMap<String, Boolean> componentStates = new HashMap<>();
    public HashMap<String, Double> componentsCompany = new HashMap<>();
    
    @Override
    protected void setup() {
        System.out.println("Company Agent " + getLocalName() + " is starting");
        loadComponents("PowerComponents.txt");
        Random random = new Random();
        int randomIndex = 5 + random.nextInt(3); 
        addRandomComponents(randomIndex);
        setConsumePower(calculateConsumePower());

        sendCapacityRequest();
        receiveResponse();
        
        // Add a behavior to periodically update and display status
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                updateCompanyStatus();
                displayComponents();
                try {
                    Thread.sleep(5000); // Update every 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendCapacityRequest() {
        String AgentId = getLocalName().equals("c1") ? "l1" : "l2";
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getAID(AgentId));
                double requestedCapacity = getConsumePower();
                msg.setContent("" + requestedCapacity);
                System.out.println(getLocalName() + " is requesting capacity: " + String.format("%.2f", requestedCapacity) + " W from " + AgentId);
                send(msg);
            }
        });
    }

    private void receiveResponse() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if ("reject".equals(content)) {
                        status = false;
                        System.out.println(getLocalName() + " received: Insufficient capacity from local.");
                    } else {
                        capacity = Double.parseDouble(content);
                        status = true;
                        System.out.println(getLocalName() + " received allocated capacity: " + String.format("%.2f", capacity) + " W");
                        setConsumePower(calculateConsumePower());
                    }
                } else {
                    block();
                }
            }
        });
    }

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

    public void displayComponents() {
        if (!status) {
            System.out.println(getLocalName() + " is offline. No components are active.");
            return;
        }
        System.out.println("\n" + getLocalName() + " Active Components:");
        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            String component = entry.getKey();
            if (componentStates.getOrDefault(component, false)) {
                Double power = entry.getValue();
                System.out.println("  " + component + ": " + power + " W");
            }
        }
        System.out.println("  Total Power Consumption: " + String.format("%.2f", consumePower) + " W\n");
    }

    public void addRandomComponents(int numbeCom) {
        Random random = new Random();
        Object[] availableComponents = components.keySet().toArray();
        for (int i = 0; i < Math.min(numbeCom, availableComponents.length); i++) {
            int randomIndex = random.nextInt(availableComponents.length - i);
            String randomKey = (String) availableComponents[randomIndex];
            componentsCompany.put(randomKey, components.get(randomKey));
            componentStates.put(randomKey, true);
            
            // Swap the selected component to the end to avoid selecting it again
            Object temp = availableComponents[availableComponents.length - 1 - i];
            availableComponents[availableComponents.length - 1 - i] = availableComponents[randomIndex];
            availableComponents[randomIndex] = temp;
        }
    }

    public Double calculateConsumePower() {
        consumePower = 0;
        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            if (componentStates.getOrDefault(entry.getKey(), false)) {
                consumePower += entry.getValue();
            }
        }
        return consumePower;
    }

    public void updateCompanyStatus() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(16, 0))) {
            status = true;
            updateComponentStatesByTime();
        } else {
            status = false;
            for (String component : componentsCompany.keySet()) {
                componentStates.put(component, false);
            }
        }
    }

    public void updateComponentStatesByTime() {
        if (!status) return;

        LocalTime currentTime = LocalTime.now();
        Random random = new Random();
        
        if (currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(12, 0))) {
            for (String component : componentsCompany.keySet()) {
                componentStates.put(component, random.nextBoolean());
            }
        } else {
            for (String component : componentsCompany.keySet()) {
                componentStates.put(component, true);
            }
        }
        setConsumePower(calculateConsumePower());
    }

    public double getConsumePower() {
        return consumePower;
    }

    private void setConsumePower(double consumePower) {
        this.consumePower = consumePower;
    }
}
