import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    protected JButton save;
	protected JComboBox actionList;
    
    public UserInterface() {
        // initialize all buttons and menus
        ImageIcon playIcon = createImageIcon("images/play.gif");
	    ImageIcon stopIcon = createImageIcon("images/stop.gif");
	    ImageIcon saveIcon = createImageIcon("images/save.gif");
        // create action menu
        String[] actions= {"Jumping", "Throwing", "Kicking", "Skipping"};
        actionList= new JComboBox(actions);
        actionList.setSelectedIndex(0);
        actionList.addActionListener(new ActionListener(){
    	    public void actionPerformed(ActionEvent event) {
    		    // handle selected action here
    	    	JComboBox cb = (JComboBox) event.getSource();
    	    	int index = (int) cb.getSelectedIndex();
    	    	switch(index){
    	    		case 0:
    	    			System.out.println("Jumping selected");
    	    			break;
    	    		case 1:
    	    			System.out.println("Throwing selected");
    	    			break;
    	    		case 2:
    	    			System.out.println("Kicking selected");
    	    			break;
    	    		case 3:
    	    			System.out.print("Skipping selected");
    	    			break;
    	    			
    	    	}
    		    
    	    }
        });
        // create record button
        record = new JButton("Record", playIcon);
        record.setActionCommand("REC");
        // create stopRecord button
        stopRecord = new JButton("Stop", stopIcon);
        stopRecord.setActionCommand("STOP");
        stopRecord.setEnabled(false);
        // create save button
        save = new JButton("", saveIcon);
        save.setActionCommand("SAV");
        save.setEnabled(false);
 
        record.addActionListener(this);
        stopRecord.addActionListener(this);
        save.addActionListener(this);
        
        add(record);
        add(stopRecord);
        add(actionList);
        add(save);
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
	      save.setEnabled(true);
	      // printing out to console when button is clicked
	      System.out.println("recording stopped");
        }
        else if("SAV".equals(event.getActionCommand())) {
        	try {
        		String content = "Sample output";
                File newTextFile = new File("src/saves/out.txt");
                FileWriter fileWriter = new FileWriter(newTextFile);
                fileWriter.write(content);
                fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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