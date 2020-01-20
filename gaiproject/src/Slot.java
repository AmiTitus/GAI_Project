/**
 * 
 */
package gaiproject;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import java.io.Serializable;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */

class Slot implements Serializable{	
	protected enum State {
		FREE,
		PROPOSED,
		LOCK
	}
	protected State currentState=State.FREE;
	protected int startTime;
	protected int day;
	protected int duration=1;
	protected double wanted=0.0;
	protected boolean lock = false; //meeting

	//private Logger logger = Logger.getLogger(Slot.class.getName());

	public Slot(int day, int startTime){
		this.startTime = startTime;
		this.day = day;
	}

	public Slot(int day, int startTime, boolean lock){
		this.startTime = startTime;
		this.lock = lock;
		this.day = day;
		if(lock)
			this.currentState=State.LOCK;
	}

	public Slot(int day, int startTime, double wanted, boolean lock){
		this.startTime = startTime;
		this.lock = lock;
		this.day = day;
		this.wanted = wanted;
		if(lock)
			this.currentState=State.LOCK;
	}

	public Slot(int day, int startTime,  int duration){
		this.startTime = startTime;
		this.day = day;
		this.duration = duration;
	}

	public Slot(int day, int startTime, int duration, boolean lock){
		this.startTime = startTime;
		this.day = day;
		this.duration = duration;
		this.lock = lock;
		if(lock)
			this.currentState=State.LOCK;
	}

	public Slot(int day, int startTime,  int duration, double wanted){
		assert wanted >= 0 && wanted <= 1 : " Incorrect value for wanted";
		this.startTime = startTime;
		this.day = day;
		this.duration = duration;
		this.wanted = wanted;
	}


	public Slot(int day, int startTime,  int duration, boolean lock, double wanted){
		assert wanted >= 0 && wanted <= 1 : " Incorrect value for wanted";
		this.startTime = startTime;
		this.day = day;
		this.duration = duration;
		this.wanted = wanted;
		this.lock = lock;
		if(lock)
			this.currentState=State.LOCK;
	}

	/**
	 * Set the slot to LOCK
	 * */
	public void lock(){
		lock = true;
		this.currentState = State.LOCK;
	}

	/**
	 * Set the slot to FREE
	 * */
	public void unlock(){
		lock = false;
		this.currentState = State.FREE;
	}

	/**
	 * Set the slot to PROPOSED
	 * */
	public void propose(){
		this.currentState = State.PROPOSED;
	}

	/**
	 * set wanted value of the slot
	 * @param wanted	value of wanted
	 * */
	public void setWanted(Double wanted){	
		assert wanted >= 0 && wanted <= 1 : " Incorrect value for wanted";
		this.wanted = wanted;
	}

	/**
	 * get the wanted value of slot
	 * @return Double
	 * */
	public Double getWanted(){
		return wanted;
	}

	public String toString(){
		return "SlotTime["
			+ "startTime" + startTime
			+ "day" + day
			+ "duration" + duration
			+ "lock" + lock
			+ "wanted" + wanted
			+ "]";
	}

}
