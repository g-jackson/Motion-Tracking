import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openni.Device;
import org.openni.OpenNI;

import org.openni.*;
import com.primesense.nite.*;

import javax.swing.JOptionPane;

public class UserViewerApplication {

	private JFrame mFrame;
	private UserViewer mViewer;
	private boolean mShouldRun = true;

	public UserViewerApplication(UserTracker tracker) {
		mFrame = new JFrame("NiTE User Tracker Viewer");
		mViewer = new UserViewer(tracker);

		// register to closing event
		mFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mShouldRun = false;
			}
		});

		// register to key events
		mFrame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					mShouldRun = false;
				}
				
				if(arg0.getKeyCode() == KeyEvent.VK_Q){
					System.out.println(mViewer.getQuaternionsToString());
				}
				
				if(arg0.getKeyCode() == KeyEvent.VK_F){
					System.out.println(mViewer.getFloorPlaneToString());
				}
			}
		});
		
		mViewer.setSize(800, 600);
		
		mFrame.add(mViewer);
		JPanel buttons = new UserInterface();
		mFrame.add(buttons);
		mFrame.setSize(new Dimension( (int)((buttons.getSize().getWidth()>mViewer.getSize().getHeight())? buttons.getSize().getWidth() : mViewer.getSize().getWidth()), (int)(buttons.getSize().getHeight() + mViewer.getSize().getHeight())));
		mFrame.setResizable(false);
		mFrame.setVisible(true); 
		
	}

	void run() {
		while (mShouldRun) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mFrame.dispose();
	}

	public static void main(String s[]) {
		// initialize OpenNI and NiTE
		try{
			OpenNI.initialize();
			NiTE.initialize();
		} catch(java.lang.RuntimeException e){
			System.out.println(e.toString());
		}
			
		List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
		if (devicesInfo.size() == 0) {
			JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Device device = Device.open(devicesInfo.get(0).getUri());
		UserTracker tracker = UserTracker.create();

		final UserViewerApplication app = new UserViewerApplication(tracker);
		app.run();

		NiTE.shutdown();
	}
}
