/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014,  XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
** Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/
/*
 * Modified by Angry-HEX Team
 * Removed unused "import" and variables
 */
/*******************************************************************************
 * Angry-HEX - an artificial player for Angry Birds based on declarative knowledge bases
 * Copyright (C) 2012-2015 Francesco Calimeri, Michael Fink, Stefano Germano, Andreas Humenberger, Giovambattista Ianni, Christoph Redl, Daria Stepanova, Andrea Tucci, Anton Wimmer.
 *
 * This file is part of Angry-HEX.
 *
 * Angry-HEX is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Angry-HEX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ab.planner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import angryhexclient.Configuration;

/* TrajectoryPlanner ------------------------------------------------------ */
public class TrajectoryPlanner {


    private static double X_OFFSET = 0.5;
    private static double Y_OFFSET = 0.65;
    private static double BOUND = 0.1;
    private static double STRETCH = 0.4;
    private static int X_MAX = 640;
    
    //                                         10,    15,    20,    25,    30,    35,    40,    45,    50,    55,    60,    65,    70
    private static double _launchAngle [] =   {0.13,  0.215, 0.296, 0.381, 0.476, 0.567, 0.657, 0.741, 0.832, 0.924, 1.014, 1.106, 1.197};
    private static double _changeAngle [] =   {0.052, 0.057, 0.063, 0.066, 0.056, 0.054, 0.050, 0.053, 0.042, 0.038, 0.034, 0.029, 0.025};
    private static double _launchVelocity[] = {2.9,   2.88,  2.866, 2.838, 2.810, 2.800, 2.790, 2.773, 2.763, 2.745, 2.74, 2.735, 2.73};
    
    // small modification to the scale and angle
    private double _scaleFactor = 1.005;
  
	
//    private double _angleAdjust = 0.0;
    

    // conversion between the trajectory time and actual time in milliseconds
    private double _timeUnit = 815;
    
    // boolean flag on set trajectory
    private boolean _trajSet = false;
    
    // parameters of the set trajectory
    private Point _release;
    private double _theta, _velocity, _ux, _uy, _a, _b;
    
    // the trajectory points
    ArrayList<Point> _trajectory;
    
    // reference point and current scale
    private Point _ref;
    private double _scale;

    // create a trajectory planner object
    public TrajectoryPlanner() {
    
    }

    /* Fit a quadratic to the given points and adjust parameters for the current scene
     * namely the scale factor, which is a number used to find the correct exit velocity
     *
     * @param   pts - list of points (on screen) the trajectory passed through
     *          slingshot - bounding box of the slingshot
     *          releasePoint - where the mouse click was released from
     */
    public void adjustTrajectory(final List<Point> pts, Rectangle slingshot, Point releasePoint)
    {
        double Sx2 = 0.0;
        double Sx3 = 0.0;
        double Sx4 = 0.0;
        double Syx = 0.0;
        double Syx2 = 0.0;

        // find scene scale and reference point
        double sceneScale = getSceneScale(slingshot);
        Point refPoint = getReferencePoint(slingshot);
        
        for (Point p : pts) {                
            // normalise the points
            double x = (p.x - refPoint.x) / sceneScale;
            double y = (p.y - refPoint.y) / sceneScale;
            
            final double x2 = x * x;
            Sx2 += x2;
            Sx3 += x * x2;
            Sx4 += x2 * x2;
            Syx += y * x;
            Syx2 += y * x2;
        }

        final double a = (Syx2 * Sx2 - Syx * Sx3) / (Sx4 * Sx2 - Sx3 * Sx3);
        final double b = (Syx - a * Sx3) / Sx2;
        
        //System.out.println("a: " + a + " b: " + b);
        
        // launch angle
        double theta = -Math.atan2(refPoint.y - releasePoint.y, refPoint.x - releasePoint.x);
//        // actual angle
//        double theta2 = -Math.atan2(b, 1);

       
        //System.out.println("angles: " + theta + ", " + theta2);
        
       
        // find initial velocity
        double ux = Math.sqrt(0.5/a);
        double uy = ux * b;
        
        //System.out.println("velocity: " + Math.sqrt(ux*ux+uy*uy));
        
        // adjust the scale and angle change
        adjustScale(Math.sqrt(ux*ux + uy*uy), theta);
        System.out.println("\nscale factor changed to: " + _scaleFactor);
        _trajSet = false;
    }
    
    /* Calculate the y-coordinate of a point on the set trajectory
     *
     * @param   sling - bounding rectangle of the slingshot
     *          releasePoint - point the mouse click is released from
     *          x - x-coordinate (on screen) of the requested point
     * @return  y-coordinate (on screen) of the requested point
     */
    public int getYCoordinate(Rectangle sling, Point releasePoint, int x)
    {
        setTrajectory(sling, releasePoint);
        
        // find the normalised coordinates
        double xn = (x - _ref.x) / _scale;
        
        return _ref.y - (int)((_a * xn * xn + _b * xn) * _scale);
    }
    
	/*
	 * Modified by Angry-HEX Team
	 * Added method "fitAngle"
	 */
    private double fitAngle(double start, double end, double x, double y)
    {
    	double retAngle = 0;
    	double bestError = 100000;
    	//System.out.println("ST:"+start+" END:"+end);
        for (double theta = start; theta <= end; theta += 0.0005)
        {
            double velocity = getVelocity(theta);
            
            // initial velocities
            double u_x = velocity * Math.cos(theta);
            double u_y = velocity * Math.sin(theta);
            
            // the normalised coefficients
            double a = -0.5 / (u_x * u_x);
            double b = u_y / u_x;
            
            // the error in y-coordinate
            double error = Math.abs(a*x*x + b*x - y);
            if (error < bestError)
            {
                retAngle = theta;
                bestError = error;
            }
        }
        return retAngle;
    }
    
    /* Estimate launch points given a desired target point using maximum velocity
     * If there are two launch point for the target, they are both returned in
     * the list {lower point, higher point)
     * Note - angles greater than 75 are not considered
     *
     * @param   slingshot - bounding rectangle of the slingshot
     *          targetPoint - coordinates of the target to hit
     * @return  A list containing 2 possible release points
     */
    public ArrayList<Point> estimateLaunchPoint(Rectangle slingshot, Point targetPoint) {
        
        // calculate relative position of the target (normalised)
        double scale = getSceneScale(slingshot);
        //System.out.println("scale " + scale);
        Point ref = getReferencePoint(slingshot);
            
        double x = (targetPoint.x - ref.x) / scale;
        double y = -(targetPoint.y - ref.y) / scale;
        // System.out.println("X:"+y+" Y:"+y);   
        
		//commented out because it's not used
//        double bestError = 1000;
        double theta1 = 0;
        double theta2 = 0;
        
        // first estimate launch angle using the projectile equation (constant velocity)
        double v = _scaleFactor * _launchVelocity[6];
        double v2 = v * v;
        double v4 = v2 * v2;
    	/*
    	 * Modified by Angry-HEX Team
    	 * Added Math.abs
    	 */
        double tangent1 = (v2 - Math.sqrt(Math.abs(v4 - (x * x + 2 * y * v2)))) / x;
        double tangent2 = (v2 + Math.sqrt(Math.abs(v4 - (x * x + 2 * y * v2)))) / x;
        
        //System.out.println("TANGENTS: "+tangent1+" "+tangent2);
        
        double t1 = actualToLaunch(Math.atan(tangent1));
        double t2 = actualToLaunch(Math.atan(tangent2));
        
    	/*
    	 * Modified by Angry-HEX Team
    	 * Moved code to fitAngle method
    	 * Added call to fitAngle method
    	 */
        theta1 = fitAngle(t1-BOUND,t1+BOUND,x,y);
        theta2 = fitAngle(t2-BOUND,t2+BOUND,x,y);
        //System.out.println("FORMER THETAS: "+Math.toDegrees(theta1)+" "+Math.toDegrees(theta2));
        
        theta1 = actualToLaunch(theta1);
        theta2 = actualToLaunch(theta2);
        
        //System.out.println("NEW THETAS: " + Math.toDegrees(theta1) + ", " + Math.toDegrees(theta2));
            
        // add launch points to the list
        ArrayList<Point> pts = new ArrayList<Point>();
        pts.add(findReleasePoint(slingshot, theta1));
        
        // add the higher point if it is below 75 degrees and not same as first
        if (theta2 < Math.toRadians(75) && theta2 != theta1)
            pts.add(findReleasePoint(slingshot, theta2));
        
        return pts;
    }
   
       
    /* the estimated tap time given the tap point
     *
     * @param   sling - bounding box of the slingshot
     *          release - point the mouse clicked was released from
     *          tapPoint - point the tap should be made
     * @return  tap time (relative to the release time) in milli-seconds
     */
	/*
	 * Modified by Angry-HEX Team
	 * Changed modifier to public
	 */
    public int getTimeByDistance(Rectangle sling, Point release, Point tapPoint)
    {
        // update trajectory parameters
        setTrajectory(sling, release);
        
        double pullback = _scale * STRETCH * Math.cos(_theta);
        double distance = (tapPoint.x - _ref.x + pullback) / _scale;
        
        //System.out.println("distance " + distance);
        //System.out.println("velocity " + _ux);
        
        return (int)(distance / _ux * _timeUnit);
    }
 
   
    /* Choose a trajectory by specifying the sling location and release point
     * Derive all related parameters (angle, velocity, equation of the parabola, etc)
     *
     * @param   sling - bounding rectangle of the slingshot
     *          releasePoint - point where the mouse click was released from
     */
    public void setTrajectory(Rectangle sling, Point releasePoint)
    {
        // don't update parameters if the ref point and release point are the same
        if (_trajSet &&
            _ref != null && _ref.equals(getReferencePoint(sling)) &&
            _release != null && _release.equals(releasePoint))
            return;
            
        // set the scene parameters
        _scale = sling.height + sling.width;
        _ref = getReferencePoint(sling);
        
        // set parameters for the trajectory
        _release = new Point(releasePoint.x, releasePoint.y);
        
        // find the launch angle
        _theta = Math.atan2(_release.y - _ref.y, _ref.x - _release.x);
        _theta = launchToActual(_theta);
        
        // work out initial velocities and coefficients of the parabola
        _velocity = getVelocity(_theta);
        _ux = _velocity * Math.cos(_theta);
        _uy = _velocity * Math.sin(_theta);
        _a = -0.5 / (_ux * _ux);
        _b = _uy / _ux;
        
        
        // work out points of the trajectory
	    _trajectory = new ArrayList<Point>();
        for (int x = 0; x < X_MAX; x++) {
            double xn = x / _scale;
            int y = _ref.y - (int)((_a * xn * xn + _b * xn) * _scale);
            _trajectory.add(new Point(x + _ref.x, y));
        }
        
        // turn on the setTraj flag
        _trajSet = true;
    }
   
    /* Plot a trajectory
     *
     * @param   canvas - the canvas to draw onto
     *          slingshot - bounding rectangle of the slingshot
     *          releasePoint - point where the mouse click was released from
     * @return  the canvas with trajectory drawn
     */
    public BufferedImage plotTrajectory(BufferedImage canvas, Rectangle slingshot, Point releasePoint) {
        List<Point> trajectory = predictTrajectory(slingshot, releasePoint);
        
        // draw estimated trajectory
        Graphics2D g2d = canvas.createGraphics();
        g2d.setColor(Color.RED);
        for (Point p : trajectory) {
            if ((p.y > 0) && (p.y < canvas.getHeight(null))) {
                g2d.drawRect(p.x, p.y, 1, 1);
            }
        }
        
        return canvas; 
    }

    // plot trajectory given the bounding box of the active bird
    public BufferedImage plotTrajectory(BufferedImage canvas, Rectangle slingshot, Rectangle activeBird) {
        
        // get active bird location
        Point bird = new Point((int)(activeBird.x + 0.5 * activeBird.width),
            (int)(activeBird.y + 0.85 * activeBird.height));
        
        return plotTrajectory(canvas, slingshot, bird);
    }

    /* find the release point given the sling location and launch angle, using maximum velocity
     *
     * @param   sling - bounding rectangle of the slingshot
     *          theta - launch angle in radians (anticlockwise from positive direction of the x-axis)
     * @return  the release point on screen
     */
    public Point findReleasePoint(Rectangle sling, double theta)
    {
    	/*
    	 * Modified by Angry-HEX Team
    	 * Added call to Configuration.getShotMagnitude()
    	 */
        double mag = sling.height * Configuration.getShotMagnitude();
        Point ref = getReferencePoint(sling);

        //System.out.println("SH:"+mag+" THETA:"+theta+" DEG:"+Math.toDegrees(theta));
        Point release = new Point((int)(ref.x - mag * Math.cos(theta)), (int)(ref.y + mag * Math.sin(theta)));
        //Point release1 = new Point((int)(ref.x - mag * Math.cos(theta)), (int)(ref.y - mag * Math.sin(theta)));
        //System.out.println("REFPOINT:("+ref.x+","+ref.y+") ");
        //System.out.println("RELPOINT:("+release.x+","+release.y+") ");
        //System.out.println("PPOINT:("+release1.x+","+release1.y+")");
        return release;
    }
    
    /* find the release point given the sling location, launch angle and velocity
     *
     * @param   sling - bounding rectangle of the slingshot
     *          theta - launch angle in radians (anticlockwise from positive direction of the x-axis)
     *          v - exit velocity as a proportion of the maximum velocity (maximum STRETCH)
     * @return  the release point on screen
     */
    public Point findReleasePoint(Rectangle sling, double theta, double v)
    {
        double mag = getSceneScale(sling) * STRETCH * v;
        Point ref = getReferencePoint(sling);
        Point release = new Point((int)(ref.x - mag * Math.cos(theta)), (int)(ref.y + mag * Math.sin(theta)));
        
        return release;
    }
    
    // find the reference point given the sling
    public Point getReferencePoint(Rectangle sling)
    {
        Point p = new Point((int)(sling.x + X_OFFSET * sling.width), (int)(sling.y + Y_OFFSET * sling.width));
        return p;
    }
    
    //get release angle 
    public double getReleaseAngle(Rectangle sling, Point releasePoint )
    {
        Point ref = getReferencePoint(sling);
        
        return -Math.atan2(ref.y - releasePoint.y, ref.x - releasePoint.x);
    }
    
    // predicts a trajectory
    public List<Point> predictTrajectory(Rectangle slingshot, Point launchPoint) {
        setTrajectory(slingshot, launchPoint);
	    return _trajectory;
    }
    
    // take the initial angle of the desired trajectory and return the launch angle required
    private double actualToLaunch(double theta)
    {
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta + _changeAngle[i-1];
        }
        return theta + _changeAngle[_launchAngle.length-1];
    }
    
    // take the launch angle and return the actual angle of the resulting trajectory
    private double launchToActual(double theta)
    {
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta - _changeAngle[i-1];
        }
        return theta - _changeAngle[_launchAngle.length-1];
    }
    
    // adjust the scale setting for this scene
    private void adjustScale(double v, double theta)
    {    
        int i = 0;
        while (i < _launchVelocity.length && theta > _launchAngle[i])
            i++;
        if (i == 0)
            i = 1;
        
        double temp = v / _launchVelocity[i-1];
            
        // avoid setting velocity to NaN
        if (temp != temp)
            return;
            
        // ignore very large changes
        if (temp > 1.1 || temp < 0.9)
           {
        	//System.out.println(" temp : " + temp);
        	return;
           }
                   
        if (theta > Math.toRadians(50))    
            _scaleFactor = temp;
        else if (theta > Math.toRadians(25))
            _scaleFactor = temp * 0.6 + _scaleFactor * 0.4;
                            
    }
    
    // get the velocity for the desired angle
    public double getVelocity(double theta)
    {
        if (theta < _launchAngle[0])    
            return _scaleFactor * _launchVelocity[0];
        
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta < _launchAngle[i])
                return _scaleFactor * _launchVelocity[i-1];
        }
        
        return _scaleFactor * _launchVelocity[_launchVelocity.length-1];
    }
    
    // return scene scale determined by the sling size
	/*
	 * Modified by Angry-HEX Team
	 * Changed modifier to public
	 */
    public double getSceneScale(Rectangle sling)
    {
        return sling.height + sling.width;
    }
    
    // finds the active bird, i.e., the one in the slingshot
    public Rectangle findActiveBird(List<Rectangle> birds) {
        // assumes that the active bird is the bird at the highest position
        Rectangle activeBird = null;
        for (Rectangle r : birds) {
            if ((activeBird == null) || (activeBird.y > r.y)) {
                activeBird = r;
            }
        }      
        return activeBird;
    }  
	
	/*
	 * Modified by Angry-HEX Team
	 * Added method "getScaleFactor"
	 */
	public double getScaleFactor()
	{
		return _scaleFactor;
	} 
	public int getTapTime(Rectangle sling, Point release, Point target, int tapInterval)
	{
		if (tapInterval == 0)
			return 0;
		Point tapPoint = new Point();
		int distance = target.x - sling.x;
		double r = ((double)tapInterval/100);
		tapPoint.setLocation(new Point((int)(distance * r + sling.x) , target.y));
		return getTimeByDistance(sling, release, tapPoint);
		
	}
	/* the estimated tap time given the tap point
    *
    * @param   sling - bounding box of the slingshot
    *          release - point the mouse clicked was released from
    *          tapPoint - point the tap should be made
    * @return  tap time (relative to the release time) in milli-seconds
    */
   public int getTapTime(Rectangle sling, Point release, Point tapPoint)
   {
       // update trajectory parameters
       setTrajectory(sling, release);
       
       double pullback = _scale * STRETCH * Math.cos(_theta);
       double distance = (tapPoint.x - _ref.x + pullback) / _scale;
       
       //System.out.println("distance " + distance);
       //System.out.println("velocity " + _ux);
       
       return (int)(distance / _ux * _timeUnit);
   }
  
}
