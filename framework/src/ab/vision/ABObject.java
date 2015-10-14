/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;

public class ABObject extends Rectangle {
 private static final long serialVersionUID = 1L;
 private static int counter = 0;
 public int id;
 //object type
 public ABType type;
 
 public int area = 0;
 //For all MBRs, the shape is Rect by default.
 public ABShape shape = ABShape.Rect;
 
//For all MBRs, the angle is 0 by default.
 public double angle = 0;
 
//is Hollow or not
 public boolean hollow = false;
 
 
public ABObject(Rectangle mbr, ABType type) {
	super(mbr);
	this.type = type;
	this.id = counter++;
}
public ABObject(Rectangle mbr, ABType type, int id) {
	super(mbr);
	this.type = type;
	this.id = id;
}
public ABObject(ABObject ab)
{
	super(ab.getBounds());
	this.type = ab.type;
	this.id = ab.id;
}
public ABObject()
{
	this.id = counter ++;
	this.type = ABType.Unknown;
}
public ABType getType()
{
	return type;
}

public Point getCenter() {
   return new Point((int)getCenterX(), (int)getCenterY());
}

public static void resetCounter() {
	counter = 0;	
}

}
