package pk2;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.GUI, "true");

        ContainerController cc = rt.createMainContainer(p);

        try {
            System.out.println("\n=== Initializing Agents ===\n");
            
            AgentController agentP = cc.createNewAgent("p", "pk2.Agents", null);
            agentP.start();
            Thread.sleep(1000);

            AgentController agentL1 = cc.createNewAgent("l1", "pk2.Agents", null);
            AgentController agentL2 = cc.createNewAgent("l2", "pk2.Agents", null);
            agentL1.start();
            agentL2.start();
            Thread.sleep(2000);

            AgentController house1 = cc.createNewAgent("h1", "pk2.House", null);
            AgentController house2 = cc.createNewAgent("h2", "pk2.House", null);
            house1.start();
            house2.start();
            Thread.sleep(2000);

            AgentController company1 = cc.createNewAgent("c1", "pk2.Company", null);
            AgentController company2 = cc.createNewAgent("c2", "pk2.Company", null);
            company1.start();
            company2.start();

            Thread.sleep(3000);

            System.out.println("\n=== Starting 24-hour simulation ===\n");
            
            LocalTime currentTime = LocalTime.of(0, 0);
            LocalTime endTime = LocalTime.of(23, 0);
            int hour = 0;

            while (!currentTime.isAfter(endTime) && hour < 24) {
                System.out.println("\n=== Current time: " + 
                    currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " ===");

                try {
                    // تحديث المنازل
                    updateHouseAgent(house1, currentTime);
                    Thread.sleep(100);
                    updateHouseAgent(house2, currentTime);
                    Thread.sleep(100);

                    // تحديث الشركات
                    updateCompanyAgent(company1, currentTime);
                    Thread.sleep(100);
                    updateCompanyAgent(company2, currentTime);
                    Thread.sleep(100);

                } catch (Exception e) {
                    System.err.println("Error in simulation at " + currentTime + ": " + e.getMessage());
                }

                currentTime = currentTime.plusHours(1);
                hour++;
                Thread.sleep(1000);
            }

            System.out.println("\n=== 24-hour simulation completed ===\n");

        } catch (Exception e) {
            System.err.println("Error in simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateHouseAgent(AgentController agent, LocalTime time) {
        try {
            agent.putO2AObject(time, AgentController.ASYNC);
        } catch (StaleProxyException e) {
            System.err.println("Error updating house agent: " + e.getMessage());
        }
    }

    private static void updateCompanyAgent(AgentController agent, LocalTime time) {
        try {
            agent.putO2AObject(time, AgentController.ASYNC);
        } catch (StaleProxyException e) {
            System.err.println("Error updating company agent: " + e.getMessage());
        }
    }
}
