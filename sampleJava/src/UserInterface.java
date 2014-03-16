package userInterface;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class UserInterface implements Runnable {

    // creating window
    private JFrame f = new JFrame("User Interface");

    @Override
    public void run() {
        // create record button
        JButton record = new JButton("    record    ");
        // create stopRecord button
        JButton StopRecord = new JButton("stop recording");
        // Sets the behavior for when the window is closed
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // set size
        f.setSize(140,120);
        // Add a layout manager so that the button is not placed on top of the label
        f.setLayout( new GridBagLayout());
        // f.setLayout(new FlowLayout());
        final GridBagConstraints GBC = new GridBagConstraints();
        
        // ADDING TWO BUTTONS TO WINDOW //
        GBC.gridx = 0; // x coordinate
        GBC.gridy = 0; // y coordinate
        f.add(record, GBC); // adding in button
        
        GBC.gridx = 0; // x coordinate
        GBC.gridy = 1; // y coordinate
        f.add(StopRecord, GBC); // adding in button
        
        // Arrange the components inside the window
        //f.pack();
        // By default, the window is not visible. Make it visible.
        f.setVisible(true);
        
        record.addActionListener(new ActionListener(){ // setting action to button
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                System.out.println("recording...."); // printing out to console when button is clicked
                
            }   
        });
        
        StopRecord.addActionListener(new ActionListener(){ // setting action to button
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                System.out.println("recording stopped"); // printing out to console when button is clicked
                
            }   
        });
    }

    public static void main(String[] args) {
        UserInterface mWindow = new UserInterface();
        // Schedules the application to be run at the correct time in the event queue.
        SwingUtilities.invokeLater(mWindow);
    }

}