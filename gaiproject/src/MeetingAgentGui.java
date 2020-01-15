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


	/* *
	 *	Calendar Class
	 *	Create a graphical Calendar as a JComponent
	 * */
	private class CalendarGrid extends JComponent{
		private static final long serialVersionUID = 2L;
		private boolean daltonism = false;

		CalendarGrid() {
			int width = myCalendar.calendarTable[0].length*RECT_SIZE;
			int height =  myCalendar.calendarTable.length*RECT_SIZE;
            		setPreferredSize(new Dimension(width, height));	
        	}

		protected void setDaltonism(boolean d){
			this.daltonism = d;
		}
		
		/* *
		 *	Draw JComponents
		 * */
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(Color.WHITE);
		
			for(int day=0; day<myCalendar.calendarTable.length; day++){
				for(int time=0; time<myCalendar.calendarTable[0].length; time++){
					if (myCalendar.getSlot(day, time).currentState.equals(Slot.State.FREE)){
						g.setColor(this.daltonism?Color.GREEN:Color.GREEN);
					}else if(myCalendar.getSlot(day, time).currentState.equals(Slot.State.PROPOSED)){
						g.setColor(this.daltonism?Color.PINK:Color.ORANGE);
					}else{
						g.setColor(this.daltonism?Color.BLUE:Color.RED);
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

		Checkbox daltonism = new Checkbox("Quentin"); 
		
		// Calendar Panel
		calendarPanel = new JPanel();
		CalendarGrid calendarGrid = new CalendarGrid();

		calendarPanel.add(calendarGrid);


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
		
		p.add(daltonism);
		
		getContentPane().add(p, BorderLayout.CENTER);
		getContentPane().add(calendarPanel, BorderLayout.WEST);

		JButton addButton = new JButton("Create Invitation");

		daltonism.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				calendarGrid.setDaltonism(e.getStateChange()==1);
				calendarGrid.repaint();
			}
		});

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

	/* *
	 * Methods to refresh the calendar
	 * */
	public void updateCalendar(){
		calendarPanel.repaint();
	}

	/* *
	 *	Display GUI
	 * */
	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}

	/* *
	 *	Close GUI
	 * */
	public void close(){
		setVisible(false);
	}
}
