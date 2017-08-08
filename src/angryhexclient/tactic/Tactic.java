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
package angryhexclient.tactic;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ab.demo.other.ClientActionRobotJava;
import ab.demo.other.Env;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import angryhexclient.Configuration;
import angryhexclient.WrongScreenshotException;
import angryhexclient.util.DebugUtils;

/**
 * The Tactic class is delegated to: - analyze the level and extract information
 * - play the current turn
 */
public abstract class Tactic {

	private static final int MAXIMUM_WAITING_TIME = 3;

	/**
	 * A class that serves as the return value of the reasoning function,
	 * because not only the target point but also the trajectory needs to be
	 * returned.
	 */
	class Target {
		/**
		 * The trajectory, 0 for low, 1 for high.
		 */
		public int trajectory;
		/**
		 * Tap coefficient, percentage variation on the tap time.
		 */
		public int tapCoeff;
		/**
		 * The target point that the bird will be shot at.
		 */
		public Point target;

		// public double yoffsetRatio;

		public Target(final Point ta, final int tr, final int tap) {
			trajectory = tr;
			target = ta;
			tapCoeff = tap;
			// yoffsetRatio = yoffset;
		}
	}

	// TODO check if the bird should be red or blue
	public final static ABType DEFAULT_BIRD_ON_SLINGSHOT = ABType.BlueBird;
	protected static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	// focus point
	private int focus_x;
	private int focus_y;
	private final ClientActionRobotJava ar;

	protected TacticManager manager;

	public Tactic(final ClientActionRobotJava ar, final TacticManager manager) {
		this.ar = ar;
		this.manager = manager;
	}

	private void addDebugDataOfTheLevel(final Vision vision, final ABType currentBird) {
		try {
			DebugUtils.setCurrentBird(currentBird.name());
			DebugUtils.addBirds(manager.getBirdsQueue());
			DebugUtils.setN_pigs(vision.findPigsMBR().size());
			DebugUtils.addBlocks(vision.findBlocksRealShape());
			DebugUtils.setN_hills(vision.findHills().size());
			DebugUtils.setN_tnt(vision.findTNTs().size());
		} catch (final Exception e) {
			// We don't care if we are not able to collect Debug data
			Tactic.Log.severe(e.toString());
		}
	}

	private Point getAnticipatedPoint(final TrajectoryPlanner tp, final Rectangle slingshot, final Point launchPoint,
			final int targetX, final double fraction) {
		//
		// Follows the parabola perimeter and find the X coordinate closer to
		// the fraction*parabola
		//

		final List<Point> ps = tp.predictTrajectory(slingshot, launchPoint);
		double dist = 0;
		for (int i = 0; i < ps.size() - 1 && ps.get(i).x < targetX; i++)
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2) + Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));
		final double targetDist = dist * fraction;
		Tactic.Log.info("Points:" + ps.size() + " Dist: " + dist + " TargetDist:" + targetDist);
		dist = 0;
		Point lastX = new Point();
		for (int i = 0; i < ps.size() - 1 && dist < targetDist; i++) {
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2) + Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));

			// FIXME Check if precision is enough without averaging between x
			// and x+1
			lastX = ps.get(i);
		}
		System.out.println("TargetX: " + targetX + " TapX:" + lastX);
		return lastX;
	}

	/**
	 * This function gets the tap time. It calculates the waiting time needed so
	 * that the bird will be tapped at approximately the position tapX.
	 */
	private int getTapTime(final int targetX, final TrajectoryPlanner tp, final Rectangle slingshot,
			final Point launchPoint, final double fraction, final ABType b) {
		// double dist = getDistance(tp, slingshot, launchPoint, targetX);
		final Point tapPoint = getAnticipatedPoint(tp, slingshot, launchPoint, targetX, fraction);
		// return (int) ((dist / (tp.getSceneScale(slingshot) * speedRatio)) *
		// 1000);
		// commented out because it's not used
		// double angle = tp.getReleaseAngle(slingshot,launchPoint);
		//
		// Our estimate
		//
		// return (int) (tapPoint.getX() / (Configuration.getSpeedRatioByBird(b)
		// * Math.cos(angle) * tp.getVelocity(angle)) * 10);
		//
		// Organizers estimate
		//
		return tp.getTapTime(slingshot, launchPoint, tapPoint);

	}

	private void quickShoot(final int focus_x2, final int focus_y2, final int i, final int j, final int k,
			final int tap_time, final boolean polar, final ClientActionRobotJava ar) {
		ar.fastshoot(focus_x2, focus_y2, i, j, k, tap_time, polar);
		waitUntilFinalized();
	}

	private void waitUntilFinalized(){
		BufferedImage screenshot = getStableImage(ar);
		GameState state = ar.checkState();
		Vision vision = new Vision(screenshot);
		int counter = 0;
		while(state == GameState.PLAYING && vision.findPigsMBR().isEmpty() && counter++ < MAXIMUM_WAITING_TIME){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			screenshot = getStableImage(ar);
			vision = new Vision(screenshot);
		}
	}
	
	private BufferedImage getStableImage(final ClientActionRobotJava ar) {
		// wait a maximum of 15 seconds
		int waitSeconds = 0;
		// do not wait if difference between screenshots is less than threshold
		// consider a cropped screenshot without the slingshot and jumping birds
		final int threshold = Configuration.getFastshootThreshold();
		BufferedImage prevImage_cropped = null;
		BufferedImage thisImage = ar.doScreenShot();
		final int xCoordinate = manager.getSling().x + 80;
		final int width = thisImage.getWidth() - xCoordinate;
		final int height = thisImage.getHeight();
		BufferedImage thisImage_cropped = thisImage.getSubimage(xCoordinate, 0, width, height);

		// int pixelDifference = 0;
		int pixelDifference_cropped = 0;
		while (prevImage_cropped == null || waitSeconds <= 15 && pixelDifference_cropped > threshold) {
			prevImage_cropped = thisImage_cropped;
			try {
				Thread.sleep(1000);
				// update frame count
				waitSeconds++;
			} catch (final InterruptedException e) {
				Tactic.Log.severe("interrupted in getStableImage: " + e.getMessage());
			}
			thisImage = ar.doScreenShot_();
			thisImage_cropped = thisImage.getSubimage(xCoordinate, 0, width, height);
			// pixelDifference = VisionUtils.numPixelsDifferent(prevImage,
			// thisImage);
			pixelDifference_cropped = VisionUtils.numPixelsDifferent(prevImage_cropped, thisImage_cropped);

			// Log.info("quickShoot: after "+waitSeconds+" seconds: pixel
			// difference "+pixelDifference+" (limit "+threshold+")");
			Tactic.Log.info("getStableImage: after " + waitSeconds + " seconds: pixel difference " + pixelDifference_cropped
					+ " (limit " + threshold + ")");
		}
		Tactic.Log.info("finished getStableImage after waiting " + waitSeconds + " seconds");
		
		return thisImage;
	}

	/**
	 * The method that does all the reasoning. It takes the vision component and
	 * returns the best shot according to our HEX program.
	 *
	 * @param tp
	 *
	 * @return A result instance, containing the target and the trajectory to be
	 *         used.
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws InterruptedException
	 */
	protected abstract Target reason(Vision vision, ABType currentBird, byte currentLevel)
			throws UnsupportedOperationException, IOException, InterruptedException;

	/**
	 * Compute the parabola to shoot the target
	 *
	 * @param sling
	 * @param currentBird
	 * @param result
	 * @return
	 * @return
	 */
	protected Point shoot(final Rectangle sling, final ABType currentBird, final Target result,
			final byte currentLevel) {

		int tap_time; // Tap Coefficient comes from reasoning

		// start shooting

		final Point _tpt = result.target;

		Tactic.Log.info(String.format("TGT: %s TR: %s", _tpt, result.trajectory == 0 ? "low" : "high"));

		// estimate the trajectory
		Point releasePoint;
		final ArrayList<Point> pts = manager.getTp().estimateLaunchPoint(sling, _tpt);
		if (pts.size() == 1)
			releasePoint = pts.get(0);
		else
			releasePoint = pts.get(result.trajectory);

		final Point refPoint = manager.getTp().getReferencePoint(sling);

		// focus_x = (int) currentBirdRect.getCenterX();
		// focus_y = (int) currentBirdRect.getCenterY();
		// Get the center of the active bird as focus point

		focus_x = (int) (Env.getFocuslist().containsKey(currentLevel) ? Env.getFocuslist().get(currentLevel).getX()
				: refPoint.x);
		focus_y = (int) (Env.getFocuslist().containsKey(currentLevel) ? Env.getFocuslist().get(currentLevel).getY()
				: refPoint.y);

		// System.out.print("FX:"+focus_x+" FY:"+focus_y+" RP: " +
		// releasePoint+" ");

		// Get the release point from the trajectory prediction
		// module
		if (releasePoint != null) {
			// commented out because it's not used
			// double releaseAngle = tp.getReleaseAngle(sling,
			// releasePoint);
			// System.out.print("ANG : "
			// + Math.toDegrees(releaseAngle)+ " Tap Coeff:"+result.tapCoeff+"
			// ");

			// Tap Coefficient now comes from reasoning
			tap_time = getTapTime(_tpt.x, manager.getTp(), sling, releasePoint, result.tapCoeff / 100.0, currentBird);
			Tactic.Log.info("Tap time: " + tap_time);
		} else {
			Tactic.Log.severe("releasePoint is null!");
			tap_time = 0;
		}

		// checks whether the slingshot is changed. the change of the
		// slingshot indicates a change in the scale.

		// ar.fullyZoomOut();
		// screenshot = ar.doScreenShot();
		// vision = new Vision(screenshot);
		// Rectangle _sling = vision.findSlingshot();
		// if (sling.equals(_sling))
		// { // }
		//
		// // make the shot
		// ar.shoot(focus_x, focus_y, (int) releasePoint.getX()
		// - focus_x, (int) releasePoint.getY() - focus_y,
		// 0, tap_time, false);

		DebugUtils.addTime("QuickShot1", System.nanoTime());

		// New method, tries to improve performance by not waiting
		// 15 still not losing shoots.
		final String par1 = focus_x + "";
		final String par2 = focus_y + "";
		final String par3 = (int) releasePoint.getX() - focus_x + "";
		final String par4 = (int) releasePoint.getY() - focus_y + "";
		final String par5 = 0 + "";
		final String par6 = tap_time + "";
		final String par7 = false + "";

		quickShoot(focus_x, focus_y, (int) releasePoint.getX() - focus_x, (int) releasePoint.getY() - focus_y, 0,
				tap_time, false, ar);
		Tactic.Log.info("Shot done.");
		Tactic.Log.info("Parameters  of the shot: " + par1 + "; " + par2 + "; " + par3 + "; " + par4 + "; " + par5
				+ "; " + par6 + "; " + par7);

		DebugUtils.addTime("QuickShotDone", System.nanoTime());

		DebugUtils.addTime("StateReco", System.nanoTime());

		return releasePoint;
	}

	/**
	 * Solve a particular level by shooting birds directly to pigs
	 *
	 * @param ar
	 * @param screenshot
	 * @param birdsQueue
	 * @param sling
	 * @param tp
	 * @param currentLevel
	 *
	 * @return GameState: the game state after shots.
	 * @throws WrongScreenshotException
	 */
	public GameState solve(final byte currentLevel) throws WrongScreenshotException {

		DebugUtils.addTime("SolveStart", System.nanoTime());

		// If there is a sling, then play, otherwise skip.
		final Rectangle sling = manager.getSling();

		if (sling != null) {
			// get information
			final Vision vision = new Vision(manager.getScreenshot());
			final ABType currentBird = manager.getCurrentBird();
			if (currentBird == null)
				throw new WrongScreenshotException();

			if (!vision.findPigsMBR().isEmpty()) {

				addDebugDataOfTheLevel(vision, currentBird);

				DebugUtils.addTime("PigsFound", System.nanoTime());

				// invoke reasoner
				Target result;

				try {
					result = reason(vision, currentBird, currentLevel);
				} catch (UnsupportedOperationException | IOException | InterruptedException e) {
					// If there is an exception just return the game state,
					// so the shot will be attempted again.
					Tactic.Log.severe("error calling reasoner: " + e.getMessage());
					return ar.checkState();
				}

				DebugUtils.addTime("Reasoned", System.nanoTime());

				if (result == null) {
					Tactic.Log.severe("null result from reasoner");
					return ar.checkState();
				}

				final Point releasePoint = shoot(sling, currentBird, result, currentLevel);

				DebugUtils.addTime("ShotEnd", System.nanoTime());

				final List<Point> traj = vision.findTrajPoints();
				manager.getTp().adjustTrajectory(traj, sling, releasePoint);

			} else{
				Tactic.Log.info("Sling was recognized but no pig is recognized, skipping the level");
				throw new WrongScreenshotException();
			}
			
			
		} else{
			Tactic.Log.info("Sling was not recognized, skipping the level");
			throw new WrongScreenshotException();
		}

		return ar.checkState();
	}

}
