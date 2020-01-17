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
import java.util.Random;
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
 * - CONFIRM: confirm the meeting slot(s) and lock them
 * - ACCEPT / REJECT
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

		// Init services
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
		
		fsm = new FSMBehaviour(this);
		// States
		fsm.registerFirstState(new Wait(), "Wait");
		fsm.registerState(new SendInvitation(), "SendInvitation");
		fsm.registerState(new ManageInvitation(), "ManageInvitation");
		fsm.registerState(new ManageResponse(), "ManageResponse");
		// Transition
		// Convention
		// Transition 0 : Loop on behaviour itself
		// From Wait
		fsm.registerTransition("Wait", "Wait", 0);
		fsm.registerTransition("Wait", "ManageInvitation", 1);
		fsm.registerTransition("Wait", "ManageResponse", 2);
		// fsm.registerTransition("WaitInvitation", "SendInvitation", 1);
		// From SendInvitation
		fsm.registerTransition("SendInvitation", "Wait", 1);
		// From ReceiveInvitation
		fsm.registerTransition("ManageInvitation", "Wait", 1);
		// From ManageResponse
		fsm.registerTransition("ManageResponse", "Wait", 1);
		addBehaviour(fsm);
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
	 * Initialize the contact list by taking a subset of all available agents
	 **/
	protected void intializeContactList(){
		// Stop initialization if contact list already exists
		if (contacts != null){
			return;
		}

		// Init contact list
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			//System.out.println(getAID().getLocalName() + ": the following agent have been added to contact list");
			// Create a sublist of contacts 
			Random rand = new Random();
			int nbContacts = rand.nextInt(result.length-1)+1; // Random int between 1 and nbAgents -1
			contacts = new AID[nbContacts];
			logger.log(Level.INFO, agentName + ": Adding " + nbContacts + " agents to the contact list out of " + result.length);
			for (int i = 0; i < nbContacts; i++){
				boolean added = false;
				while (!added){
					int id = rand.nextInt(result.length);
					for (int j = 0; j <= i; j++){
						if (contacts[j] == result[id].getName() || getAID().equals(result[id].getName())){
							added = false;
							break;
						} else {
							added = true;
						}
					}
					if (added){
						logger.log(Level.INFO, agentName + ": " + result[id].getName() + " has been added to the contact list");
						contacts[i] = result[id].getName();
					}
				}
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
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
		private static final long serialVersionUID=10L;
		private int nextState=0;

		public void onStart(){
			System.out.println(agentName + " is in Wait state");
		}

		@Override
		public void action(){
			logger.log(Level.INFO, agentName + " is waiting for message.");
			// Receiving message
			//
			currentMsg = myAgent.receive();
			if(currentMsg!=null){
				System.out.println("===== "+agentName+" received "+currentMsg.getPerformative(currentMsg.getPerformative()) +" from " + currentMsg.getSender().getLocalName());
				if(currentMsg.getPerformative()==ACLMessage.PROPOSE ||
				   currentMsg.getPerformative()==ACLMessage.CONFIRM){
					nextState=1;
				}else if(currentMsg.getPerformative()==ACLMessage.ACCEPT_PROPOSAL || 
					 currentMsg.getPerformative()==ACLMessage.REJECT_PROPOSAL ||
					 currentMsg.getPerformative()==ACLMessage.AGREE ||
					 currentMsg.getPerformative()==ACLMessage.REFUSE){
					nextState=2;
				}
			}else{
				block();
				nextState = 0;
			}
		}
		public int onEnd(){
			gui.updateCalendar();
			logger.log(Level.INFO, agentName + " is going to "+nextState+ " from "+getClass().getName());
			return nextState;
		}
	}


	/**
	 * This behaviour is used to send invitations
	 * THis behaviour is invoked using GUI
	 * */
	private class SendInvitation extends OneShotBehaviour{
		private static final long serialVersionUID=12L;
		/**
		 * Create Object Content for one Invitation
		 * We suppose that when we are sending invitation we want it at 1.0
		 * @param day	Day of Slot
		 * @param starttime	When start meeting
		 * @param duration	How long the meeting is
		 * */
		private Slot[] slots;
		private int day=-1;
		private int startTime=-1;
		private int duration=-1;
		private int retint=-1;

		public SendInvitation(){
			super();
		}

		public SendInvitation(int day, int startTime, int duration){
			super();
			this.day = day;
			this.startTime = startTime;
			this.duration = duration;
		}

		public void onStart(){
			logger.log(Level.INFO, agentName + " creates new invitation: day="+day+" time="+startTime+" duration="+duration);
		}

		@Override
		public void action(){
			// Initialize the contact list if needed
			intializeContactList();

			ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
			HashMap <String, Integer> invit = new HashMap<String, Integer>();
			
			myCalendar.manageSlot(this.day, this.startTime, this.duration, Slot.State.PROPOSED);
			invit.put("day", this.day);
			invit.put("duration", this.duration);
			invit.put("startTime", this.startTime);

			// If the agent doesnt have contacts, we got an error and don't send message
			if(contacts == null || contacts.length == 0){
				myCalendar.manageSlot(day, startTime, duration, Slot.State.FREE);
				retint = -1;
				logger.log(Level.SEVERE, agentName + " No contacts");
			}else{
				invitId++;
				msg.setConversationId("invit-"+agentName+"-"+invitId);
				for(AID aid: contacts)
					msg.addReceiver(aid);
				try{
					msg.setContentObject(invit);
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
			gui.updateCalendar();
			return retint;
		}
		
	}

	/**
	 * This Behaviour computes an invitation
	 **/
	private class ManageInvitation extends OneShotBehaviour{
		private int nextState = -1;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentName + " is in ManageInvitation state");
			if(currentMsg!=null)
				logger.log(Level.INFO, agentName + " received "+ACLMessage.getPerformative(currentMsg.getPerformative())+" id="+currentMsg.getConversationId() +" from "+currentMsg.getSender().getLocalName());	
		}


		@Override
		public void action(){
			int day=-1, startTime=-1, duration=-1;
			boolean freeSlots = false;
			HashMap<String, Integer> invit = new HashMap<String, Integer>();
			// Can only respond to a message from contacts and a message is a proposal
			if(currentMsg!=null){
				ACLMessage reply = currentMsg.createReply();
				try{
					invit = (HashMap<String, Integer>)currentMsg.getContentObject();
					day = invit.get("day");
					startTime = invit.get("startTime");
					duration = invit.get("duration");
				}catch(UnreadableException e){
					logger.log(Level.SEVERE, agentName + " Error receiving message ");
					nextState = -1;
					return;
				}catch(java.lang.NullPointerException e){
					logger.log(Level.SEVERE, agentName + " Error receiving message, invit is null");
					nextState = -1;
					return;
				}
				freeSlots = myCalendar.areSlotsFree(day, startTime, duration);
				if (currentMsg.getPerformative()==ACLMessage.PROPOSE){
					reply.setPerformative((freeSlots)?ACLMessage.ACCEPT_PROPOSAL:ACLMessage.REJECT_PROPOSAL);
				}else if(currentMsg.getPerformative()==ACLMessage.CONFIRM){
					reply.setPerformative((freeSlots)?ACLMessage.AGREE:ACLMessage.REFUSE);
					if(freeSlots)
						myCalendar.manageSlot(day, startTime, duration, Slot.State.LOCK);
				}
				try{
					reply.setContentObject(invit);
				}catch(IOException e){
					logger.log(Level.SEVERE, agentName + "Error adding ContentObject to msg");
					nextState = -1;
					return;
				}
				System.out.println("===== "+agentName + " replied to "+currentMsg.getSender().getLocalName()+" with " + reply.getPerformative(reply.getPerformative()));
				myAgent.send(reply);
			}
			nextState = 1;
			gui.updateCalendar();
		}

		public int onEnd(){
			gui.updateCalendar();
			logger.log(Level.INFO, agentName+" is going to "+nextState+" from ManageInvitation");
			return nextState;
		}
	}


	/**
	 * This behaviour computes a response	 
	 * */
	private class ManageResponse extends OneShotBehaviour{

		private int nextState = 1;

		@Override
		public void onStart(){
			logger.log(Level.INFO, agentName + " received response to " + currentMsg.getConversationId() +" from "+currentMsg.getSender().getLocalName());	
		}


		@Override
		public void action(){
			// Add the response to the invitTable
			// Check if we got all response for this invitation
			// if one is missing wait for it
			// else looks anwers, if there is negative one we should send new invitation with new time
			ArrayList<ACLMessage> rejected = new ArrayList<ACLMessage>();
			ArrayList<ACLMessage> accepted = new ArrayList<ACLMessage>();

			HashMap<String, Integer> invit = new HashMap<String, Integer>();
			int day=-1, startTime=-1, duration=-1;

			try{
				invit = (HashMap<String, Integer>)currentMsg.getContentObject();
				day = invit.get("day");
				startTime = invit.get("startTime");
				duration = invit.get("startTime");
			}catch(UnreadableException e){}
			
			String id = currentMsg.getConversationId();
			ArrayList<ACLMessage> list = (ArrayList<ACLMessage>)invitTable.get(id);
			list.add(currentMsg);

			if(list.size()==contacts.length){
				for (ACLMessage msg : list) {
					switch(msg.getPerformative()){
						case ACLMessage.ACCEPT_PROPOSAL: accepted.add(msg);break;
						case ACLMessage.REJECT_PROPOSAL: rejected.add(msg);break;
						
						case ACLMessage.AGREE: accepted.add(msg);break;
						case ACLMessage.REFUSE: rejected.add(msg);break;
					}
				}
				
				if(rejected.size()==0){
					// If messages are AGREE so the meeting is done
					if(currentMsg.getPerformative() == ACLMessage.AGREE){
						// lock meeting in calendar
						myCalendar.manageSlot(day, startTime, duration, Slot.State.LOCK);
						// remove entry from table
						invitTable.remove(id);
					}else if(currentMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
						// everyone agreed on the slot, we can send confirmation
						for(ACLMessage msg : list){
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							try{	
								reply.setContentObject(invit);
							}catch(IOException e){}
							System.out.println("===== " + agentName + " replied to " +currentMsg.getSender().getLocalName()+" with "+ACLMessage.getPerformative(reply.getPerformative()));
							myAgent.send(reply);
						}
						// clear list, we are expecting response to CONFIRM now
						list.clear();
					}
				}else{
					
					// some rejected, we have to propose new slot
				}
			}
			nextState = 1;
			
		}

		public int onEnd(){
			gui.updateCalendar();
			return nextState;
		}
	}
}
