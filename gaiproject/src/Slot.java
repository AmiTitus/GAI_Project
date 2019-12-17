/**
 * 
 */
package gaiproject;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
class Slot {
	protected int startTime;
	protected int duration;
	protected Double wanted=0.0;
	protected boolean lock = false;

	private Logger logger = Logger.getLogger(Slot.class.getName());

	public Slot(int startTime,  int duration){
		this.startTime = startTime;
		this.duration = duration;
	}

	public Slot(int startTime, int duration, boolean lock){
		this.startTime = startTime;
		this.duration = duration;
		this.lock = lock;
	}

	public Slot(int startTime,  int duration, Double wanted){
		assert wanted >= 0 && wanted <= 1 : " Incorrect value for wanted";
		this.startTime = startTime;
		this.duration = duration;
		this.wanted = wanted;
	}


	public Slot(int startTime,  int duration, boolean lock, Double wanted){
		assert wanted >= 0 && wanted <= 1 : " Incorrect value for wanted";
		this.startTime = startTime;
		this.duration = duration;
		this.wanted = wanted;
		this.lock = lock;
	}

	
	public void lock(){
		if(lock)
			logger.log(Level.WARNING, "The slot was already lock");
		lock = true;
	}

	public void unlock(){
		if(!lock)
			logger.log(Level.WARNING, "The slot was already unlock");
		lock = false;
	}

	public String toString(){
		return "SlotTime["
			+ "startTime" + startTime
			+ "duration" + duration
			+ "lock" + lock
			+ "wanted" + wanted
			+ "]";
	}

}
