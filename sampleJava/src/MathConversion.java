//to avoid path conflicts, geomlibs are imported, while nite types are referenced by full path
import de.yvert.geometry.*;

/**
 * This class onverts nite Quaternion and Point2/3D<Float> 
 * to the geomlib Vector2/3 and Quaternion.
 * It also converts goemlib Vector3 back to Point3D
*/
public class MathConversion{
	public static Quaternion quaternion(com.primesense.nite.Quaternion from){
		return new Quaternion(from.getW(), from.getX(), from.getY(), from.getZ());
	}

	public static Vector2 vector2(com.primesense.nite.Point2D<Float> from){
		return new Vector2(from.getX(), from.getY());
	}

	public static Vector3 vector3(com.primesense.nite.Point3D<Float> from){
		return new Vector3(from.getX(), from.getY(), from.getZ());
	}

	//this makes use of PublicPoint3D (an addition by yours truly), because Point3D has no public constructor
	public static com.primesense.nite.Point3D<Float> point3d(Vector3 from){
		return new com.primesense.nite.PublicPoint3D<Float>((float)from.getX(), (float)from.getY(), (float)from.getZ());
	}

	//http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/index.htm
	//could be using the Matrix3.fromQuaternion, but this already works as it is
	public static void onbFromQuaternion(com.primesense.nite.Quaternion quat, Vector3 a, Vector3 b, Vector3 c){
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

		a.set(ax, ay, az);
		b.set(bx, by, bz);
		c.set(cx, cy, cz);

		a.normalize();
		b.normalize();
		c.normalize();
	}
}