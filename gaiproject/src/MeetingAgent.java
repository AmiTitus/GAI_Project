/**
 * 
 */
package gaiproject;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import gaiproject.Calendar;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
public class MeetingAgent extends Agent{
	private Calendar myCalendar;
	private AID[] meetingAgents;
	private Logger logger = Logger.getLogger(MeetingAgent.class.getName());
	protected void setup(){
		// Init calendar randomly to have already planned meeting
		myCalendar = new Calendar(true);
		myCalendar.prettyPrint();
		logger.log(Level.INFO, "Hello! " + getAID().getLocalName() + " is ready to work.");
	}
	protected void takeDown(){
		logger.log(Level.INFO, "Meeting agent " + getAID().getLocalName() + " terminated.");
	}
}
