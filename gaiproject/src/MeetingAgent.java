/**
 * 
 */
package gaiproject;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import gaiproject.Calendar;
import gaiproject.Slot;

/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
public class MeetingAgent extends Agent{
	private Calendar myCalendar;
	private AID[] contacts;
	private Logger logger = Logger.getLogger(MeetingAgent.class.getName());
	private String agentname = getAID().getLocalName();
	protected void setup(){
		// Init calendar randomly to have already planned meeting
		myCalendar = new Calendar(true);
		myCalendar.prettyPrint();
		logger.log(Level.INFO, "Hello! " + agentname + " is ready to work.");
	}
	protected void takeDown(){
		logger.log(Level.INFO, "Meeting agent " + agentname + " terminated.");
	}

	private class WaitInvitation extends OneShotBehaviour{
		@Override
		public void action(){
			logger.log(Level.INFO, agentname + " is waiting for invitation.");
			block();
		}
	}

	private class WaitResponse extends OneShotBehaviour{
		@Override
		public void action(){
			logger.log(Level.INFO, agentname + " is waiting for response.");
			block();
		}
	}

	/**
	 * This behaviour is used to send invitations
	 * */
	private class DendInvitation extends OneShotBehaviour{
		/**
		 * Create Object Content for one Invitation
		 * We suppose that when we are sending invitation we want it at 1.0
		 * @param day	Day of Slot
		 * @param starttime	When start meeting
		 * @param duration	How long the meeting is
		 * */
		private Slot[] slots;

		private void createContentObject(){	
			int day=1;
			int starttime=8;
			int duration=1;
			for(int i=0;i<duration;i++){
				slots[i] = new Slot(starttime, duration, 1.0);
				starttime+=1;
			}
		}

		@Override
		public void action(){
			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			for(AID aid: contacts)
				msg.addReceiver(aid);
				msg.setConversationId("meeting-invit");
			try{
				msg.setContentObject(slots);
			}catch(IOException e){
				logger.log(Level.SEVERE, agentname + " Error sending message " + msg);
			}
			myAgent.send(msg);
			logger.log(Level.INFO, agentname + " sent an invitation to his contacts.");
		}
		
	}

	/**
	 * This behaviour is waiting for an invitation, then look if the slots are free, confirm or refuse the proposition
	 **/
	private class ReceiveInvitation extends OneShotBehaviour{
		
		private Slot[] slots;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentname + " received inventation.");	
		}

		public boolean freeSlots(){
			for(Slot s: slots) if(!s.lock) return false;
			return true;
		}

		@Override
		public void action(){
			// Can only respond to a message from contacts and a message is a proposal
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchReceiver(contacts), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
			ACLMessage msg = myAgent.receive(mt);
			ACLMessage reply = msg.createReply();
			if(msg!=null){
				try{
					slots = (Slot[])msg.getContentObject();
				}catch(UnreadableException e){
					logger.log(Level.SEVERE, agentname + " Error receiving message ");
				}
				reply.setConversationId("meeting-response");
				if(freeSlots()){
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}else{
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				}
				myAgent.send(reply);
			}
		}
	}

	private class FindConsensus extends Behaviour{
		
		private AID[] contactrefuse;
		private AID[] contactaccept;
		private int repliesCnt=0;
		private long timeout = 1*60*1000;
		private boolean consensus=false; //when this value is true, negociations are done and the meeting was locked, the behaviour end

		public void action(){
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchReceiver(contacts),
					     	 	         MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
						    	         MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)));
			ACLMessage msg = receive(mt);
			// Collect all responses of the invitation
			if(msg!=null){
				repliesCnt++;
		
			}
		}

		public boolean done(){
			return consensus;
		}
	}

}
