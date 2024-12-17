package pk2;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;

public class Local extends Agent {
    private double allocatedCapacity = 0.0; // السعة المخصصة من المزود
    private double availableCapacity = 0.0; // السعة المتاحة للتوزيع
    private HashMap<String, Double> consumerAllocations = new HashMap<>(); // تخصيصات المستهلكين

    @Override
    protected void setup() {
        System.out.println("Local Agent " + getLocalName() + " started");

        // سلوك لمعالجة طلبات السعة من المستهلكين (المنازل والشركات)
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        // طلب من مستهلك
                        handleConsumerRequest(msg);
                    } else {
                        // رد من المزود
                        handleProviderResponse(msg);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void handleConsumerRequest(ACLMessage msg) {
        String consumerId = msg.getSender().getLocalName();
        double requestedCapacity = Double.parseDouble(msg.getContent());
        
        // إذا كان المستهلك لديه تخصيص سابق، قم بإزالته
        if (consumerAllocations.containsKey(consumerId)) {
            availableCapacity += consumerAllocations.get(consumerId);
            consumerAllocations.remove(consumerId);
        }

        if (requestedCapacity <= availableCapacity) {
            // يمكن تلبية الطلب من السعة المتاحة
            allocateCapacityToConsumer(msg, requestedCapacity);
        } else {
            // طلب سعة إضافية من المزود
            requestCapacityFromProvider(requestedCapacity, msg);
        }
    }

    private void handleProviderResponse(ACLMessage msg) {
        if (msg.getPerformative() == ACLMessage.AGREE) {
            // المزود وافق على الطلب
            double newCapacity = Double.parseDouble(msg.getContent());
            allocatedCapacity += newCapacity;
            availableCapacity += newCapacity;
            
            // معالجة الطلب المعلق
            ACLMessage pendingMsg = (ACLMessage) getO2AObject();
            if (pendingMsg != null) {
                double requestedCapacity = Double.parseDouble(pendingMsg.getContent());
                allocateCapacityToConsumer(pendingMsg, requestedCapacity);
            }
        } else {
            // المزود رفض الطلب
            ACLMessage pendingMsg = (ACLMessage) getO2AObject();
            if (pendingMsg != null) {
                ACLMessage reply = pendingMsg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("reject");
                send(reply);
                System.out.println(getLocalName() + ": Rejected request from " + 
                                 pendingMsg.getSender().getLocalName() + 
                                 " (Provider refused additional capacity)");
            }
        }
    }

    private void allocateCapacityToConsumer(ACLMessage msg, double requestedCapacity) {
        String consumerId = msg.getSender().getLocalName();
        availableCapacity -= requestedCapacity;
        consumerAllocations.put(consumerId, requestedCapacity);
        
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        reply.setContent(String.valueOf(requestedCapacity));
        send(reply);
        
        System.out.println(getLocalName() + ": Allocated " + String.format("%.2f", requestedCapacity) + 
                         " W to " + consumerId);
        System.out.println(getLocalName() + ": Available Capacity: " + 
                         String.format("%.2f", availableCapacity) + " W");
    }

    private void requestCapacityFromProvider(double requestedCapacity, ACLMessage consumerMsg) {
        ACLMessage providerRequest = new ACLMessage(ACLMessage.REQUEST);
        providerRequest.addReceiver(getAID("provider"));
        providerRequest.setContent(String.valueOf(requestedCapacity));
        
        // حفظ طلب المستهلك للمعالجة اللاحقة
        putO2AObject(consumerMsg, false);
        
        send(providerRequest);
        System.out.println(getLocalName() + ": Requesting " + String.format("%.2f", requestedCapacity) + 
                         " W from provider for " + consumerMsg.getSender().getLocalName());
    }
}
