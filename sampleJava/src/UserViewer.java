import java.util.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.*;

/*import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;
import com.primesense.nite.Skeleton;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.JointType;
import com.primesense.nite.Quaternion;
import com.primesense.nite.PublicPoint3D; //extra class to add a public contructor to Point3D
import com.primesense.nite.Point3D;
import com.primesense.nite.Point2D;*/
import com.primesense.nite.*;

import org.openni.VideoFrameRef;

public class UserViewer extends Component implements UserTracker.NewFrameListener {
	float mHistogram[];
	int[] mDepthPixels;
	UserTracker mTracker;
	UserTrackerFrameRef mLastFrame;
	BufferedImage mBufferedImage;
	int[] mColors;

	//error states
	final static int STATE_ERROR_NO_USER = 0; //there must be one user only
	final static int STATE_ERROR_TOO_MANY_USERS = 1; //there must be one user only
	final static int STATE_ERROR_USER_NOT_IN_FRAME = 2; //both head and feet must both be in frame

	final static int STATE_CALIBRATE = 3; //wait for a few seconds to calibrate
	final static int STATE_BEGIN = 4; //first pose
	final static int STATE_BEND = 5; //bending down in preparation to jump
	final static int STATE_JUMP = 6; //mid air
	final static int STATE_FINISHED = 7; //successful test

	final static String[] stateNames = new String[]{"no user", "more than one user", "user not in frame", "calibrating", "begin", "bend", "jump", "finish"};
	private String statusString = null;

	int state = STATE_CALIBRATE; //initial state
	int lastState = state;

	public UserViewer(UserTracker tracker) {
		mTracker = tracker;
		mTracker.addNewFrameListener(this);
		mColors = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF };
	}
	
	public synchronized void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		if (mLastFrame == null) {
			return;
		}
		
		int framePosX = 0;
		int framePosY = 0;
		
		VideoFrameRef depthFrame = mLastFrame.getDepthFrame();
		if (depthFrame != null) {
			int width = depthFrame.getWidth();
			int height = depthFrame.getHeight();
			
			// make sure we have enough room
			if (mBufferedImage == null || mBufferedImage.getWidth() != width || mBufferedImage.getHeight() != height) {
				mBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			}
			
			mBufferedImage.setRGB(0, 0, width, height, mDepthPixels, 0, width);
			
			framePosX = (getWidth() - width) / 2;
			framePosY = (getHeight() - height) / 2;

			g.drawImage(mBufferedImage, framePosX, framePosY, null);
		}
		
		for (UserData user : mLastFrame.getUsers()) {
			if (user.getSkeleton().getState() == SkeletonState.TRACKED) {
				drawLimb(g2, framePosX, framePosY, user, JointType.HEAD, JointType.NECK);
				
				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_SHOULDER, JointType.LEFT_ELBOW);
				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_ELBOW, JointType.LEFT_HAND);

				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_SHOULDER, JointType.RIGHT_ELBOW);
				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_ELBOW, JointType.RIGHT_HAND);

				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_SHOULDER, JointType.RIGHT_SHOULDER);

				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_SHOULDER, JointType.TORSO);
				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_SHOULDER, JointType.TORSO);

				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_HIP, JointType.TORSO);
				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_HIP, JointType.TORSO);
				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_HIP, JointType.RIGHT_HIP);

				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_HIP, JointType.LEFT_KNEE);
				drawLimb(g2, framePosX, framePosY, user, JointType.LEFT_KNEE, JointType.LEFT_FOOT);

				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_HIP, JointType.RIGHT_KNEE);
				drawLimb(g2, framePosX, framePosY, user, JointType.RIGHT_KNEE, JointType.RIGHT_FOOT);

				//draw each orthonormal basis
				for (SkeletonJoint joint : user.getSkeleton().getJoints()){
					if (joint.getOrientationConfidence() > 0.5 && joint.getPositionConfidence() > 0.5){
						float[] matrix = quatToMatrix(joint.getOrientation());
						Point3D<Float> center = joint.getPosition();
						final float len = 10;
						Point3D<Float> dirA = new PublicPoint3D<Float>(center.getX() + matrix[0] * len, center.getY() + matrix[1] * len, center.getZ() + matrix[2] * len);
						Point3D<Float> dirB = new PublicPoint3D<Float>(center.getX() + matrix[3] * len, center.getY() + matrix[4] * len, center.getZ() + matrix[5] * len);
						Point3D<Float> dirC = new PublicPoint3D<Float>(center.getX() + matrix[6] * len, center.getY() + matrix[7] * len, center.getZ() + matrix[8] * len);

						//System.out.println(dirA.getX() + ", " + dirA.getY() + ", " + dirA.getZ());
						System.out.println(matrix[0] + ", " + matrix[1] + ", " + matrix[2]);

						Point2D<Float> orig = mTracker.convertJointCoordinatesToDepth(center);
						Point2D<Float> a = mTracker.convertJointCoordinatesToDepth(dirA);
						Point2D<Float> b = mTracker.convertJointCoordinatesToDepth(dirB);
						Point2D<Float> c = mTracker.convertJointCoordinatesToDepth(dirC);

						g2.setStroke(new BasicStroke(3));

						g2.setColor(new Color(0xFF0000));
						g2.draw(new Line2D.Float(orig.getX().intValue(), a.getX().intValue(), orig.getY().intValue(), a.getY().intValue()));

						g2.setColor(new Color(0x00FF00));
						g2.draw(new Line2D.Float(orig.getX().intValue(), b.getX().intValue(), orig.getY().intValue(), b.getY().intValue()));

						g2.setColor(new Color(0x0000FF));
						g2.draw(new Line2D.Float(orig.getX().intValue(), c.getX().intValue(), orig.getY().intValue(), c.getY().intValue()));
					}
				}
			}
		}
	}

	private void drawLimb(Graphics2D g2, int x, int y, UserData user, JointType from, JointType to) {
		SkeletonJoint fromJoint = user.getSkeleton().getJoint(from);
		SkeletonJoint toJoint = user.getSkeleton().getJoint(to);
		
		if (fromJoint.getPositionConfidence() == 0.0 || toJoint.getPositionConfidence() == 0.0) {
			return;
		}
		
		Point2D<Float> fromPos = mTracker.convertJointCoordinatesToDepth(fromJoint.getPosition());
		Point2D<Float> toPos = mTracker.convertJointCoordinatesToDepth(toJoint.getPosition());

		/*// draw it in another color than the use color
		g.setColor(new Color(mColors[(user.getId() + 1) % mColors.length]));
		g.drawLine(x + fromPos.getX().intValue(), y + fromPos.getY().intValue(), x + toPos.getX().intValue(), y + toPos.getY().intValue());*/
	
		float minConfFrom = Math.min(fromJoint.getPositionConfidence(), fromJoint.getOrientationConfidence());
		float minConfTo = Math.min(toJoint.getPositionConfidence(), toJoint.getOrientationConfidence());

		float x1 = x + fromPos.getX().intValue();
		float y1 = y + fromPos.getY().intValue();
		float x2 = x + toPos.getX().intValue();
		float y2 = y + toPos.getY().intValue();

		g2.setStroke(new BasicStroke(Math.min(minConfFrom, minConfTo) * 5));
		g2.draw(new Line2D.Float(x1, y1, x2, y2));

		//also draw ONBs

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

		java.util.List<UserData> users = mLastFrame.getUsers();

		// check if any new user detected
		for (UserData user : users) {
			if (user.isNew()) {
				// start skeleton tracking
				mTracker.startSkeletonTracking(user.getId());
			}
		}

		UserData targetUser = null;
		Skeleton skeleton = null;
		statusString = null; //reset

		if (users.size() == 1){
			targetUser = users.get(0);
			skeleton = targetUser.getSkeleton();
		} 

		//conditions for validity throughout; can go into an error state at any point
		if (state >= STATE_CALIBRATE){ //non-error state;
			if (users.size() == 0) state = STATE_ERROR_NO_USER;
			else if (users.size() > 1) state = STATE_ERROR_TOO_MANY_USERS;
			else { //one user
				if (skeleton.getJoint(JointType.HEAD).getPositionConfidence() < 0.1 && 
				    skeleton.getJoint(JointType.LEFT_FOOT).getPositionConfidence() < 0.1 &&
				    skeleton.getJoint(JointType.RIGHT_FOOT).getPositionConfidence() < 0.1) state = STATE_ERROR_USER_NOT_IN_FRAME;
			}
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
					if (skeleton.getJoint(JointType.HEAD).getPositionConfidence() > 0.1 && 
						skeleton.getJoint(JointType.LEFT_FOOT).getPositionConfidence() > 0.1 &&
						skeleton.getJoint(JointType.RIGHT_FOOT).getPositionConfidence() > 0.1){
							state = STATE_CALIBRATE;
					}
				}	
				break;

			//progress states
			case STATE_CALIBRATE: 
				assert targetUser != null;
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

				break;

			//TODO: stuff here
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
			int depth = depthBuffer.getShort() & 0xFFFF;
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
	synchronized String getQuaternionsToString(){
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
		}else
			strQuaternion = "Last frame is null.";
		
		return strQuaternion;
	}
	
	/**
	*@return: string containing the normal vector and point on the floor plane as well as confidence.
	**/
	synchronized String getFloorPlaneToString(){
		String strFloor = "";
		
		if(mLastFrame!=null){
			strFloor ="Floor(normal vector): (" + mLastFrame.getPlane().getNormal().getX() + "," + mLastFrame.getPlane().getNormal().getY() + 
				"," + mLastFrame.getPlane().getNormal().getZ()+")" + 
				"\nFloor(point): (" + mLastFrame.getPlane().getPoint().getX() +","+ mLastFrame.getPlane().getPoint().getY() +","+ mLastFrame.getPlane().getPoint().getZ() + ")" + 
				"\n" +"Confidence: " + mLastFrame.getFloorConfidence();
		}
		
		return strFloor;
	}

	//http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/index.htm
	static float[] quatToMatrix(Quaternion quat){
		float sqw = quat.getW() * quat.getW();
		float sqx = quat.getX() * quat.getX();
		float sqy = quat.getY() * quat.getY();
		float sqz = quat.getZ() * quat.getZ();

		float ax = ( sqx - sqy - sqz + sqw); // since sqw + sqx + sqy + sqz =1/invs
		float by = (-sqx + sqy - sqz + sqw);
		float cz = (-sqx - sqy + sqz + sqw);

		float tmp1 = quat.getX() * quat.getY();
		float tmp2 = quat.getZ() * quat.getW();


		float ay = 2.0f * (tmp1 + tmp2);
		float bx = 2.0f * (tmp1 - tmp2);

		tmp1 = quat.getX() * quat.getZ();
		tmp2 = quat.getY() * quat.getW();

		float az = 2.0f * (tmp1 - tmp2);
		float cx = 2.0f * (tmp1 + tmp2);

		tmp1 = quat.getY() * quat.getZ();
		tmp2 = quat.getX() * quat.getW();

		float bz = 2.0f * (tmp1 + tmp2);
		float cy = 2.0f * (tmp1 - tmp2);


		/*float bx = 2.0f * (tmp1 + tmp2);
		float ay = 2.0f * (tmp1 - tmp2);

		tmp1 = quat.getX() * quat.getZ();
		tmp2 = quat.getY() * quat.getW();

		float cx = 2.0f * (tmp1 - tmp2);
		float az = 2.0f * (tmp1 + tmp2);

		tmp1 = quat.getY() * quat.getZ();
		tmp2 = quat.getX() * quat.getW();

		float cy = 2.0f * (tmp1 + tmp2);
		float bz = 2.0f * (tmp1 - tmp2);*/

		float la = (float)Math.sqrt(ax * ax + ay * ay + az * az);
		float lb = (float)Math.sqrt(bx * bx + by * by + bz * bz);
		float lc = (float)Math.sqrt(cx * cx + cy * cy + cz * cz);

		return new float[]{ ax/la, ay/la, az/la, 
		                    bx/lb, by/lb, bz/lb, 
		                    cx/lc, cy/lc, cz/lc };
		//return new float[]{ ax/la, bx/lb, cx/lc, ay/la, by/lb, cy/lc, az/la, bz/lb, cz/lc };
		//return new Point3D<Float>[]{ new Point3D<Float>(ax, ay, az), new Point3D<Float>(bx, by, bz), new Point3D<Float>(cx, cy, cz) };
	}
}


