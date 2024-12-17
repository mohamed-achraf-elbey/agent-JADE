package pk2;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Provider extends Agent {
    private double totalCapacity = 20000.0; // إجمالي السعة المتاحة
    private double availableCapacity; // السعة المتبقية
    private double allocatedCapacity = 0.0; // السعة المخصصة

    @Override
    protected void setup() {
        availableCapacity = totalCapacity;
        System.out.println("Provider Agent " + getLocalName() + " started with capacity: " + totalCapacity + " W");
        
        // سلوك لمعالجة طلبات السعة من الوكلاء المحليين
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent();
                    double requestedCapacity = Double.parseDouble(content);
                    ACLMessage reply = msg.createReply();

                    if (requestedCapacity <= availableCapacity) {
                        // قبول الطلب وتخصيص السعة
                        availableCapacity -= requestedCapacity;
                        allocatedCapacity += requestedCapacity;
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent(String.valueOf(requestedCapacity));
                        System.out.println("\nProvider: Allocated " + String.format("%.2f", requestedCapacity) + 
                                         " W to " + msg.getSender().getLocalName());
                        System.out.println("Provider: Available Capacity: " + String.format("%.2f", availableCapacity) + 
                                         " W, Allocated: " + String.format("%.2f", allocatedCapacity) + " W");
                    } else {
                        // رفض الطلب لعدم كفاية السعة
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("insufficient_capacity");
                        System.out.println("\nProvider: Rejected request from " + msg.getSender().getLocalName() + 
                                         " for " + String.format("%.2f", requestedCapacity) + " W (Insufficient capacity)");
                        System.out.println("Provider: Available Capacity: " + String.format("%.2f", availableCapacity) + " W");
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });

        // سلوك دوري لتحديث السعة المتاحة
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                try {
                    Thread.sleep(10000); // تحديث كل 10 ثواني
                    // إعادة تعيين جزء من السعة المخصصة
                    double releasedCapacity = allocatedCapacity * 0.1; // تحرير 10% من السعة المخصصة
                    availableCapacity += releasedCapacity;
                    allocatedCapacity -= releasedCapacity;
                    System.out.println("\nProvider: Released " + String.format("%.2f", releasedCapacity) + " W");
                    System.out.println("Provider: New Available Capacity: " + String.format("%.2f", availableCapacity) + " W");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
