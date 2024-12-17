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
            
            AgentController agentP = cc.createNewAgent("p", "pk2.Agents2", null);
            agentP.start();
            Thread.sleep(1000);

            AgentController agentL1 = cc.createNewAgent("l1", "pk2.Agents2", null);
            AgentController agentL2 = cc.createNewAgent("l2", "pk2.Agents2", null);
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
                System.out.println("\n=============================================== Current time: " + 
                    currentTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " ===");

                try {
                    updateHouseAgent(house1, currentTime);
                    Thread.sleep(100);
                    updateHouseAgent(house2, currentTime);
                    Thread.sleep(100);

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
        generateSimulationReport();
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

    private static void generateSimulationReport() {
        System.out.println("\n=== Simulation Report ===");
        System.out.println("Total Power Distributed:");
        System.out.println("Total power distributed to Houses: " + House.getTotalConsumedPower() + " W");
        System.out.println("Total power distributed to Companies: " + Company.getTotalConsumedPower() + " W");
        System.out.println("Total power distributed by Local Agents (l1, l2): " + (Agents.getTotalPowerDistributed()) + " W");
        System.out.println("Total power distributed by Principal Agent (p): " + (Agents.getTotalPowerDistributed()) + " W");
        
        System.out.println("Agent Activity Summary:");
        System.out.println("Total House Online Hours: " + House.getOnlineHours() + " hours");
        System.out.println("Total Company Online Hours: " + Company.getOnlineHours() + " hours");
        System.out.println("Total Requests Handled by Local Agents (l1, l2): " + Agents.getTotalRequestsHandled());
        System.out.println("Total Requests Handled by Principal Agent (p): " + Agents.getTotalRequestsHandled());
        
        double totalPower = House.getTotalConsumedPower() + Company.getTotalConsumedPower() + Agents.getTotalPowerDistributed();
        double efficiency = (totalPower / (24 * 10000)) * 100; // Assuming 10000 W capacity per hour
        System.out.println("Overall System Efficiency: " + String.format("%.2f", efficiency) + "%");
    }
}
