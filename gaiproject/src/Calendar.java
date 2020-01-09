/**
 * 
 */
package gaiproject;
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
	private int numberDays = 5;
	private int numberSlotPerDay = 24;

	public Calendar(boolean rand){
		this.calendarTable = new Slot[this.numberDays][this.numberSlotPerDay];
		if(rand){
			randomlyInitCalendarTable();
		}else{
			initCalendarTable();	
		}
	}

	public Calendar(int numberDays, int numberSlotPerDay, boolean rand){
		this.numberDays = numberDays;
		this.numberSlotPerDay = numberSlotPerDay;
		this.calendarTable = new Slot[numberDays][numberSlotPerDay];	
		if(rand){
			randomlyInitCalendarTable();
		}else{
			initCalendarTable();	
		}
	}


	/**
 	* Initialize the calendar with unlock slots
	* */
	private void initCalendarTable(){
		for(int day=0; day<this.numberDays;day++){
			for(int time=0; time<this.numberSlotPerDay; time++){
				this.calendarTable[day][time] = new Slot(time);
			}
		}
	}

	private void randomlyInitCalendarTable(){
		Double treshold = 0.25;
		double x;
		Slot s;
		for (int day=0; day<this.numberDays; day++){
			for (int time=0; time<this.numberSlotPerDay; time++){
				x = Math.random();
				if (x < treshold){
					s = new Slot(time, true);
				}else{
					s = new Slot(time, false);
				}
				this.calendarTable[day][time] = s;
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


	public void prettyPrint(){
		String lines = "";
		String line = "----";
		String column = "|";
		boolean l;
		for (int day=0; day<this.numberDays; day++){
			lines += "\n"+column;
			for (int time=0; time<this.numberSlotPerDay; time++){
				l = this.calendarTable[day][time].lock;
				lines += (l) ? " T " : " F ";
				lines += column;
			}
		}
		System.out.println(lines);
	}

}
