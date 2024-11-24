package pk2;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
public static void main(String []args){
	Runtime rt = Runtime.instance();
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.MAIN_PORT, "1300");
    p.setParameter(Profile.GUI, "true"); 
    
    ContainerController cc = rt.createMainContainer(p);

    try {
        AgentController agentP = cc.createNewAgent("p", "pk2.Agents", null);
        AgentController agentL1 = cc.createNewAgent("l1", "pk2.Agents", null);
        AgentController agentL2 = cc.createNewAgent("l2", "pk2.Agents", null);
        AgentController house1 = cc.createNewAgent("h1", "pk2.Agents", null);
        AgentController house2 = cc.createNewAgent("h2", "pk2.Agents", null);
        AgentController company1 = cc.createNewAgent("c1", "pk2.Agents", null);
        AgentController company2 = cc.createNewAgent("c2", "pk2.Agents", null);

        
        agentP.start();
        agentL1.start();
        agentL2.start();
        house1.start();
        house2.start();
        company1.start();
        company2.start();
    
    
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    
}
}