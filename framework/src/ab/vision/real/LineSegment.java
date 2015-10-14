/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class LineSegment {
    
    public static int NO_CHANGE = 0;
    public static int CLOCKWISE = 1;
    public static int ANTICLOCKWISE = -1;
    
    private static double THRESHOLD1 = Math.toRadians(20);
    private static double JOIN_THRESHOLD = Math.toRadians(5);
    private static double JOIN_THRESHOLD2 = 40;//40;
    
    // angle tracking parameters
    private double prevAngle;
    private double accumChange;
    private int dirChange;
    
    public Point _start = null;
    public Point _end = null;
    public Point _prevEnd = null;

    public LineSegment(Point start, double angle)
    {
        // initialise start and end points
        _start = start;
        _end = start;
        _prevEnd = start;
        
        // tracking parameters
        prevAngle = angle;
        accumChange = 0;
        dirChange = NO_CHANGE;
    }
    
    
    public double addPoint(Point p, double angle, double THRESHOLD2)
    {
        if (angle != ConnectedComponent.ANGLE_UNDEFINED)
        {
            // test point is not a corner
            double diff = angleDiff(angle, prevAngle);
            
            // if the sign of angle change is reversed
            if (sign(diff) != dirChange)
            {
                // corner detected
                if (accumChange >= THRESHOLD1)
                {
                    return angleDiff(angle, approximateAngle());
                }
                
                // reset cummulated results
                if (diff != 0)
                {
                    accumChange = Math.abs(diff);
                    dirChange = sign(diff);
                }
            }
            else
            {
                accumChange += Math.abs(diff);
                if (accumChange >= THRESHOLD2)
                    return angleDiff(angle, approximateAngle());
            }
            prevAngle = angle;
        }
        _prevEnd = _end;
        _end = p;
        
        return 0;
    }
    
    /* draw start point of the line onto canvas */
    public void draw(Graphics2D g, int left, int top)
    {        
        g.setColor(Color.CYAN);
        g.fillOval(_start.x + left - 2, _start.y + top - 2, 4, 4);
        //g.fillOval(_end.x + left - 1, _end.y + top - 1, 2, 2);
    }
    
    public double approximateAngle()
    {        
        double xDiff = _start.x - _end.x;
        double yDiff = _start.y - _end.y;
        
        if (xDiff == 0)
            return Math.PI / 2;
        else
        {
            if (Math.atan(yDiff / xDiff) < 0)
                return Math.atan(yDiff / xDiff) + Math.PI;
            else
                return Math.atan(yDiff / xDiff);
        }
    }
    
    public boolean join(LineSegment line)
    {
        double angle = approximateAngle();
        double angle2 = line.approximateAngle();
        double diff = Math.abs(angleDiff(angle, angle2));
        double minD = Math.min(distance(_start, _end), distance(line._start, line._end));
        if (diff < JOIN_THRESHOLD ||
            minD * 2 + Math.toDegrees(diff) < JOIN_THRESHOLD2)
        {
            _end = line._end;
            return true;
        }
        
        return false;
    }
    
    /* calculate angle difference a - b
     * @param   a, b - angles (radians) in range [0, PI)
     * @return  difference a - b (positive for clockwise)
     */
    public static double angleDiff(double a, double b)
    {
        double diff = a - b;
        
        if (diff < - Math.PI / 2)
            diff += Math.PI;
        else if (diff > Math.PI / 2)
            diff -= Math.PI;
            
        return diff;
    }
    
    /* convert the angle a to a representation which is close 
     * to the target by adding/subtracting PI
     *  - e.g  closeAngle(179, 0) = -1
     */
    public static double closeAngle(double a, double target)
    {
        return target + angleDiff(a, target);
    }
    
    
    /* return direction of the angle change,
       clockwise for positive changes */
    public static int sign(double diff)
    {
        if (diff > 0)
            return CLOCKWISE;
        else if (diff < 0)
            return ANTICLOCKWISE;
        else
            return NO_CHANGE;
    }
    
    public static double distance(Point a, Point b)
    {
        int x = a.x - b.x;
        int y = a.y - b.y;
        return Math.sqrt(x*x + y*y);
    }
    
    public void removeEndPoint()
    {
        _end = _prevEnd;
    }
}
