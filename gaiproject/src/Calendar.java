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


	private void initCalendarTable(){
		for(int day=0; day<this.numberDays;day++){
			for(int time=0; time<this.numberSlotPerDay; time++){
				this.calendarTable[day][time] = new Slot(time);
			}
		}
	}

	public Calendar(Calendar calendar){
		this.calendarTable = calendar.calendarTable;
		this.numberDays = calendar.numberDays;
		this.numberSlotPerDay = calendar.numberSlotPerDay;
	}

	public boolean isSlotFree(int day, int time){
		assert day < this.numberDays : "Incorrect value day";
		assert time > 0 && time < this.numberSlotPerDay : "Incorrect value time";
		return this.calendarTable[day][time].lock;
	}

	public void insertSlot(Slot s, int day){
		assert day < this.numberDays: "Incorrect value day";
		for (int time=s.startTime; time<s.startTime+s.duration; time++){
			this.calendarTable[day][time] = s;
		}
	}

	public void insertSlot(int day, int startTime, int duration){
		assert day < this.numberDays : "Incorrect value day";
		for(int time=startTime; time<startTime+duration; time++){
			this.calendarTable[day][time] = new Slot(startTime, duration, true);
		}
	}


}
