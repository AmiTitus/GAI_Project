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
import java.util.HashMap;
import java.util.ArrayList;
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
 * ConversationId: invit-00 : invit + id of the invitation
 * - PROPOSE: send meeting slots
 * - REJECT_PROPOSAL: Agent can send a set of slot wich fit better for him
 * - ACCEPT_PROPOSAL:
 *
 */
public class MeetingAgent extends Agent{
	
	private Calendar myCalendar;
	private AID[] contacts;
	private Logger logger = Logger.getLogger(MeetingAgent.class.getName());
	private String agentName;
	private MeetingAgentGui gui;
	private FSMBehaviour fsm;

	private int invitId=0;
	// Message currently processed
	private ACLMessage currentMsg;
	// Map of all messages for each invitation
	private HashMap<String, ArrayList<ACLMessage>> invitTable = new HashMap<String, ArrayList<ACLMessage>>();

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
		fsm.registerFirstState(new Wait(), "Wait");
		fsm.registerState(new SendInvitation(), "SendInvitation");
		fsm.registerState(new ReceiveInvitation(), "ReceiveInvitation");
		fsm.registerState(new ReceiveInvitation(), "ReceiveResponse");
		// Transition
		// Convention
		// Transition 0 : Loop on behaviour itself
		// From Wait
		fsm.registerTransition("Wait", "Wait", 0);
		fsm.registerTransition("Wait", "ReceiveInvitation", 1);
		fsm.registerTransition("Wait", "ReceiveResponse", 2);
		// fsm.registerTransition("WaitInvitation", "SendInvitation", 1);
		// From SendInvitation
		fsm.registerTransition("SendInvitation", "Wait", 1);
		// From ReceiveInvitation
		fsm.registerTransition("ReceiveInvitation", "Wait", 1);
	}

	protected void takeDown(){
		logger.log(Level.INFO, "Meeting agent " + agentName + " terminated.");
	}

	/**
	 * @return Calendar
	 **/
	public Calendar getCalendar(){
		return myCalendar;
	}

	/**
	 * Invoked from GUI, when a new invitation is created
	 * */
	public void sendInvitation(final int day, final int starttime, final int duration){
		addBehaviour(new SendInvitation(day, starttime, duration));
	}

	/**
	 * State where agent is waiting for ALL responses to its invitation
	 * */
	public class Wait extends OneShotBehaviour{
		private int nextState=0;

		@Override
		public void action(){
			logger.log(Level.INFO, agentName + " is waiting for message.");
			// block until receiving a message
			block();
			// Receiving message
			MessageTemplate mt = MessageTemplate.MatchReceiver(contacts);
			currentMsg = myAgent.receive(mt);
			
			if(currentMsg!=null){	
				if(currentMsg.getPerformative()==ACLMessage.PROPOSE){
					nextState=1;	// Message is an invitation
				}else if(currentMsg.getPerformative()==ACLMessage.ACCEPT_PROPOSAL || 
					currentMsg.getPerformative()==ACLMessage.REJECT_PROPOSAL)
				{nextState=2;}
			}
		}
		public int onEnd(){
			return nextState;
		}
	}


	/**
	 * This behaviour is used to send invitations
	 * THis behaviour is invoked using GUI
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
				invitId++;
				for(AID aid: contacts)
					msg.addReceiver(aid);
					msg.setConversationId("invit-"+invitId);
				try{
					msg.setContentObject(slots);
				}catch(IOException e){
					logger.log(Level.SEVERE, agentName + " Error sending message " + msg);
				}
				// Create a new Entry in invitMap for this new Invitation
				invitTable.put(msg.getConversationId(), new ArrayList<ACLMessage>());	
				myAgent.send(msg);
				logger.log(Level.INFO, agentName + " sent invitation "+msg.getConversationId()+" to his contacts.");
			}
			retint = 1;
		}

		public int onEnd(){
			return retint;
		}
		
	}

	/**
	 * This Behaviour computes an invitation
	 **/
	private class ReceiveInvitation extends OneShotBehaviour{
		
		private Slot[] slots;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentName + " received inventation "+currentMsg.getConversationId());	
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
			ACLMessage reply = currentMsg.createReply();
			if(currentMsg!=null){
				try{
					slots = (Slot[])currentMsg.getContentObject();
				}catch(UnreadableException e){
					logger.log(Level.SEVERE, agentName + " Error receiving message ");
				}
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


	/**
	 * This behaviour computes a response	 
	 * */
	private class ReceiveResponse extends OneShotBehaviour{

		private int nextState = 1;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentName + " received response to " + currentMsg.getConversationId());	
		}


		@Override
		public void action(){

			ArrayList<ACLMessage> rejected = new ArrayList<ACLMessage>();
			ArrayList<ACLMessage> accepted = new ArrayList<ACLMessage>();

			// Add the response to the invitTable
			// Check if we got all response for this invitation
			// if one is missing wait for it
			// else looks anwers, if there is negative one we should send new invitation with new time
			ArrayList<ACLMessage> list = (ArrayList<ACLMessage>)invitTable.get(currentMsg.getConversationId());
			list.add(currentMsg);
			if(list.size()==contacts.length){
				for (ACLMessage msg : list) {
					switch(msg.getPerformative()){
						case ACLMessage.ACCEPT_PROPOSAL: accepted.add(msg);break;
						case ACLMessage.REJECT_PROPOSAL: rejected.add(msg);break;
					}
				}
				if(rejected.size()==0){
					// everyone agreed on the slot, we can send confirmation
				}else{
					// some rejected, we have to propose new slot
				}
			}else{
				nextState = 1;
				return;
			}
			
		}

		public int onEnd(){
			return nextState;
		}
	}
}
