/*
 * Modified by Angry-HEX Team
 * Removed Auto-generated TODOs
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
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.Vision;

public class ShootingAgent {
    
	
	public static void shoot(String[] args, boolean cshoot)
	{
		ActionRobot ar = new ActionRobot();
		TrajectoryPlanner tp = new TrajectoryPlanner();
		ActionRobot.fullyZoomOut();
		Vision vision = new Vision(ActionRobot.doScreenShot());
		Rectangle slingshot = vision.findSlingshotMBR();
		while(slingshot == null)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
			vision = new Vision(ActionRobot.doScreenShot());
			slingshot = vision.findSlingshotMBR();
		}
		Point refPoint = tp.getReferencePoint(slingshot);
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int tap = 0;
		if(args.length > 3)
			tap = Integer.parseInt(args[3]);
		
		Shot shot = null;
		if(cshoot)
			shot = new Shot( refPoint.x, refPoint.y, -x, y,0,tap);
		else
		{
			int r = x;
			double theta = y / 100;
			int dx = -(int) (r * Math.cos(Math.toRadians(theta)));
			int dy = (int) (r * Math.sin(Math.toRadians(theta)));
			shot = new Shot( refPoint.x, refPoint.y, dx, dy,0,tap);
		}
		vision = new Vision(ActionRobot.doScreenShot());
		Rectangle _slingshot = vision.findSlingshotMBR();
		if(!slingshot.equals(_slingshot))
			System.out.println("the scale is changed, the shot might not be executed properly.");
		ar.cshoot(shot);
		System.exit(0);
	}
	


}
