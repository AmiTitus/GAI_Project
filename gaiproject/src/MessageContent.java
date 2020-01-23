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
 * Represent the content of an invitation
 */
public class MessageContent implements Serializable{
	
	protected ArrayList <Slot> availableSlots;
	protected HashMap<String, Integer> invit;

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


	/**
	 * Getter for invitation
	 * @return HashMap<String, Integer>
	 * */
	public HashMap<String, Integer> getInvit(){
		return this.invit;
	}

	/**
	 * Getter for list of avaible slots
	 * @return ArrayList<Slot>
	 * */
	public ArrayList<Slot> getAvailableSlots(){
		return this.availableSlots;
	}

	@Override
	public String toString(){
		return ("Invitation : " + this.invit + " - Available slots : " + this.availableSlots);
	}
}
