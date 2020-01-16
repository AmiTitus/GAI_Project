package gaiproject;

import jade.core.AID;
import gaiproject.MeetingAgent;
import gaiproject.Calendar;
import gaiproject.Slot;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MeetingAgentGui extends JFrame {	
	private static final long serialVersionUID = 1L;
	private MeetingAgent myAgent;
	private Calendar myCalendar;
	private final int RECT_SIZE = 10;
	private final int RECT_GAP = 5;
	private JTextField dayField, durationField, timeField;
	private JPanel p, calendarPanel;

	private class CalendarGrid extends JComponent{
		private static final long serialVersionUID = 2L;
		CalendarGrid() {
            		setPreferredSize(new Dimension(myCalendar.calendarTable[0].length*RECT_SIZE,
						       myCalendar.calendarTable.length*RECT_SIZE));
        	}

		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(Color.WHITE);
		
			for(int day=0; day<myCalendar.calendarTable.length; day++){
				for(int time=0; time<myCalendar.calendarTable[0].length; time++){
					if (myCalendar.getSlot(day, time).currentState.equals(Slot.State.FREE)){
						g.setColor(Color.GREEN);
					}else if(myCalendar.getSlot(day, time).currentState.equals(Slot.State.PROPOSED)){
						g.setColor(Color.ORANGE);
					}else{
						g.setColor(Color.RED);
					}
					Rectangle r = new Rectangle(time*RECT_SIZE, day*RECT_SIZE, RECT_SIZE, RECT_SIZE);
					g.fillRect(r.x, r.y, r.width, r.height);
					
				}
			}
		
		}
	}

	MeetingAgentGui(MeetingAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		myCalendar = a.getCalendar();

		// Calendar Panel
		calendarPanel = new JPanel();
		calendarPanel.add(new CalendarGrid());


		p = new JPanel();
		p.setLayout(new GridLayout(5, 2));
		// Day Field
		p.add(new JLabel("Day:"));
		dayField = new JTextField(15);
		p.add(dayField);
		// Time Field
		p.add(new JLabel("Time:"));
		timeField = new JTextField(15);
		p.add(timeField);
		// Duration Field
		p.add(new JLabel("Duration:"));
		durationField = new JTextField(15);
		p.add(durationField);

		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(calendarPanel, BorderLayout.WEST);

		JButton addButton = new JButton("Create Invitation");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				String text;
				try {
					text = dayField.getText().trim();
					int day = Integer.parseInt(text);	
					text = timeField.getText().trim();
					int time = Integer.parseInt(text);
					text = durationField.getText().trim();
					int duration = Integer.parseInt(text);

					if (day >= myCalendar.numberDays ||
					    time >= myCalendar.numberSlotPerDay ||
					    duration+time > myCalendar.numberSlotPerDay) {
						System.out.println("Error: The meeting information are invalid");
						System.out.println(duration+time);
						System.out.println(myCalendar.numberSlotPerDay);
						return;
					}
					myAgent.sendInvitation(day, time, duration);
					dayField.setText("");
					timeField.setText("");
					durationField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(MeetingAgentGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}

	public void updateCalendar(){
		calendarPanel.repaint();
	}

	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}	
}
