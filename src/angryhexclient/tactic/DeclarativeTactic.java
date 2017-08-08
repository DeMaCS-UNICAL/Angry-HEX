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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import ab.demo.other.ClientActionRobotJava;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import angryhexclient.Memory;
import angryhexclient.TargetReasoner;
import angryhexclient.VisionFact;
import angryhexclient.util.DebugUtils;

public class DeclarativeTactic extends Tactic {

	public DeclarativeTactic(final ClientActionRobotJava ar, final TacticManager manager) {
		super(ar, manager);
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
	@Override
	protected Target reason(final Vision vision, final ABType currentBird, final byte currentLevel)
			throws UnsupportedOperationException, IOException, InterruptedException {
		Tactic.Log.info("Reasoning...");

		final VisionFact visionFacts = new VisionFact(vision, TargetReasoner.getInstance(), manager.getTp());
		visionFacts.createFacts(currentBird);

		final BufferedImage img = DebugUtils.drawObjectsWithID(manager.getScreenshot(), visionFacts.getABObjects());
		DebugUtils.showImage(img);
		DebugUtils.saveScreenshot(img, String.format("level_%d_%d", (int) currentLevel, manager.getCurrentTurn()));

		// If it's the first shoot it load the targets selected before
		if (manager.isFirstShoot()) {
			// load memory for current level
			final List<Point.Double> targets = Memory.load(currentLevel);
			// It finds the objects more similar to the previous targets
			for (final Point.Double point : targets)
				for (int i = 0; i < visionFacts.getABObjects().size(); i++) {
					final ABObject rr = visionFacts.getABObject(i);
					if (rr.contains(point)) {
						final String ss = String.format(":- target(%s,_).", i);
						TargetReasoner.getInstance().addFact(ss);
						break;
					}
				}
		}

		DebugUtils.addTime("CallReasonBird", System.nanoTime());

		Tactic.Log.info("Calling HEX...");
		// Now we call HEX to evaluate our program.
		TargetReasoner.getInstance().reason(currentBird);
		Tactic.Log.info("Called HEX");

		DebugUtils.addTime("EndReasonBird", System.nanoTime());

		// We extract the solution(s) of the program and select a random one.
		// All solutions to the hex program should be reasonably reasonable. No
		// solution should be significantly worse than the others and the best
		// solutions should be included. Therefore we can just select any one of
		// them at random.
		final List<TargetReasoner.TargetData> answers = TargetReasoner.getInstance().getTargets();

		if (answers.size() == 0) {
			Tactic.Log.warning("No answer set found");
			final List<ABObject> pigs = vision.findPigsMBR();

			final ABObject pig = pigs.get(new Random().nextInt(pigs.size()));
			return new Target(new Point((int) pig.getCenterX(), (int) pig.getCenterY()), new Random().nextInt(2),
					80 + new Random().nextInt(31));
		}

		final TargetReasoner.TargetData answer = answers.get(new Random().nextInt(answers.size()));

		final ABObject answerObject = visionFacts.getABObject(answer.id);
		Tactic.Log.info("///////////");
		Tactic.Log.info(answer.toString());
		Tactic.Log.info("///////////");

		// If it's the first shoot it saves on file the target
		if (manager.isFirstShoot())
			Memory.store(currentLevel, answerObject.getCenter());

		//
		// Builds target point depending on trajectory and offset: right face
		// for low traj, upper face for high traj
		//
		// double x_target = answer.trajectory == 0 ? answerRectangle.center.x -
		// answerRectangle.size.width/2 : answerRectangle.center.x;
		// double y_target = answer.trajectory == 1 ? answerRectangle.center.y +
		// answerRectangle.size.height/2 :
		// Math.max(0,answerRectangle.center.y-answerRectangle.size.height*answer.yoffsetRatio/2);
		//
		double x_target;
		double y_target;
		if (answer.eggMode) {
			x_target = answerObject.getCenterX();
			y_target = answer.yoffsetRatio;
		} else {
			x_target = answer.trajectory == 0 ? answerObject.getCenterX() - answerObject.width / 2
					: answer.yoffsetRatio;
			y_target = answer.trajectory == 1 ? answerObject.getCenterY() - answerObject.height / 2
					: answer.yoffsetRatio;
		}

		return new Target(new Point((int) x_target, (int) y_target), answer.trajectory, answer.tapCoeff);
	}
}
