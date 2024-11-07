package pk;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;

public class Agenttt extends Agent {
    private Double capacity;
    private boolean status;
    private int simulatedHour;
    private final int onlineStart = 8;
    private final int onlineEnd = 16;
    private List<Agenttt> localAgents = new ArrayList<>();
    
    // Only for local agents
    private List<House> houses = new ArrayList<>();

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " is starting.");
        updateStatus(); 
    }
    
    public void setSimulatedHour(int hour) {
        this.simulatedHour = hour;
        updateStatus();
    }

    private void updateStatus() {
        status = (simulatedHour >= onlineStart && simulatedHour < onlineEnd);
        System.out.println("Agent " + getLocalName() + " is " + (status ? "online" : "offline"));
    }
    
    private void receiveMsg() {
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
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

    private void sendCapacity() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setContent("Requesting capacity");
        msg.addReceiver(getAID("p"));
        send(msg);
        System.out.println(getLocalName() + " sent a capacity request.");
    }
}
