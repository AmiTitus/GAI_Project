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

	public MessageContent(HashMap<String, Integer> inv){
		this.invit = inv;
		this.availableSlots = null;
	}

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
