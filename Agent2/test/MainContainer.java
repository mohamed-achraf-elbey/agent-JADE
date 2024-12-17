package pk2;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class MainContainer {
    public static void main(String[] args) {
        try {
            // Get the JADE runtime instance
            Runtime rt = Runtime.instance();
            
            // Create a Profile
            Profile profile = new ProfileImpl(false);
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");
            
            // Create the main container
            System.out.println("Starting main container...");
            AgentContainer mainContainer = rt.createMainContainer(profile);
            
            // Create and start the agents
            System.out.println("Starting agents...");
            
            // Create Provider agent
            AgentController provider = mainContainer.createNewAgent("provider", Provider.class.getName(), null);
            
            // Create Local agents
            AgentController local1 = mainContainer.createNewAgent("l1", Local.class.getName(), null);
            AgentController local2 = mainContainer.createNewAgent("l2", Local.class.getName(), null);
            
            // Create Company agents
            AgentController company1 = mainContainer.createNewAgent("c1", Company.class.getName(), null);
            AgentController company2 = mainContainer.createNewAgent("c2", Company.class.getName(), null);
            
            // Create House agents
            AgentController house1 = mainContainer.createNewAgent("h1", House.class.getName(), null);
            AgentController house2 = mainContainer.createNewAgent("h2", House.class.getName(), null);
            
            // Start the agents in order
            provider.start();  // بدء المزود أولاً
            Thread.sleep(100);
            
            local1.start();    // ثم الوكلاء المحليين
            local2.start();
            Thread.sleep(100);
            
            company1.start();  // ثم الشركات والمنازل
            company2.start();
            house1.start();
            house2.start();
            
            System.out.println("All agents started successfully!");
            
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
