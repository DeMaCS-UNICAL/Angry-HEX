package ab.utils;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.Vision;

public class ABUtil {
	
	public static int gap = 5; //vision tolerance.
	private static TrajectoryPlanner tp = new TrajectoryPlanner();

	// If o1 supports o2, return true
	public static boolean isSupport(ABObject o2, ABObject o1)
	{
		if(o2.x == o1.x && o2.y == o1.y && o2.width == o1.width && o2.height == o1.height)
				return false;
		
		int ex_o1 = o1.x + o1.width;
		int ex_o2 = o2.x + o2.width;
		
		int ey_o2 = o2.y + o2.height;
		if(
			(Math.abs(ey_o2 - o1.y) < gap)
			&&  
 			!( o2.x - ex_o1  > gap || o1.x - ex_o2 > gap )
		  )
	        return true;	
		
		return false;
	}
	//Return a link list of ABObjects that support o1 (test by isSupport function ). 
	//objs refers to a list of potential supporters.
	//Empty list will be returned if no such supporters. 
	public static List<ABObject> getSupporters(ABObject o2, List<ABObject> objs)
			{
				List<ABObject> result = new LinkedList<ABObject>();
				//Loop through the potential supporters
		        for(ABObject o1: objs)
		        {
		        	if(isSupport(o2,o1))
		        		result.add(o1);
		        }
		        return result;
			}

	//Return true if the target can be hit by releasing the bird at the specified release point
	public static boolean isReachable(Vision vision, Point target, Shot shot)
	{ 
		//test whether the trajectory can pass the target without considering obstructions
		Point releasePoint = new Point(shot.getX() + shot.getDx(), shot.getY() + shot.getDy()); 
		int traY = tp.getYCoordinate(vision.findSlingshotMBR(), releasePoint, target.x);
		if (Math.abs(traY - target.y) > 100)
		{	
			//System.out.println(Math.abs(traY - target.y));
			return false;
		}
		boolean result = true;
		List<Point> points = tp.predictTrajectory(vision.findSlingshotMBR(), releasePoint);		
		for(Point point: points)
		{
		  if(point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400)
			for(ABObject ab: vision.findBlocksMBR())
			{
				if( 
						((ab.contains(point) && !ab.contains(target))||Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 72 ) < 10) 
						&& point.x < target.x
						)
					return false;
			}
		  
		}
		return result;
	}
	

}
