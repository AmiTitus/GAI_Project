/**
 * 
 */
package gaiproject;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import gaiproject.Calendar;
import gaiproject.Slot;
import gaiproject.MeetingAgentGui;

/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 * Language:
 * ConversationId:
 * - meeting-ivit: the according message is an invitation PROPOSAL
 * - meeting-invit PROPROSAL_REJECT
 * - meeting-invit: PROPOSAL_ACCEPT
 *
 */
public class MeetingAgent extends Agent{
	private Calendar myCalendar;
	private AID[] contacts;
	private Logger logger = Logger.getLogger(MeetingAgent.class.getName());
	private String agentName;
	private MeetingAgentGui gui;
	private FSMBehaviour fsm;
	protected void setup(){
		// Init calendar randomly to have already planned meeting
		myCalendar = new Calendar(true);
		myCalendar.prettyPrint();
		gui = new MeetingAgentGui(this);
		agentName = getAID().getLocalName();

		// SERVICE AND SET THE CONTACT LIST
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(agentName);
		sd.setType("");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}catch(FIPAException fe){
			logger.log(Level.SEVERE, fe.toString());
		}

		logger.log(Level.INFO, "Hello! " + agentName + " is ready to work.");
		gui.display();
		// Final State Machine
		fsm = new FSMBehaviour(this);
		// States
		fsm.registerFirstState(new WaitInvitation(), "WaitInvitation");
		fsm.registerState(new SendInvitation(), "SendInvitation");
		fsm.registerState(new WaitResponse(), "WaitResponse");
		fsm.registerState(new ReceiveInvitation(), "ReceiveInvitation");
		fsm.registerState(new FindConsensus(), "FindConsensus");
		// Transition
		// Convention
		// Transition 0 : Loop on behaviour itself
		// From WaitInvitation
		fsm.registerTransition("WaitInvitation", "WaitInvitation", 0);
		// fsm.registerTransition("WaitInvitation", "SendInvitation", 1);
		fsm.registerTransition("WaitInvitation", "ReceiveInvitation", 2);
		// From SendInvitation
		fsm.registerTransition("SendInvitation", "WaitResponse", 1);
		fsm.registerTransition("SendInvitation", "WaitInvitation", -1);
		// From WaitResponse
		fsm.registerTransition("WaitResponse", "FindConsensus", 1);
		// From ReceiveInvitation
		fsm.registerTransition("ReceiveInvitation", "WaitInvitation", 1);
	}
	protected void takeDown(){
		logger.log(Level.INFO, "Meeting agent " + agentName + " terminated.");
	}

	public Calendar getCalendar(){
		return myCalendar;
	}

	/**
	 * Invoked from GUI, when a new invitation is created
	 * */
	public void sendInvitation(final int day, final int starttime, final int duration){
		addBehaviour(new SendInvitation(day, starttime, duration));
	}

	private class WaitInvitation extends OneShotBehaviour{
		private long timeOut = 2*60*1000;
		@Override
		public void action(){
			logger.log(Level.INFO, agentName + " is waiting for invitation.");
			block();
		}
		
		public int onEnd(){
			return 2;	
		}
	}

	private class WaitResponse extends OneShotBehaviour{
		@Override
		public void action(){
			logger.log(Level.INFO, agentName + " is waiting for response.");
			block();
		}
		public int onEnd(){
			return 1;
		}
	}

	/**
	 * This behaviour is used to send invitations
	 * We can also send message using jade platform, looks better to do it manually
	 * */
	private class SendInvitation extends OneShotBehaviour{
		/**
		 * Create Object Content for one Invitation
		 * We suppose that when we are sending invitation we want it at 1.0
		 * @param day	Day of Slot
		 * @param starttime	When start meeting
		 * @param duration	How long the meeting is
		 * */
		private Slot[] slots;
		private int day=-1;
		private int starttime=-1;
		private int duration=-1;
		private int retint=-1;

		public SendInvitation(){
			super();
		}

		public SendInvitation(int day, int starttime, int duration){
			super();
			this.day = day;
			this.starttime = starttime;
			this.duration = duration;
		}

		public void onStart(){
			logger.log(Level.INFO, agentName + " creates new invitation: day="+day+" time="+starttime+" duration="+duration);
		}

		@Override
		public void action(){
			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			slots = new Slot[duration];
			for(int i=0;i<duration;i++){
				// Update the slots in agent calendar to PROPOSED state
				myCalendar.getSlot(day, starttime+i).propose();
				// Content objects for invitation
				slots[i] = new Slot(this.starttime+i, this.duration, 1.0);
				slots[i].propose();
			}
			// Update the Calendar in gui
			gui.updateCalendar();
			
			// If the agent doesnt have contacts, we got an error and don't send message
			if(contacts == null || contacts.length == 0){
				myCalendar.manageSlot(day, starttime, duration, Slot.State.FREE);
				retint = -1;
				logger.log(Level.SEVERE, agentName + " No contacts");
			}else{
				for(AID aid: contacts)
					msg.addReceiver(aid);
					msg.setConversationId("meeting-invit");
				try{
					msg.setContentObject(slots);
				}catch(IOException e){
					logger.log(Level.SEVERE, agentName + " Error sending message " + msg);
				}
				myAgent.send(msg);
				logger.log(Level.INFO, agentName + " sent an invitation to his contacts.");
			}
			retint = 1;
		}

		public int onEnd(){
			return retint;
		}
		
	}

	/**
	 * This behaviour is waiting for an invitation, then look if the slots are free, confirm or refuse the proposition
	 **/
	private class ReceiveInvitation extends OneShotBehaviour{
		
		private Slot[] slots;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentName + " received inventation.");	
		}


		/**
		 * Test if all the slots are free
		 * */
		public boolean freeSlots(){
			for(Slot s: slots) if(!s.currentState.equals(Slot.State.FREE)) return false;
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
					logger.log(Level.SEVERE, agentName + " Error receiving message ");
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

		public int onEnd(){
			return 1;
		}
	}

	private class FindConsensus extends OneShotBehaviour{
		
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
	}

}
