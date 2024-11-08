package pk;

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
        AgentController agentP = cc.createNewAgent("p", "pk.Agenttt", null);
        AgentController agentL1 = cc.createNewAgent("l1", "pk.Agenttt", null);
        AgentController agentL2 = cc.createNewAgent("l2", "pk.Agenttt", null);

        
        agentP.start();
        agentL1.start();
        agentL2.start();
    
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    
}
}
