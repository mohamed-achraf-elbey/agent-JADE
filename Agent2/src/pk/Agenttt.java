package pk;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Agenttt extends Agent {
	public int capacite ;
	public int onlineSite ;
	public int offlineSite ;
	public boolean etat = true;
	public int time = 0 ;

	   @Override
	    protected void setup() {
	        System.out.println("Agent " + getLocalName() + " is starting.");
	        System.out.println("gg");
	    }
	   
	   public void listenMsg() {
		    while (etat) {
		        ACLMessage msg = receive();
		        if (msg != null) {
		            System.out.println("Agent2 received message: " + msg.getContent());
		        } 
		    }
		}
	   
}
