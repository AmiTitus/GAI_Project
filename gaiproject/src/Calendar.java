/**
 * 
 */
package gaiproject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import gaiproject.Slot;
/**
 * @author Maxime Dhaisne
 * @author Quentin Lanusse
 *
 */
class Calendar {

	protected Slot[][] calendarTable;
	protected int numberDays = 5;
	protected int numberSlotPerDay = 24;

	public Calendar(){
		this.calendarTable = new Slot[this.numberDays][this.numberSlotPerDay];
		initCalendarTable();	
	}

	public Calendar(int numberDays, int numberSlotPerDay){
		this.numberDays = numberDays;
		this.numberSlotPerDay = numberSlotPerDay;
		this.calendarTable = new Slot[numberDays][numberSlotPerDay];	
		initCalendarTable();
	}


	/**
 	* Initialize the calendar with unlock slots
	* */
	private void initCalendarTable(){
		for(int day=0; day<this.numberDays;day++){
			for(int time=0; time<this.numberSlotPerDay; time++){
				this.calendarTable[day][time] = new Slot(day, time);
			}
		}
	}

	/**
	 * Initialize a calendar with random values
	 * @param lockTreshhold		double
	 * @param wantedTreshhold	double
	 * */
	public void randomlyInitCalendar(double lockTreshhold, double wantedTreshhold){	
		double x, wanted;
		Slot s;
		for (int day=0; day<this.numberDays; day++){
			for (int time=0; time<this.numberSlotPerDay; time++){
				x = Math.random();	
				wanted = Math.random() * (1.0 - wantedTreshhold) + wantedTreshhold;
				if (x < lockTreshhold){
					s = new Slot(day, time, wanted, true);
				}else{
					s = new Slot(day, time, wanted, false);
				}
				this.calendarTable[day][time] = s;
			}
		}
	}

	/**
	 * Initialize a calendar with random values
	 * @param c	Calendar
	 * @param lockTreshhold		double
	 * @param wantedTreshhold	double
	 * */
	public static void randomlyInitCalendar(Calendar c, double lockTreshhold, double wantedTreshhold){
		double x, wanted;
		Slot s;
		for (int day=0; day<c.numberDays; day++){
			for (int time=0; time<c.numberSlotPerDay; time++){
				wanted = Math.random() * (+1.0 - wantedTreshhold) + wantedTreshhold;
				x = Math.random();
				if (x < lockTreshhold){
					s = new Slot(day, time, wanted, true);
				}else{
					s = new Slot(day, time, wanted, false);
				}
				c.calendarTable[day][time] = s;
			}
		}
	}

	public Calendar(Calendar calendar){
		this.calendarTable = calendar.calendarTable;
		this.numberDays = calendar.numberDays;
		this.numberSlotPerDay = calendar.numberSlotPerDay;
	}

	/**
 	* Return is the slot is free
 	* @param day 	Day of the slot
 	* @param time	Time of the slot
 	* @return true if the slot is lock else false
	* */
	public boolean isSlotFree(int day, int time){
		assert day < this.numberDays : "Incorrect value day";
		assert time > 0 && time < this.numberSlotPerDay : "Incorrect value time";
		return this.calendarTable[day][time].lock;
	}

	/**
	 * Return if the slots are free
	 * @param day	Day of meeting
	 * @param time	Time of meetin
	 * @param duration	Duration of meeting
	 * @return true if slots are free else false
	 * */
	public boolean areSlotsFree(int day, int startTime, int duration){	
		assert startTime > 0 && startTime < this.numberSlotPerDay : "Incorrect value startTime";
		assert duration > 0 && startTime+duration <= this.numberSlotPerDay: "Incorrect value duration";
		assert day >0 && day < this.numberDays : "Incorrect value day";
		for(int t=startTime; t<startTime+duration; t++) if (!this.calendarTable[day][t].currentState.equals(Slot.State.FREE)) return false;
		return true;
	}

	/**
 	* Insert a Slot
 	* @param s 	Slot to insert
 	* @param day	Day to insert the Slot
	* */
	public void insertSlot(Slot s, int day){
		assert day < this.numberDays: "Incorrect value day";
		for (int time=s.startTime; time<s.startTime+s.duration; time++){
			this.calendarTable[day][time] = s;
		}
	}

	/**
 	* Locking a Slot
	* @Deprecated Now please use manageSlot() instead
 	* @param day 	Day of the slot
 	* @param startTime	Time of the slot
 	* @param duration	Duration of the slot
	* */
	public void lockingSlot(int day, int startTime, int duration){
		assert day < this.numberDays : "Incorrect value day";
		for(int time=startTime; time<startTime+duration; time++){
			this.calendarTable[day][time].lock();
		}
	}

	/**
	 * Manage state of slots in calendar
	 * @param day 	Day of slot
	 * @param startTime	Time of the slot
	 * @param duration	Duration of meeting
	 * @param state		in which state the slot will be
	 * */
	public void manageSlot(int day, int startTime, int duration, Slot.State state){
		assert startTime > 0 && startTime < this.numberSlotPerDay : "Incorrect value startTime";
		assert duration > 0 : "Incorrect value duration";
		assert day < this.numberDays : "Incorrect value day";
		for(int time=startTime; time<startTime+duration; time++){
			switch(state){
				case FREE: this.calendarTable[day][time].unlock();break;
				case LOCK: this.calendarTable[day][time].lock();break;
				case PROPOSED: this.calendarTable[day][time].propose();break;
			}
		}
	}


	/**
 	* Return a Slot
 	* @param day 	Day of the slot
 	* @param startTime	Time of the slot
 	* @return Slot
	* */
	public Slot getSlot(int day, int startTime){
		assert day < this.numberDays : "Incorrect value day";
		assert startTime > 0 && startTime < this.numberSlotPerDay : "Incorrect value startTime";
		return this.calendarTable[day][startTime];
	}

	/**
	 * Get an ArrayList of wanted slot equal or greater than the treshhold
	 * @param day	Day
	 * @param treshhold 	treshhold value
	 * @return ArrayList<Slot>
	 * */
	public ArrayList<Slot> getWantedSlots(int day, double treshhold){
		assert day < this.numberDays && day >= 0: "Incorrect value day";
		assert treshhold >= 0 && treshhold <= 1: "Incorrect treshhold value";
		ArrayList<Slot> slots = new ArrayList<Slot>();
		for(int t=0; t<this.numberSlotPerDay; t++){
			if(this.calendarTable[day][t].wanted >= treshhold &&
			   this.calendarTable[day][t].currentState == Slot.State.FREE)
				slots.add(this.calendarTable[day][t]);
		}
		return slots;
	}

	public Slot[][] getCalendarTable(){
		return this.calendarTable;
	}

	public void prettyPrint(){
		String lines = "";
		String line = "----";
		String column = "|";
		Slot.State l;
		for (int day=0; day<this.numberDays; day++){
			lines += "\n"+column;
			for (int time=0; time<this.numberSlotPerDay; time++){
				l = this.calendarTable[day][time].currentState;
				lines += (l == Slot.State.LOCK) ? " O " : (l == Slot.State.PROPOSED) ? " P " : "   ";
				lines += column;
			}
		}
		System.out.println(lines);
	}

}
