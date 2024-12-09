package pk2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Agents extends Agent {
    private double totalCapacity;
    private double availableCapacity;
    private boolean isPrincipal;
    private Map<String, Double> allocatedPower;

    protected void setup() {
        allocatedPower = new HashMap<>();
        Random random = new Random();

        if (getLocalName().equals("p")) {
            isPrincipal = true;
            totalCapacity = 10000 + random.nextDouble() * 20000;
            availableCapacity = totalCapacity;
            System.out.println("Principal agent " + getLocalName() + " initial capacity: " +
                    String.format("%.2f", totalCapacity) + " W");
        } else {
            isPrincipal = false;
            totalCapacity = 1000 + random.nextDouble() * 3000;
            availableCapacity = 0;
            System.out.println("Agent " + getLocalName() + " is starting");
            requestPowerFromPrincipal();
        }

        addBehaviour(new CyclicBehaviour() {
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
                    try {
                        String content = msg.getContent();
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            if (content.startsWith("POWER_ALLOCATED:")) {
                                double receivedPower = Double.parseDouble(content.split(":")[1].replace(",", "."));
                                if (!isPrincipal) {
                                    availableCapacity = receivedPower;
                                    totalCapacity = receivedPower;
                                }
                            }
                        } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                            if (content.startsWith("POWER_REQUEST:") || content.startsWith("ADDITIONAL_POWER:")) {
                                double requestedPower = Double.parseDouble(content.split(":")[1].replace(",", "."));
                                handlePowerRequest(msg.getSender(), requestedPower);
                            }
                        } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                            System.out.println(getLocalName() + " received refusal for power request.");
                            if (!isPrincipal) {
                                requestPowerFromOtherAgent();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error in " + getLocalName() + ": " + e.getMessage());
                        System.err.println("Message content: " + msg.getContent());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void requestPowerFromPrincipal() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("p", AID.ISLOCALNAME));
        msg.setContent("POWER_REQUEST:" + String.format("%.2f", totalCapacity).replace(",", "."));
        send(msg);
        System.out.println(getLocalName() + " is requesting capacity: " +
                String.format("%.2f", totalCapacity) + " W from p");
    }

    private void requestPowerFromOtherAgent() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        String targetAgent = getLocalName().equals("l1") ? "l2" : "l1";
        msg.addReceiver(new AID(targetAgent, AID.ISLOCALNAME));
        msg.setContent("POWER_REQUEST:" + String.format("%.2f", totalCapacity).replace(",", "."));
        send(msg);
        System.out.println(getLocalName() + " is requesting capacity from "+targetAgent);
    }

    private void handlePowerRequest(AID sender, double requestedPower) {
        String agentName = sender.getLocalName();

        // إذا كان هناك طاقة مخصصة سابقاً، قم بإعادتها إلى الطاقة المتاحة
        if (allocatedPower.containsKey(agentName)) {
            availableCapacity += allocatedPower.get(agentName);
            allocatedPower.remove(agentName);
        }

        if (availableCapacity >= requestedPower) {
            availableCapacity -= requestedPower;
            allocatedPower.put(agentName, requestedPower);

            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(sender);
            reply.setContent("POWER_ALLOCATED:" + String.format("%.2f", requestedPower).replace(",", "."));
            send(reply);

            if (isPrincipal) {
                System.out.println("Principal allocated " + String.format("%.2f", requestedPower) +
                        " W to " + agentName + ". Remaining capacity: " +
                        String.format("%.2f", availableCapacity) + " W.");
            } else {
                System.out.println(getLocalName() + " allocated " + String.format("%.2f", requestedPower) +
                        " W to " + agentName + ". Remaining capacity: " +
                        String.format("%.2f", availableCapacity) + " W.");
            }
        } else {
            ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
            reply.addReceiver(sender);
            reply.setContent("POWER_DENIED:Insufficient capacity");
            send(reply);

            System.out.println(getLocalName() + " refused to allocate " + String.format("%.2f", requestedPower) +
                    " W to " + agentName + " (insufficient capacity)");
        }
    }
}
