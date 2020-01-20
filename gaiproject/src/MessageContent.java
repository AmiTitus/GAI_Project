/*
 *
 */
package gaiproject;
import java.util.HashMap;
import java.util.ArrayList;
import gaiproject.Slot;
import java.io.Serializable;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
public class MessageContent implements Serializable{
	
	protected ArrayList <Slot> availableSlots;
	protected HashMap<String, Integer> invit;
	protected Integer cycle=0;

	/**
	 * Create a MessageContent with an invitation
	 * @param inv 	HashMap<String, Integer>
	 * */
	public MessageContent(HashMap<String, Integer> inv){
		this.invit = inv;
		this.availableSlots = null;
	}

	/**
	 * Create a MessageContent with an invitation and availableSlots
	 * @param inv 	HashMap<String, Integer>
	 * @param slots	ArrayList<Slot>
	 * */
	public MessageContent(HashMap<String, Integer> inv, ArrayList<Slot> slots){
		this.availableSlots = new ArrayList<Slot>();
		for (Slot elem : slots){
			this.availableSlots.add(elem);
		}
		//this.availableSlots = slots;
		this.invit = inv;
	}

	public HashMap<String, Integer> getInvit(){
		return this.invit;
	}

	public ArrayList<Slot> getAvailableSlots(){
		return this.availableSlots;
	}

	@Override
	public String toString(){
		return ("Invitation : " + this.invit + " - Available slots : " + this.availableSlots);
	}
}
