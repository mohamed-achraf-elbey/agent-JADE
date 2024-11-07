package pk;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Agenttt extends Agent {
	public int capacity ;
	public int onlineSite ;
	public int offlineSite ;
	public boolean status = true;
	public int time = 0 ;

	   @Override
	    protected void setup() {
	        System.out.println("Agent " + getLocalName() + " is starting.");
	        System.out.println("gg");
	    }
	   
	   public void listenMsg() {
		    while (status) {
		        ACLMessage msg = receive();
		        if (msg != null) {
		            System.out.println("Agent2 received message: " + msg.getContent());
		        } 
		    }
		}
	   
}
