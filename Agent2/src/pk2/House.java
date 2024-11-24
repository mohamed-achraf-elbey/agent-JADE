package pk2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class House {
	
	public boolean status = true ;
	public double consumePower = 0.0 ;
	public HashMap<String, Double> components = new HashMap<>();
    public HashMap<String, Double> componentsHouse = new HashMap<>();
	
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
	        System.out.println("displaying componentsHouse :");
	        
	        for (String component : componentsHouse.keySet()) {
	            Double power = componentsHouse.get(component);
	            System.out.println(component + ": " + power + " W");
	        }
	    }
	 
	 public  void addRandomComponents(int numbeCom) {
	        Random random = new Random();
	        for (int i = 0; i < numbeCom; i++) {
	            int randomIndex = random.nextInt(components.size());
	            String randomKey = (String) components.keySet().toArray()[randomIndex];
	            //System.out.println(randomKey);
	            componentsHouse.put(randomKey, components.get(randomKey));
	            components.remove(randomKey);
	        }
	    }
	 
	 public void  clculConsumePower(){
	    	consumePower = 0 ;
	        consumePower = componentsHouse.values().stream().mapToDouble(Double::doubleValue).sum(); 
	    }
	 
	 public double getConsumePower() {
			return consumePower;
		}

		private String getStatus() {
			if(status)
			return "online";
			else return "offline" ;
		}
		private void setStatus(boolean status) {
			this.status = status;
		}

		private void setConsumePower(double consumePower) {
			this.consumePower = consumePower;
		}
		
		public static House initializeHouse() {
		    House h = new House(); 
		    h.loadComponents("PowerComponents.txt");
		    Random random = new Random();
		    int randomIndex;
		   randomIndex = 5 + random.nextInt(3); // Random number between 5 and 7
		    h.addRandomComponents(randomIndex);
		    h.clculConsumePower();
		    return h;
		}
}
