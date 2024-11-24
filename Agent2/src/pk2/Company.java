package pk2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Company {
    public boolean status = true; // Company status (online/offline)
    public double consumePower = 0.0;
    public HashMap<String, Double> components = new HashMap<>();
    public HashMap<String, Boolean> componentStates = new HashMap<>();
    public HashMap<String, Double> componentsCompany = new HashMap<>();

    public void loadComponents(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String component = parts[0].trim();
                    Double power = Double.parseDouble(parts[1].trim());
                    components.put(component, power);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayComponents() {
        if (!status) {
            System.out.println("Company is offline. No components are active.");
            return;
        }
        System.out.println("Displaying componentsCompany (ON only):");
        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            String component = entry.getKey();
            if (componentStates.getOrDefault(component, false)) { // Only display ON components
                Double power = entry.getValue();
                System.out.println(component + ": " + power + " W");
            }
        }
    }

    public void addRandomComponents(int numbeCom) {
        Random random = new Random();
        for (int i = 0; i < numbeCom; i++) {
            int randomIndex = random.nextInt(components.size());
            String randomKey = (String) components.keySet().toArray()[randomIndex];
            componentsCompany.put(randomKey, components.get(randomKey));
            componentStates.put(randomKey, true); // Default state ON
            components.remove(randomKey);
        }
    }

    public void calculateConsumePower() {
        consumePower = 0;
        if (!status) {
            consumePower = 0; // No consumption if the company is offline
            return;
        }
        for (Map.Entry<String, Double> entry : componentsCompany.entrySet()) {
            if (componentStates.getOrDefault(entry.getKey(), false)) { // Only calculate ON components
                consumePower += entry.getValue();
            }
        }
    }

    public void updateCompanyStatus() {
        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(16, 0))) {
            status = true; // Company is online
            updateComponentStatesByTime();
        } else {
            status = false; // Company is offline
            for (String component : componentsCompany.keySet()) {
                componentStates.put(component, false); // Turn all components OFF
            }
        }
    }

    public void updateComponentStatesByTime() {
        if (!status) return; // Do nothing if company is offline

        LocalTime currentTime = LocalTime.now();
        if (currentTime.isAfter(LocalTime.of(8, 0)) && currentTime.isBefore(LocalTime.of(12, 0))) {
            Random random = new Random();
            for (String component : componentsCompany.keySet()) {
                if (random.nextBoolean()) { // Randomly turn some components OFF
                    componentStates.put(component, false);
                }
            }
        } else {
            for (String component : componentsCompany.keySet()) {
                componentStates.put(component, true); // Turn all components ON outside 8-12
            }
        }
    }

    public double getConsumePower() {
        return consumePower;
    }

    private String getStatus() {
        return status ? "online" : "offline";
    }

    private void setStatus(boolean status) {
        this.status = status;
    }

    private void setConsumePower(double consumePower) {
        this.consumePower = consumePower;
    }

    public static Company initializeHouse() {
        Company C = new Company();
        C.loadComponents("PowerComponents.txt");
        Random random = new Random();
        int randomIndex = 5 + random.nextInt(3); // Random number between 5 and 7
        C.addRandomComponents(randomIndex);
        C.updateCompanyStatus(); // Adjust company status and components' state based on time
        C.calculateConsumePower();
        return C;
    }
}
