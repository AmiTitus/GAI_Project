/**
 * 
 */
package gaiproject;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import gaiproject.Calendar;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
public class MeetingAgent extends Agent{
	private Calendar myCalendar;
	private AID[] meetingAgents;
	protected void setup(){
		// TODO
		System.out.println("Hello! " + getAID().getLocalName() + " is ready to work.");
	}
	protected void takeDown(){
		System.out.println("Meeting agent " + getAID().getLocalName() + " terminated.");
	}
}
