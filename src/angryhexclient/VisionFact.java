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
package angryhexclient;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

// import org.opencv.core.RotatedRect;

import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;
// import angryhexclient.OurVision.Block;

public class VisionFact {

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private Vision vision;
	private TargetReasoner reasoner;
	private List<ABObject> abobjects;
	private TrajectoryPlanner planner;
	
	//just for ground
	// private OurVision ourVision;

	public VisionFact(Vision vision, TargetReasoner reasoner,
			TrajectoryPlanner planner) {
		this.vision = vision;
		this.reasoner = reasoner;
		this.abobjects = new LinkedList<ABObject>();
		this.planner = planner;
	}

	public ABObject getABObject(int i) {
		return abobjects.get(i);
	}

	public List<ABObject> getABObjects() {
		return abobjects;
	}

	private int addABObject(ABObject ab) {
		ab.id = abobjects.size();
		abobjects.add(ab);
		return ab.id;
	}

	public void createFacts(ABType currentBird) {
		addBirdTypeToExecutor(currentBird);
		addTrajectoryInfoToExecutor(vision.findSlingshotRealShape());
		addPigs(vision.findPigsMBR());
		addBlocks(vision.findBlocksRealShape());
		addTNTs(vision.findTNTs());
		// addGround(ourVision.detectGround());
		// TODO test and uncomment the next line
		addHills(vision.findHills());
	}

	private void addPigs(List<ABObject> objs)
			throws UnsupportedOperationException {
		if (objs.isEmpty()) {
			throw new UnsupportedOperationException(
					"No pigs found in the scene.");
		}
		//at the moment pigs are blocks since we don't care about their
		//circle shape (they don't roll like stones etc..)
		addBlocks(objs);
	}

	private void addTNTs(List<ABObject> objs) {
	    //TODO if TNTs are treated the same way as blocks, remove this method
	    addBlocks(objs);
	}

	private void addBlocks(List<ABObject> objs) {
		for (ABObject ab : objs) {
		    if (ab.type != ABType.Unknown){
			switch (ab.shape) {
			case Circle:
				addCircle(ab);
				break;
			case Poly:
				addPoly(ab);
				break;
			case Rect:
				addRect(ab);
				break;
			case Triangle:
				addPoly(ab);
				break;
			default:
				Log.warning("unknown ABShape");
				break;
			}
		    } else{
			Log.warning("Found an Unknown object type. This object has been avoided.");
		    }
		}
	}

	private void addCircle(ABObject ab) {
            int index = addABObject(ab);
            String fact = String.format("circle(%d, %d, %d, \"%f\" ).",
                            index, (int) ab.getCenter().x, (int) ab.getCenter().y, ((Circle)ab).r);
           
            reasoner.addFact(fact);
            String type = VisionFact.getABTypeString(ab.getType()).toLowerCase();
            addBoundingBox(((Circle)ab).getBounds(), type, index,0.f);


	}

	private void addPoly(ABObject ab) {

	    String type = VisionFact.getABTypeString(ab.getType()).toLowerCase();
		int index = addABObject(ab);
		
	    Poly poly = (Poly) ab;

	    for (int i = 0; i < poly.polygon.npoints; i++) {
	    	// FIXME should we check if xpoint[i] or ypoint[i] are NULL?
			String fact = String.format("polyline(%d, %s, %d, %d, %d).", index, type,
				i, poly.polygon.xpoints[i], poly.polygon.ypoints[i]);
			reasoner.addFact(fact);
	    }

		addBoundingBox(ab.getBounds(), type, index, 0.f);

	}
        
        private void addBoundingBox(Rectangle obj, String type, int index, double angle){
            String fact = String.format("boundingBox(%d, %s, %d, %d, %d, %d, \"%f\").",
                                index, type, (int) obj.getCenterX(), (int) obj.getCenterY(), obj.width,
                                obj.height, angle);
            
		reasoner.addFact(fact);
        }

	private void addRect(ABObject obj) {
                //get the type of the object
                String type = VisionFact.getABTypeString(obj.getType()).toLowerCase();
                
		int width = obj.width;
		int height = obj.height;
		double angle = obj.angle;
		if (obj instanceof Rect){
		    Rect rr = (Rect)obj;
		    width = (int) rr.getpWidth();
		    height = (int) rr.getpLength();
		    //adjust the angle for box2d representation
		    angle = obj.angle - Math.toRadians(90);
		    //System.out.print(rr.getpWidth() + " " + rr.getpLength() + " " + rr.angle);
		}
		
                int index = addABObject(obj);
		//System.out.print(" at " + obj.getCenter().x + " , " + obj.getCenter().y);
		//System.out.println(" " + index);
                String fact = String.format("rectangle(%d, %s, %d, %d, %d, %d, \"%f\").",
                                index, type, (int) obj.getCenter().x, (int) obj.getCenter().y, width,
                                height, angle);

		reasoner.addFact(fact);
		Rectangle boundingBox = new Rectangle (obj.getCenter().x - width/2, obj.getCenter().y - height/2, width, height);
                addBoundingBox(boundingBox,type, index,angle);
	}

	// private void addTriangle(ABObject ab) {
 //           int index = addABObject(ab);
 //           Poly triangle = (Poly) ab;
 //           //get the type of the object
 //           String type = VisionFact.getABTypeString(ab.getType()).toLowerCase();
 //           String fact = String.format("triangle(%d, %s, %d, %d, %d, %d, %d, %d).", index, type,
 //                            triangle.polygon.xpoints[0], triangle.polygon.ypoints[0],
 //                            triangle.polygon.xpoints[1], triangle.polygon.ypoints[1],
 //                            triangle.polygon.xpoints[2], triangle.polygon.ypoints[2]);

 //            reasoner.addFact(fact);
 //            addBoundingBox(triangle.getBounds(), type, index,0.f);
 //        }

	private void addBirdTypeToExecutor(ABType type) {
		String ss = String.format("birdType(%s).", type.name().toLowerCase());
		reasoner.addFact(ss);
	}

	private void addTrajectoryInfoToExecutor(Rectangle slingshot) {
		// In previous versions, the scale factor was dubbed velocity.
		reasoner.addFact("velocity(\"" + planner.getScaleFactor() + "\").");

		String ss = String.format("slingshot(%d, %d, %d, %d).",
				(int) slingshot.getX(), (int) slingshot.getY(),
				(int) slingshot.getWidth(), (int) slingshot.getHeight());

		reasoner.addFact(ss);
	}

	public static String getABTypeString(ABType type) {
		switch (type) {
		case BlackBird:
			break;
		case BlueBird:
			break;
		case Ground:
			return "ground";
		case Hill:
			return "hill";
		case Ice:
			return "ice";
		case Pig:
			return "pig";
		case RedBird:
			break;
		case Sling:
			return "sling";
		case Stone:
			return "stone";
		case TNT:
			return "tnt";
		case Unknown:
            return "unknown";
		case WhiteBird:
			break;
		case Wood:
			return "wood";
		case YellowBird:
			break;
		default:
			break;

		}
		Log.warning("unknown ABType");
		return null;
	}
	
//Needed for ground. To be deleted once ground is computed by the new vision
	// private void addGround(List<Block> blocks){
	    // int count = abobjects.size();
	    // for (OurVision.Block b : blocks) {
		   //  // Each list member must be a block with at least one pixel.
		   //  assert (!b.pixels.isEmpty());

		   //  RotatedRect rr = b.getRBoundingBox();
		   //  // Enlarge Rectangles by 2 pixels, as they are frequently "seen"
		   //  // too small.
		   //  rr.size.height += 2;
		   //  rr.size.width += 2;

		   //  addRotatedRectToExecutor(count++,rr);
	    // }
	// }

	private void addHills(List<ABObject> l_obj) {
		for (ABObject ab : l_obj)
			if (ab instanceof Poly)
				addPoly(ab);
			else
				Log.severe("Hill is not a Poly!");
	}

	//Needed for ground. To be deleted once ground is computed by the new vision
	// private void addRotatedRectToExecutor(int index,RotatedRect rr) {
	// 	//
	// 	// GB: avoids strange 90 or -90 degrees rotations
	// 	//
	// 	int width, height, angle;
	// 	if (rr.angle >= 45) {
	// 		width = (int) rr.size.height;
	// 		height = (int) rr.size.width;
	// 		angle = (int) rr.angle - 90;
	// 	} else if (rr.angle <= -45) {
	// 		width = (int) rr.size.height;
	// 		height = (int) rr.size.width;
	// 		angle = (int) rr.angle + 90;
	// 	} else {
	// 		width = (int) rr.size.width;
	// 		height = (int) rr.size.height;
	// 		angle = (int) rr.angle;
	// 	}
	// 	String fact = String.format("boundingBox(%d, %s, %d, %d, %d, %d, \"%f\").",
	// 			index, "ground", (int) rr.center.x, (int) rr.center.y, width,
	// 			height, Math.toRadians(angle));
	// 	reasoner.addFact(fact);
	// }
}
