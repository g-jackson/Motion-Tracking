import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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

	private static final long serialVersionUID = 1L;
	protected JButton record;
    protected JButton stopRecord;
    protected JButton save;
    protected JButton compare;
	protected JComboBox actionList;
    private int saves;
    // update with type used in comparison algorithm 
    private String currentActionData= "";
	
    public UserInterface() {
        // Initialise all buttons and menus
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
    	    	System.out.println(cb.getSelectedItem() + " selected");
    	    	currentActionData= loadFile("src/loads/"+cb.getSelectedItem()+".txt");
    	    }
        });
        // load default action
        currentActionData= loadFile("src/loads/"+actionList.getSelectedItem()+".txt");
        		
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
        // create compare button
        compare = new JButton("Compare");
        compare.setActionCommand("COMP");
        compare.setEnabled(false);
 
        record.addActionListener(this);
        stopRecord.addActionListener(this);
        save.addActionListener(this);
        compare.addActionListener(this);
        
        add(record);
        add(stopRecord);
        add(actionList);
        add(save);
        add(compare);
    }
    
    
    //Listens to the record, stop, save and compare
    public void actionPerformed(ActionEvent event){
    	String actionCommand = event.getActionCommand();
        if ("REC".equals(actionCommand)) {
          // start recording
          record.setEnabled(false);
          stopRecord.setEnabled(true);
          actionList.setEnabled(false);
          compare.setEnabled(false);
          save.setEnabled(false);
          // printing out to console when button is clicked
          System.out.println("recording....");
          saves++;
        }
        else if("STOP".equals(actionCommand)) {
          // stop recording
          record.setEnabled(true);
          stopRecord.setEnabled(false);
	      actionList.setEnabled(true);
	      save.setEnabled(true);
	      // printing out to console when button is clicked
	      System.out.println("recording stopped");
        }
        else if("SAV".equals(actionCommand)) {
        	// save file as "out<save number>.txt"
        	// currently only saves the selected action
          try {
        	String content = ""+ actionList.getSelectedItem() + " data";
            File newTextFile = new File("src/saves/out"+saves+".txt");
            FileWriter fileWriter = new FileWriter(newTextFile);
            fileWriter.write(content);
            fileWriter.close();
			} catch (IOException e) {
			  e.printStackTrace();
			}
          System.out.println("File saved as a " + actionList.getSelectedItem() + " action");
          compare.setEnabled(true);
        }
        else if("COMP".equals(actionCommand)) {
        	// compares the last saved file with the current selected action file
        	String cmp = loadFile("src/saves/out"+saves+".txt");
        	System.out.println("Loaded saved file: " + cmp);
        	System.out.println("Selected action file: " + currentActionData);
        	if(cmp.equals(currentActionData)){
        		System.out.println("Files match");
        	}
        	else{
        		System.err.println("Files don't match");
        	}
        }
    }

    // create icons for buttons
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
    
    // load a file contents into a multi-line string
    protected static String loadFile(String path) {
        String str = "";
        String fileContents= "";
    	File file = new File(path);
        BufferedReader reader = null;
        if (file.exists() && file.isFile()) {
        	try {
				reader = new BufferedReader(new FileReader(file));
				while((str=reader.readLine()) != null){
					fileContents+= str+"\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try{ 
					reader.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
        else {
            System.err.println("Couldn't find file: " + path);
        }
        return fileContents;
    }
    
    // simple main for testing
    public static void main(String[] args){
    	JFrame mFrame = new JFrame();
    	mFrame.add(new UserInterface());
    	mFrame.setSize(400,200);
		mFrame.setVisible(true); 
    }
 }