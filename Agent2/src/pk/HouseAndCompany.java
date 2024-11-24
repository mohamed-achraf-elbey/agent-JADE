package pk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;


public class HouseAndCompany {
	public boolean status = true ;
	public double consumePower = 0 ;
	public boolean type = false ;
    public HashMap<String, Double> components = new HashMap<>();
    public HashMap<String, Double> componentsHouseAndCompany = new HashMap<>();
    


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
        
        for (String component : componentsHouseAndCompany.keySet()) {
            Double power = componentsHouseAndCompany.get(component);
            System.out.println(component + ": " + power + " W");
        }
    }
    
    public  void addRandomComponents(int numbeCom) {
        Random random = new Random();
        for (int i = 0; i < numbeCom; i++) {
            int randomIndex = random.nextInt(components.size());
            String randomKey = (String) components.keySet().toArray()[randomIndex];
            //System.out.println(randomKey);
            componentsHouseAndCompany.put(randomKey, components.get(randomKey));
            components.remove(randomKey);
        }
    }
    
    
    
    public void  clculConsumePower(){
    	consumePower = 0 ;
        consumePower = componentsHouseAndCompany.values().stream().mapToDouble(Double::doubleValue).sum(); 
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
	
	public static HouseAndCompany initializeHouseAndCompany() {
	    HouseAndCompany h = new HouseAndCompany(); 
	    h.loadComponents("PowerComponents.txt");
	    
	    Random random = new Random();
	    int randomIndex;
	    
	    if (random.nextDouble() > 0.4) { // Add house
	        randomIndex = 5 + random.nextInt(3); // Random number between 5 and 7
	    } else { // Add company
	        randomIndex = 15 + random.nextInt(3); // Random number between 15 and 17
	    }
	    
	    h.addRandomComponents(randomIndex);
	    h.clculConsumePower();
	    
	    return h;
	}

	

	/*public static void main(String[] args) {
        House house = new House();
        house.addHouse();
        house.displayComponents(componentsHouse);
        System.out.println("status : "+house.getStatus());
        System.out.println("consume power = "+house.getConsumePower());
    }*/
   
    
    
}
