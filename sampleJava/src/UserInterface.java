import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class UserInterface extends JPanel
				implements ActionListener{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JButton record;
    protected JButton stopRecord;
    protected JComboBox<String> actionList;
    
    public UserInterface() {
        // initialize all buttons and menus
        ImageIcon playIcon = createImageIcon("images/play.gif");
	    ImageIcon stopIcon = createImageIcon("images/stop.gif");
        // create action menu
        String[] actions= {"Jumping", "Throwing", "Kicking", "Skipping"};
        actionList= new JComboBox<String>(actions);
        actionList.setSelectedIndex(0);
        actionList.addActionListener(new ActionListener(){
    	    public void actionPerformed(ActionEvent event) {
    		    // handle selected action here
    		    // switch(event.getIndex()){}
    		    
    	    }
        });
        // create record button
        record = new JButton("Record", playIcon);
        record.setActionCommand("REC");
        // create stopRecord button
        stopRecord = new JButton("Stop", stopIcon);
        stopRecord.setActionCommand("STOP");
        stopRecord.setEnabled(false);
 
        record.addActionListener(this);
        stopRecord.addActionListener(this);
        
        add(record);
        add(stopRecord);
        add(actionList);
    }

    //Listens to the record and stop
    public void actionPerformed(ActionEvent event){
        if ("REC".equals(event.getActionCommand())) {
          // start recording
          record.setEnabled(false);
          stopRecord.setEnabled(true);
          actionList.setEnabled(false);
          // printing out to console when button is clicked
          System.out.println("recording....");
        }
        else if("STOP".equals(event.getActionCommand())) {
    	  // stop recording
          record.setEnabled(true);
          stopRecord.setEnabled(false);
	      actionList.setEnabled(true);
	      // printing out to console when button is clicked
	      System.out.println("recording stopped");
        }
    }

    // create icons for record and stop recording
    // can be used to load in files to compare to
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = UserInterface.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private static void createAndShowGUI() {
        final JFrame f = new JFrame("User Interface");
        //Create and set up the content pane.
        UserInterface window = new UserInterface();
        //content panes must be opaque
        //window.setOpaque(true);
        f.setContentPane(window);
        // Sets the behavior for when the window is closed
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // set size
        f.setSize(240,120);
        // Arrange the components inside the window
        //f.pack();
        // By default, the window is not visible. Make it visible.
        f.setVisible(true);
    }
    
    public static void main(String[] args) {
	      // Schedules the application to be run at the correct time in the event queue.
	      javax.swing.SwingUtilities.invokeLater(new Runnable(){
	    	    @Override
	    	    public void run() {
	    	    	createAndShowGUI();
	    	    }
	      });
	  }
}