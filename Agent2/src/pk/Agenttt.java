/*package pk;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agenttt extends Agent {
    private Double capacity;
    private boolean status;
    private Double totalConsumedPower = 0.0;

    private final int onlineStart = 8;
    private final int onlineEnd = 16;
    private int time = 0;

    private List<HouseAndCompany> houseAndCompany = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " is starting ");
        initializeAgent();
        simulate();
    }

    private void initializeAgent() {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                if (getLocalName().equals("p")) {
                    status = true;
                    capacity = setRandomCapacity(80000, 90000);
                    System.out.println("Principal agent " + getLocalName() + " initial capacity: " + String.format("%.2f", capacity) + " W");
                    sendCapacityToLocalAgents();
                } else {
                    status = true;
                    initializeHouses();
                    displayAllHousesComponents();
                    receiveCapacityUpdates();
                }
            }
        });
    }

    private void updateStatus() {
        if (time >= onlineStart && time <= onlineEnd) {
            status = capacity > 0;  // Agents are online only if they have capacity
        } else {
            status = false;  // Agents are offline outside the online time range
        }
    }

    private void sendCapacityToLocalAgents() {
        if (getLocalName().equals("p")) {
            double capacityL1 = setRandomCapacity(20000, 30000);
            double capacityL2 = setRandomCapacity(20000, 30000);

            if (capacity < capacityL1 + capacityL2) {
                status = false; // Principal agent goes offline if not enough capacity
                System.out.println("Principal agent " + getLocalName() + " is offline due to insufficient capacity.");
            } else {
                sendCapacityRequest("l1", capacityL1);
                sendCapacityRequest("l2", capacityL2);
                System.out.println("P agent sent capacity requests to local agents.");
            }
        }
    }

    private void sendCapacityRequest(String localAgentName, double requestedCapacity) {
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

    private void requestAdditionalCapacity() {
        if (!getLocalName().equals("p")) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            double addCapacity = setRandomCapacity(5000, 10000);
            msg.addReceiver(getAID("p"));
            msg.setContent(String.valueOf(addCapacity));
            send(msg);
            System.out.println(getLocalName() + " requested capacity from Principal.");
        }
    }

    private void receiveCapacityUpdates() {
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

    private double setRandomCapacity(int minCap, int maxCap) {
        Random random = new Random();
        return minCap + (maxCap - minCap) * random.nextDouble();
    }

    public void initializeHouses() {
        int numHouses = 4 + new Random().nextInt(3);
        for (int i = 0; i < numHouses; i++) {
        	houseAndCompany.add(HouseAndCompany.initializeHouseAndCompany());
        }
        System.out.println("Agent " + getLocalName() + " initialized " + numHouses + " houses");
    }

    public void displayAllHousesComponents() {
        if (houseAndCompany.isEmpty()) {
            System.out.println("No houses to display for Agent " + getLocalName());
            return;
        }

        System.out.println("Displaying components for " + getLocalName() + ":");
        for (HouseAndCompany house : houseAndCompany) {
            totalConsumedPower += house.getConsumePower();
        }
        System.err.println("Total consumption for " + getLocalName() + ": " + String.format("%.2f", totalConsumedPower) + " W in 1H");
    }

    private void simulate() {
        addBehaviour(new TickerBehaviour(this, 1000) { // Simulate each hour
            protected void onTick() {
                System.out.println("\n--- Simulation time: " + time + "h ---");

                updateStatus();  // Update the agent's online status based on time and capacity

                if (getLocalName().equals("p")) {
                    receiveCapacityRequests();
                    System.out.println("Principal agent status: " + (status ? "online" : "offline"));
                } else {
                    double powerNeeded = calculateTotalPowerConsumption();
                    if (capacity < powerNeeded) {
                        requestAdditionalCapacity();
                    } else {
                        capacity -= powerNeeded;
                    }
                    System.out.println(getLocalName() + " status: " + (status ? "online" : "offline"));
                }

                time++;
                if (time >= 24) {
                    System.out.println("\nSimulation completed after 24 hours.");
                    doDelete();
                }
            }
        });
    }

    private void receiveCapacityRequests() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    if ("reject".equals(content)) {
                        System.out.println(getLocalName() + " received: Insufficient capacity from principal.");
                    } else {
                        capacity += Double.parseDouble(content);
                        System.out.println(getLocalName() + " received additional capacity: " + String.format("%.2f", capacity) + " W.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    public double calculateTotalPowerConsumption() {
        double totalPower = 0.0;
        for (HouseAndCompany house : houseAndCompany) {
            totalPower += house.getConsumePower();
        }
        return totalPower;
    }
}*/