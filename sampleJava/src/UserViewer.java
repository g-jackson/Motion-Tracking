import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.image.*;

import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.JointType;

import org.openni.VideoFrameRef;

import de.yvert.geometry.*;

public class UserViewer extends Component implements UserTracker.NewFrameListener {
	//error states
	final static int STATE_ERROR_NO_USER = 0; //there must be one user only
	final static int STATE_ERROR_TOO_MANY_USERS = 1; //there must be one user only
	final static int STATE_ERROR_USER_NOT_IN_FRAME = 2; //both head and feet must both be in frame

	//testing states
	final static int STATE_CALIBRATE = 3; //wait for a few seconds to calibrate
	final static int STATE_BEGIN = 4; //first pose
	final static int STATE_BEND = 5; //bending down in preparation to jump
	final static int STATE_JUMP = 6; //mid air
	final static int STATE_FINISHED = 7; //successful test

	private final static Vector3 upVector = new Vector3(0.0, 1.0, 0.0);

	private final static int[] mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };

	private final static String[] stateNames = new String[]{"no user", "more than one user", "user not in frame", "calibrating", "begin", "bend", "jump", "finish"};

	private final static List<JointType> limbEndTypes = Arrays.asList(new JointType[]{ JointType.LEFT_HAND, JointType.RIGHT_HAND, JointType.LEFT_FOOT, JointType.RIGHT_FOOT });

	//displayed bitmap
	private float mHistogram[];
	private int[] mDepthPixels;
	private BufferedImage mBufferedImage;

	//framework
	private UserTracker mTracker;
	private UserTrackerFrameRef mLastFrame;

	//tracking
	private int state = STATE_CALIBRATE; //initial state
	private int lastState = state;

	private UserData targetUser = null;
	private Skeleton skeleton = null;
	private String statusString = null;

	//joint values: some are needed for pose matching, but all are needed for drawing
	//kinect space values
	private Vector3[] jointPositions = new Vector3[JointType.values().length];
	private Vector3[][] jointONB = new Vector3[JointType.values().length][];

	//head-on-facing space values
	private Quaternion[] rotatedQuaternions = new Quaternion[JointType.values().length];
	private Vector3[][] rotatedONB = new Vector3[JointType.values().length][];
	private Vector3[] rotatedPositions = new Vector3[JointType.values().length];

	//used to prevent the paint thread from accessing the above values while they're being changed by the main thread
	private Lock jointValuesLock = new ReentrantLock(); 

	public UserViewer(UserTracker tracker) {
		mTracker = tracker;
		mTracker.addNewFrameListener(this);
		for (int i = 0; i < JointType.values().length; i++){
			jointPositions[i] = new Vector3();
			jointONB[i] = new Vector3[]{ new Vector3(), new Vector3(), new Vector3() };
			rotatedQuaternions[i] = new Quaternion();
			rotatedONB[i] = new Vector3[]{ new Vector3(), new Vector3(), new Vector3() };
			rotatedPositions[i] = new Vector3();
		} 
	}

	public String getStatus(){
		return statusString;
	}
	
	public synchronized void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		if (mLastFrame != null) {
			int framePosX = 0;
			int framePosY = 0;
			
			VideoFrameRef depthFrame = mLastFrame.getDepthFrame();
			if (depthFrame != null) {
				int width = depthFrame.getWidth();
				int height = depthFrame.getHeight();
				
				//dimension changed
				if (mBufferedImage == null || mBufferedImage.getWidth() != width || mBufferedImage.getHeight() != height) 
					mBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				
				mBufferedImage.setRGB(0, 0, width, height, mDepthPixels, 0, width);
				
				framePosX = 0;//(getWidth() - width) / 2;
				framePosY = (getHeight() - height) / 2;

				g.drawImage(mBufferedImage, framePosX, framePosY, null);
			}
			
			if (skeleton != null && skeleton.getState() == SkeletonState.TRACKED){
				jointValuesLock.lock();

				//draw the skeleton both on screen, and to the right of the screen, with vertical rotation cancelled
				for (int i = 0; i < 2; i++){
					Vector3[] positions;
					Vector3[][] onbs;
					Vector3 torsoPosition;

					if (i == 0){
						positions = jointPositions;
						onbs = jointONB;
						torsoPosition = jointPositions[JointType.TORSO.toNative()];
					}

					else{
						framePosX = depthFrame.getWidth();
						positions = rotatedPositions;
						onbs = rotatedONB;
						torsoPosition = new Vector3();
					}

					drawLimb(g2, framePosX, framePosY, JointType.HEAD, JointType.NECK, torsoPosition, positions);
					
					drawLimb(g2, framePosX, framePosY, JointType.LEFT_SHOULDER, JointType.LEFT_ELBOW, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.LEFT_ELBOW, JointType.LEFT_HAND, torsoPosition, positions);

					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_SHOULDER, JointType.RIGHT_ELBOW, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_ELBOW, JointType.RIGHT_HAND, torsoPosition, positions);

					//drawLimb(g2, framePosX, framePosY, JointType.LEFT_SHOULDER, JointType.RIGHT_SHOULDER, torsoPosition, positions);

					drawLimb(g2, framePosX, framePosY, JointType.LEFT_SHOULDER, JointType.TORSO, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_SHOULDER, JointType.TORSO, torsoPosition, positions);

					drawLimb(g2, framePosX, framePosY, JointType.LEFT_HIP, JointType.TORSO, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_HIP, JointType.TORSO, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.LEFT_HIP, JointType.RIGHT_HIP, torsoPosition, positions);

					drawLimb(g2, framePosX, framePosY, JointType.LEFT_HIP, JointType.LEFT_KNEE, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.LEFT_KNEE, JointType.LEFT_FOOT, torsoPosition, positions);

					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_HIP, JointType.RIGHT_KNEE, torsoPosition, positions);
					drawLimb(g2, framePosX, framePosY, JointType.RIGHT_KNEE, JointType.RIGHT_FOOT, torsoPosition, positions);

					//draw the orientations too 
					for (SkeletonJoint joint : skeleton.getJoints()){
						JointType jointType = joint.getJointType();
						int jointTypeId = jointType.toNative();

						if (!limbEndTypes.contains(jointType)){
							//draw the onb
							Vector3 pos = torsoPosition.add(jointPositions[jointType.toNative()]);

							final float len = 70; //drawing length
							assert joint.getPosition() != null;

							com.primesense.nite.Point2D<Float> orig = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(pos));
							com.primesense.nite.Point2D<Float> a = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(pos.add(jointONB[jointTypeId][0].scale(len))));
							com.primesense.nite.Point2D<Float> b = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(pos.add(jointONB[jointTypeId][1].scale(len))));
							com.primesense.nite.Point2D<Float> c = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(pos.add(jointONB[jointTypeId][2].scale(len))));

							g2.setStroke(new BasicStroke(3));

							g2.setColor(new Color(0xFF0000));
							g2.draw(new Line2D.Float(framePosX + orig.getX().intValue(), framePosY + orig.getY().intValue(), framePosX + a.getX().intValue(), framePosY + a.getY().intValue()));

							g2.setColor(new Color(0x00FF00));
							g2.draw(new Line2D.Float(framePosX + orig.getX().intValue(), framePosY + orig.getY().intValue(), framePosX + b.getX().intValue(), framePosY + b.getY().intValue()));

							g2.setColor(new Color(0x0000FF));
							g2.draw(new Line2D.Float(framePosX + orig.getX().intValue(), framePosY + orig.getY().intValue(), framePosX + c.getX().intValue(), framePosY + c.getY().intValue()));
						}
					}
				}

				try{jointValuesLock.unlock();}
				catch(IllegalMonitorStateException e){e.printStackTrace();}
			}
		}
	}

	private void drawLimb(Graphics2D g2, int left, int top, JointType from, JointType to, Vector3 torsoPosition, Vector3[] jointPositions) {
		com.primesense.nite.Point2D<Float> fromPos = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(torsoPosition.add(jointPositions[from.toNative()])));
		com.primesense.nite.Point2D<Float> toPos = mTracker.convertJointCoordinatesToDepth(MathConversion.point3d(torsoPosition.add(jointPositions[from.toNative()])));

		float x1 = left + fromPos.getX().intValue();
		float y1 = top + fromPos.getY().intValue();
		float x2 = left + toPos.getX().intValue();
		float y2 = top + toPos.getY().intValue();

		g2.setColor(new Color(mColors[(targetUser.getId() + 1) % mColors.length]));
		g2.setStroke(new BasicStroke(3));//g2.setStroke(new BasicStroke(Math.min(minConfFrom, minConfTo) * 5));
		g2.draw(new Line2D.Float(x1, y1, x2, y2));
	}
	
	public synchronized void onNewFrame(UserTracker tracker) {
		if (mLastFrame != null) {
			mLastFrame.release();
			mLastFrame = null;
		}
		
		mLastFrame = mTracker.readFrame();
		
		VideoFrameRef depthFrame = mLastFrame.getDepthFrame();
		
		if (depthFrame != null) {
			ByteBuffer frameData = depthFrame.getData().order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer usersFrame = mLastFrame.getUserMap().getPixels().order(ByteOrder.LITTLE_ENDIAN);
		
			// make sure we have enough room
			if (mDepthPixels == null || mDepthPixels.length < depthFrame.getWidth() * depthFrame.getHeight()) {
				mDepthPixels = new int[depthFrame.getWidth() * depthFrame.getHeight()];
			}
		
			calcHist(frameData);
			frameData.rewind();

			int pos = 0;
			while(frameData.remaining() > 0) {
				short depth = frameData.getShort();
				short userId = usersFrame.getShort();
				short pixel = (short)mHistogram[depth];
				int color = 0xFFFFFFFF;
				if (userId > 0) {
					color = mColors[userId % mColors.length];
				}
				
				mDepthPixels[pos] = color & (0xFF000000 | (pixel << 16) | (pixel << 8) | pixel);
				pos++;
			}
		}

		//detect any new users in case they are the target user
		java.util.List<UserData> users = mLastFrame.getUsers();
		for (UserData user : users) {
			if (user.isNew()) mTracker.startSkeletonTracking(user.getId());
		}

		//reset on each frame
		targetUser = null;
		skeleton = null;
		statusString = null;

		if (users.size() == 1){
			targetUser = users.get(0);
			skeleton = targetUser.getSkeleton();
		}

		//conditions for validity throughout; can go into an error state at any point
		if (state >= STATE_CALIBRATE){ //non-error state;
			if (users.size() == 0) state = STATE_ERROR_NO_USER;
			else if (users.size() > 1) state = STATE_ERROR_TOO_MANY_USERS;
			else { //one user
				if (skeleton != null){
					for (JointType limbEndType : limbEndTypes){
						if (skeleton.getJoint(limbEndType).getPositionConfidence() < 0.1){
							//one of the limb ends is out of frame
							state = STATE_ERROR_USER_NOT_IN_FRAME;
							break;
						}
					}
				}
			}
		}

		//only do this is we really have a skeleton that's being tracked
		if (state >= STATE_BEGIN){
			//this is not really needed
			jointValuesLock.lock();

			//this joint holds the orientation of the body as a whole
			SkeletonJoint torso = skeleton.getJoint(JointType.TORSO);

			//get the position and orientation in geomlib types
			Quaternion torsoQuaternion = MathConversion.quaternion(torso.getOrientation());
			Vector3 torsoPosition = MathConversion.vector3(torso.getPosition());

			//get the vertical axis rotation and cancel it for every joint
			Matrix3 torsoRotationMatrix = new Matrix3(torsoQuaternion);
			//MathConversion.onbFromQuaternion(torso.getOrientation(), ta, tb, tc);

			//get a "right" vector from the cross product of the up vector and the forward vector
			Vector3 forwardDirection = torsoRotationMatrix.getColumn(0); //TODO: find the right row that points forward when stading upright
			Vector3 rightDirection = forwardDirection.cross(upVector);
			rightDirection.normalize();

			//get the rotation about the up vector 
			double verticalRotation = Math.atan2(rightDirection.getZ(), rightDirection.getX());

			//create a reverse vertical rotation quaternion and rotation matrix
			Quaternion reverseVerticalRotation = new Quaternion(upVector, -verticalRotation);
			Matrix3 reverseVerticalRotationMatrix = new Matrix3(reverseVerticalRotation);

			//multiply each joint quaternion by $reverseVerticalRotation,
			//which will make it seem as if the subject is always facing the camera directly.
			//this simplifies the pose matching process greatly

			//NOTE: a way to baypass some of the rigmarole below: get the joint vector by subtracting the position of the joint connected to the joint
			//doesn't work on the head, though, so might as well
			for (SkeletonJoint joint : skeleton.getJoints()) {
				int jointTypeId = joint.getJointType().toNative();
				
				//take the joint position and rotate it about the up vector
				Vector3 jointPosition = MathConversion.vector3(joint.getPosition());

				//translate it to the origin
				Vector3 diff = jointPosition.sub(torsoPosition);
				jointPositions[jointTypeId] = diff;//jointPosition;

				//rotate it
				Vector3 rotated = diff.multiply(reverseVerticalRotationMatrix);

				//just store it as it is				//translate it back, and it's in body space
				rotatedPositions[jointTypeId] = rotated;//.add(torsoPosition);

				//hand and foot bones don't have orientation (but the head does)
				if (!limbEndTypes.contains(joint.getJointType())){
					Quaternion jointOrientation = MathConversion.quaternion(joint.getOrientation());
					Matrix3 originalMatrix = new Matrix3(jointOrientation);
					jointONB[jointTypeId][0] = originalMatrix.getColumn(0);
					jointONB[jointTypeId][1] = originalMatrix.getColumn(1);
					jointONB[jointTypeId][2] = originalMatrix.getColumn(2);

					rotatedQuaternions[jointTypeId] = reverseVerticalRotation.multiply(jointOrientation);
					Matrix3 rotatedMatrix = new Matrix3(rotatedQuaternions[jointTypeId]);

					rotatedONB[jointTypeId][0] = rotatedMatrix.getColumn(0);
					rotatedONB[jointTypeId][1] = rotatedMatrix.getColumn(1);
					rotatedONB[jointTypeId][2] = rotatedMatrix.getColumn(2);
				}
			}

			try{jointValuesLock.unlock();}
			catch(IllegalMonitorStateException e){e.printStackTrace();}
		}
		

		switch(state){
			//error states
			case STATE_ERROR_NO_USER: 
				if (users.size() > 0) state = STATE_CALIBRATE;
				break;

			case STATE_ERROR_TOO_MANY_USERS:
				if (users.size() == 1) state = STATE_CALIBRATE;
				break;

			case STATE_ERROR_USER_NOT_IN_FRAME:
				if (skeleton != null){
					boolean solved = true;
					for (JointType limbEndType : limbEndTypes){
						if (skeleton.getJoint(limbEndType).getPositionConfidence() < 0.1){
							//one of the limbs is still out of frame
							solved = false;
							break;
						}
					}
					if (solved) state = STATE_CALIBRATE;
				}
				break;

			//progress states
			case STATE_CALIBRATE: 
				assert targetUser != null;
				if (skeleton != null){
					SkeletonState skelState = skeleton.getState();

					if (skelState == SkeletonState.TRACKED) state = STATE_BEGIN;
					else if (skelState == SkeletonState.NONE){
						//not sure what this means
						System.out.println("skelState is NONE");
					}
					else if (skelState == SkeletonState.CALIBRATION_ERROR_NOT_IN_POSE){
						//not sure about this one either
						System.out.println("skelState is CALIBRATION_ERROR_NOT_IN_POSE");
					}
					else{
						//some kind of error, with a hint for the user as to how to fix
						if (skelState == SkeletonState.CALIBRATION_ERROR_HANDS) statusString = "error calibrating hands";
						else if (skelState == SkeletonState.CALIBRATION_ERROR_HEAD) statusString = "error calibrating head";
						else if (skelState == SkeletonState.CALIBRATION_ERROR_LEGS) statusString = "error calibrating legs";
						else if (skelState == SkeletonState.CALIBRATION_ERROR_TORSO) statusString = "error calibrating torso";
					}
				}

				break;

			//TODO: pose stuff here
			case STATE_BEGIN: break;
			case STATE_BEND: break;
			case STATE_JUMP: break;
			case STATE_FINISHED: break;
		}

		if (statusString == null) statusString = stateNames[state];
		if (state != lastState){
			System.out.println("entered state: " + statusString);
		}

		lastState = state;
		repaint();
	}

	/**
	 * Maps the 0 .. 65535 possible depth values to 0 .. 255 grayscale values, expanding the most common range of depths
	 * Similar to high dynamic range
	*/
	private void calcHist(ByteBuffer depthBuffer) {
		// make sure we have enough room
		if (mHistogram == null) {
			mHistogram = new float[0x10000];
		}
		
		// reset
		for (int i = 0; i < mHistogram.length; ++i)
			mHistogram[i] = 0;

		int points = 0;
		while (depthBuffer.remaining() > 0) {
			int depth = (int)depthBuffer.getShort() & 0xFFFF;
			if (depth != 0) {
				mHistogram[depth]++;
				points++;
			}
		}

		for (int i = 1; i < mHistogram.length; i++) {
			mHistogram[i] += mHistogram[i - 1];
		}

		if (points > 0) {
			for (int i = 1; i < mHistogram.length; i++) {
				mHistogram[i] = (int) (256 * (1.0f - (mHistogram[i] / (float) points)));
			}
		}
	}
	
	/**
	*@return: string containing quaternions of all the joints if a skeleton is detected. 
	**/
	/*synchronized String getQuaternionsToString(){
		String strQuaternion = "";
		
		if(mLastFrame!=null){
			for(UserData user : mLastFrame.getUsers()){
				if (user.getSkeleton().getState() == SkeletonState.TRACKED) {
					for(SkeletonJoint joint : user.getSkeleton().getJoints()){
						Quaternion orientation = joint.getOrientation();
						strQuaternion += joint.getJointType() + ": " + "W - " +orientation.getW()+ "; X - " +orientation.getX()+ "; Y - " +orientation.getY()+ "; Z - " +orientation.getZ()+ ";\n";
					}
				}
				else{
					strQuaternion = "Skeleton not found.";
				}
			}
		}
		else
			strQuaternion = "Last frame is null.";
		
		return strQuaternion;
	}*/
	
	/**
	*@return: string containing the normal vector and point on the floor plane as well as confidence.
	**/
	/*synchronized String getFloorPlaneToString(){
		String strFloor = "";
		
		if(mLastFrame!=null){
			strFloor ="Floor(normal vector): (" + mLastFrame.getPlane().getNormal().getX() + "," + mLastFrame.getPlane().getNormal().getY() + 
				"," + mLastFrame.getPlane().getNormal().getZ()+")" + 
				"\nFloor(point): (" + mLastFrame.getPlane().getPoint().getX() +","+ mLastFrame.getPlane().getPoint().getY() +","+ mLastFrame.getPlane().getPoint().getZ() + ")" + 
				"\n" +"Confidence: " + mLastFrame.getFloorConfidence();
		}
		
		return strFloor;
	}*/
}


