import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class UserInterface extends JPanel
				implements ActionListener 
{
	protected JButton recButton, sButton;
	protected JComboBox<String> actionList;

public UserInterface() {
	String[] actions= {"Jumpung", "Throwing", "Kicking", "Skipping"};
	ImageIcon playIcon = createImageIcon("images/play.gif");
	ImageIcon stopIcon = createImageIcon("images/stop.gif");

	recButton= new JButton("Record", playIcon);
	recButton.setEnabled(true);
	recButton.setActionCommand("REC");
	
	sButton= new JButton("Stop", stopIcon);
	sButton.setActionCommand("STOP");
	sButton.setEnabled(false);
	
	recButton.addActionListener(this);
    sButton.addActionListener(this);
    actionList= new JComboBox<String>(actions);
    actionList.setSelectedIndex(0);
    actionList.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent event) {
    		//handle selected action here
    	}
    });    
    
    add(recButton);
    add(sButton);
    add(actionList);
}
//Listens to the record and stop
public void actionPerformed(ActionEvent event){
    if ("REC".equals(event.getActionCommand())) {
        //start recording
        recButton.setEnabled(false);
        sButton.setEnabled(true);
        actionList.setEnabled(false);
    } 
    else if("STOP".equals(event.getActionCommand())) {
    	//stop recording
        recButton.setEnabled(true);
        sButton.setEnabled(false);
	   actionList.setEnabled(true);
    }
}

//create icons for record and stop recording
protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = UserInterface.class.getResource(path);
    if (imgURL != null) {
        return new ImageIcon(imgURL);
    } else {
        System.err.println("Couldn't find file: " + path);
        return null;
    }
}

private static void createAndShowGUI() {

    //Create and set up the window.
    JFrame frame = new JFrame("User Interface");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    UserInterface window = new UserInterface();
    window.setOpaque(true); //content panes must be opaque
    frame.setContentPane(window);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
}

public static void main(String[] args) {
		UserInterface mWindow = new UserInterface();
	    // Schedules the application to be run at the correct time in the event queue.
	    javax.swing.SwingUtilities.invokeLater(new Runnable(){
	    	@Override
	    	public void run() {
                createAndShowGUI(); 
            }
	    });
	}	
}
