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

import org.opencv.core.RotatedRect;

import ab.planner.TrajectoryPlanner;
import ab.vision.BirdType;

public class OurVisionFact {

	private OurVision vision;
	private TargetReasoner reasoner;
	private TrajectoryPlanner planner;
	private List<RotatedRect> objects;
	
	public OurVisionFact(OurVision vision, TargetReasoner reasoner, TrajectoryPlanner planner) {
		this.vision = vision;
		this.reasoner = reasoner;
		this.planner = planner;
		this.objects = new LinkedList<RotatedRect>();
	}

	public RotatedRect getObject(int index) {
		return objects.get(index);
	}
	
	public List<RotatedRect> getObjects() {
		return objects;
	}
	
	public void createFacts(BirdType currentBird) {
		addPigsToExecutor(vision.findPigs());

		addBlocksToExecutor(vision.findIce(), "ice");
		addBlocksToExecutor(vision.findStones(), "stone");
		addBlocksToExecutor(vision.findWood(), "wood");
		addBlocksToExecutor(vision.detectGround(), "ground");
		addTNTs(vision.findTNTs());
		
		addBirdTypeToExecutor(currentBird);
		addTrajectoryInfoToExecutor(vision.findSlingshot());
	}
	
	/**
	 * Adds facts for the pigs to the executor.
	 */
	private void addPigsToExecutor(List<Rectangle> pigs) {
		for (Rectangle r : pigs) {
			RotatedRect rr = convertRectangleToRotatedRect(r);
			addRotatedRectToExecutor(objects.size(), "pig", rr);
			objects.add(rr);
		}
	}

	/**
	 * Adds the facts for the blocks to the executor.
	 * 
	 * @param blocks
	 *            The list of blocks as returned by the vision component. Each
	 *            List Member must be a block with at least one pixel.
	 */
	private void addBlocksToExecutor(List<OurVision.Block> blocks, String type) {
		for (OurVision.Block b : blocks) {
			// Each list member must be a block with at least one pixel.
			assert (!b.pixels.isEmpty());

			RotatedRect rr = b.getRBoundingBox();
			// Enlarge Rectangles by 2 pixels, as they are frequently "seen"
			// too small.
			rr.size.height += 2;
			rr.size.width += 2;

			addRotatedRectToExecutor(objects.size(), type, rr);
			objects.add(rr);
		}
	}

	/**
	 * Adds facts for the TNT boxes to the executor.
	 */
	private void addTNTs(List<Rectangle> TNTs) {
		for (Rectangle r : TNTs) {
			RotatedRect rr = convertRectangleToRotatedRect(r);
			addRotatedRectToExecutor(objects.size(), "tnt", rr);
			objects.add(rr);
		}
	}

	private RotatedRect convertRectangleToRotatedRect(Rectangle r) {
		return new RotatedRect(new org.opencv.core.Point(r.getCenterX(),
				r.getCenterY()), new org.opencv.core.Size(r.getSize()
				.getWidth(), r.getSize().getHeight()), 0);
	}

	private void addRotatedRectToExecutor(int index, String type,
			RotatedRect rr) {
		//
		// GB: avoids strange 90 or -90 degrees rotations
		//
		int width, height, angle;
		if (rr.angle >= 45) {
			width = (int) rr.size.height;
			height = (int) rr.size.width;
			angle = (int) rr.angle - 90;
		} else if (rr.angle <= -45) {
			width = (int) rr.size.height;
			height = (int) rr.size.width;
			angle = (int) rr.angle + 90;
		} else {
			width = (int) rr.size.width;
			height = (int) rr.size.height;
			angle = (int) rr.angle;
		}
		String fact = String.format("object(%d, %s, %d, %d, %d, %d, \"%f\").",
				index, type, (int) rr.center.x, (int) rr.center.y, width,
				height, Math.toRadians(angle));
		reasoner.addFact(fact);
	}
	
	private void addBirdTypeToExecutor(BirdType type) {
		String ss = String.format("birdType(%s).", type.name());
		reasoner.addFact(ss);
	}

	private void addTrajectoryInfoToExecutor(Rectangle slingshot) {
		// In previous versions, the scale factor was dubbed velocity.
		reasoner.addFact(
				"velocity(\"" + planner.getScaleFactor() + "\").");

		String ss = String.format("slingshot(%d, %d, %d, %d).",
				(int) slingshot.getX(), (int) slingshot.getY(),
				(int) slingshot.getWidth(), (int) slingshot.getHeight());

		reasoner.addFact(ss);
	}
}
