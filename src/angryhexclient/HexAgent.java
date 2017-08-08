/*******************************************************************************
 * Angry-HEX - an artificial player for Angry Birds based on declarative knowledge bases
 * Copyright (C) 2012-2016 Francesco Calimeri, Michael Fink, Stefano Germano, Andreas Humenberger, Giovambattista Ianni, Christoph Redl, Daria Stepanova, Peter Schueller, Andrea Tucci, Anton Wimmer.
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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
//commented out because it's not used
//import org.opencv.core.RotatedRect;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import angryhexclient.strategy.BenchmarkStrategy;
import angryhexclient.strategy.DeclarativeStrategy;
import angryhexclient.strategy.StrategyManager;
import angryhexclient.tactic.DeclarativeTactic;
import angryhexclient.tactic.Tactic;
import angryhexclient.tactic.TacticManager;
import angryhexclient.util.DebugUtils;

/**
 * This is the agent containing all the tactics
 *
 */
public class HexAgent implements Runnable {

	private static final int MAXIMUM_WAITING_TIME = 10;
	private static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final GameStateExtractor gse = new GameStateExtractor();

	private static final int MAX_FAILURES_ALLOWED = 3;

	// Wrapper of the communicating messages
	private final ClientActionRobotJava ar;
	private Tactic currentTactic;
	private TacticManager tacticManager;
	private StrategyManager strategyManager;

	public HexAgent(final ClientActionRobotJava ar) throws Exception {
		this(ar, (byte) 1);
	}

	public HexAgent(final ClientActionRobotJava ar, final byte startingLevel) throws Exception {
		this.ar = ar;

		final byte[] configureData = ar.configure(ClientActionRobot.intToByteArray(Configuration.getTeamID()));
		// TODO: configureData has 4 bytes as far as I can see - where are the
		// meanings of these bytes documented? we use the first three TODO we
		// should *interpret* these bytes here and store them to properly named
		// variables before giving them to other system parts
		// with the following we can override numberOfLevels
		// configureData[2] = 2;
		if (BenchmarkStrategy.BENCHMARK)
			strategyManager = new BenchmarkStrategy(ar, startingLevel, configureData);
		else
			strategyManager = new DeclarativeStrategy(ar, startingLevel, configureData);

	}

	private void emptyQueueException() throws WrongScreenshotException {
		Queue<ABObject> birdsQueue = getBirdsQueue(tacticManager.getScreenshot());
		if (birdsQueue == null || birdsQueue.isEmpty()) {
			final ABType birdType = tacticManager.getCurrentBird();
			if (birdType == null) {
				// If the queue is empty, then try to do recognition of the bird
				// on a slingshot again
				final ABType type = getBirdType(new Vision(tacticManager.getScreenshot()), tacticManager.getSling());
				if (type == null)
					throw new WrongScreenshotException();

				if (birdsQueue == null)
					birdsQueue = new LinkedList<>();
				birdsQueue.add(new ABObject(type));
				tacticManager.setBirdsQueue(birdsQueue);
			}
		}

		tacticManager.setNextBird();

	}

	private void extractInfo() {
		boolean hopelessLevel = true;
		tacticManager.setSling(null);
		while (tacticManager.getSling() == null && hopelessLevel) {
			HexAgent.Log.info("Making screenshot..");
			tacticManager.setScreenshot(ar.doScreenShot());
			HexAgent.Log.info("Building birds queue..");
			tacticManager.setBirdsQueue(getBirdsQueue(tacticManager.getScreenshot()));
			if (tacticManager.getSling() == null) {
				hopelessLevel = true;
				strategyManager.loadNewLevel(GameState.LOST);
				tacticManager.reset();
			} else
				hopelessLevel = false;
		}
	}

	/**
	 * use this method to all the operations needed after playing level
	 *
	 * @param state
	 */
	private void finalizeLevel() {
		
		GameState state = ar.checkState();
		
		// If the level was processed completely, i.e. either success or fail,
		// then update the scores according to the strategy
		if (state == GameState.WON || state == GameState.LOST) {
			HexAgent.Log.info("ZGS: state is either won or lost");
			if (BenchmarkStrategy.BENCHMARK)
				if (state == GameState.WON) {
					final int score = HexAgent.gse.getScoreEndGame(ar.doScreenShot());
					strategyManager.updateScore(score);
					HexAgent.Log.fine("WON|score: " + score);
				} else {
					strategyManager.updateScore(0);
					HexAgent.Log.fine("LOST|score: 0");
				}

			// Store the scores in the csv file if we are in the debug mode
			if (state == GameState.WON && DebugUtils.DEBUG)
				try {
					strategyManager.saveScores("scores.csv");
				} catch (final IOException e) {
					HexAgent.Log.warning("cannot save scores: " + e.getMessage());
				}
		} else
			HexAgent.Log.info("unexpected state " + state + ", go to the last current level : "
					+ strategyManager.getCurrentLevel());
	}

	// Get an ordered set of bird types for a given level
	private Queue<ABObject> getBirdsQueue(final BufferedImage scr) {

		HexAgent.Log.info("Started birds queue construction");
		// Create visions for both MBR and RealShape algorithms
		Vision vision_mbr = new Vision(scr);
		Vision vision_rs = new Vision(scr);

		// Create lists for storing the identified birds
		List<ABObject> birds_mbr = new ArrayList<>();
		List<ABObject> birds_rs = new ArrayList<>();
		List<ABObject> birds = new ArrayList<>();
		final Queue<ABObject> ordered_birds = new LinkedList<>();

		// variables for the number of recognized birds
		int number_of_mbr_birds;
		int number_of_rs_birds;

		// flag defining the recognition algorithm to be used
		boolean mbr = false;
		boolean rs = false;

		// By default determine a slingshot using MBR algorithm
		tacticManager.setSling(vision_mbr.findSlingshotMBR());

		// If the sling was not properly recognized, try various possibilities
		// to still find it
		if (tacticManager.getSling() == null && ar.checkState() == GameState.PLAYING) {
			BufferedImage screenshot2;
			HexAgent.Log.info("No slingshot was detected by MBR. Please remove pop up or zoom out");
			HexAgent.Log.info("Redo screenshot and try realShape");
			screenshot2 = ar.doScreenShot();
			vision_rs = new Vision(screenshot2);
			tacticManager.setSling(vision_rs.findSlingshotRealShape());
			if (tacticManager.getSling() == null) {
				HexAgent.Log.info("Sling is null, try to zoomIn and run MBR");
				ar.fullyZoomIn();
				screenshot2 = ar.doScreenShot();
				vision_mbr = new Vision(screenshot2);
				tacticManager.setSling(vision_mbr.findSlingshotMBR());
			}

			if (tacticManager.getSling() == null) {
				HexAgent.Log.info("Sling is still null, try to zoomIn and run RealShape");
				ar.fullyZoomIn();
				screenshot2 = ar.doScreenShot();
				vision_rs = new Vision(screenshot2);
				tacticManager.setSling(vision_rs.findSlingshotRealShape());
			}
		}

		// If the sling was recognized, proceed with bird detection
		if (tacticManager.getSling() != null) {
			HexAgent.Log.info("Slingshot's X coordinate is: " + tacticManager.getSling().getCenterX());

			// Recognize birds using MBR and RealShape algorithms
			birds_mbr = vision_mbr.findBirdsMBR();
			birds_rs = vision_rs.findBirdsRealShape();

			// Store the number of identified birds
			number_of_mbr_birds = birds_mbr.size();
			number_of_rs_birds = birds_rs.size();

			HexAgent.Log.info("Found " + number_of_mbr_birds + " mbr birds");
			HexAgent.Log.info("Found " + number_of_rs_birds + " realshape birds");

			// Pick the algorithm that recognized more birds
			if (number_of_mbr_birds >= number_of_rs_birds) {
				HexAgent.Log.info("Choose mbr algorithm");
				birds = birds_mbr;
				mbr = true;
			} else {
				HexAgent.Log.info("Choose realshape algorithm");
				birds = birds_rs;
				rs = true;
			}
			HexAgent.Log.info("Original birdset");
			for (int i = 0; i < birds.size(); i++)
				HexAgent.Log.info(i + ": " + birds.get(i).getCenterX() + ":  " + birds.get(i).type);

			// Create an ordered set of bird types, the first bird in the one on
			// the slingshot, the rest are ordered from right to left

			if (birds.size() > 1) {

				// case when the most left bird is the closest to the slingshot
				// sort birds based in centerX in the decsending order

				HexAgent.Log.info("Sorting birds in descending order..");
				Collections.sort(birds, (ab1, ab2) -> Double.compare(ab1.getCenterX(), ab2.getCenterX()));

				for (int i = 0; i < birds.size(); i++)
					HexAgent.Log.info(i + ": " + birds.get(i).getCenterX() + ":  " + birds.get(i).type);

				HexAgent.Log.info("Detecting the bird on the sling");
				HexAgent.Log.info("The distance between the first bird and the sling is "
						+ Math.abs(birds.get(0).getCenterX() - tacticManager.getSling().getCenterX()));
				HexAgent.Log.info("The distance between the last bird and the slingshot is "
						+ Math.abs(birds.get(birds.size() - 1).getCenterX() - tacticManager.getSling().getCenterX()));

				// Determine the bird that is the closest to the slingshot
				// Case when the most left bird is the closest to the slinshot:

				if (Math.abs(birds.get(0).getCenterX() - tacticManager.getSling().getCenterX()) <= 5) {
					ordered_birds.add(birds.get(0));
					HexAgent.Log.info("The first bird is on the sling" + ordered_birds.peek());
					for (int i = birds.size() - 1; i > 0; i--)
						ordered_birds.add(birds.get(i));
				}

				// Case when the most right bird is the closest to the slingshot
				else if (Math
						.abs(tacticManager.getSling().getCenterX() - birds.get(birds.size() - 1).getCenterX()) <= 5) {
					ordered_birds.add(birds.get(birds.size() - 1));
					HexAgent.Log.info("The last bird is on the sling" + ordered_birds.peek());
					for (int i = birds.size() - 2; i >= 0; i--)
						ordered_birds.add(birds.get(i));
				} else {
					HexAgent.Log.info("No sling bird was found");
					if (mbr) {
						HexAgent.Log.info("Check a sling bird among mbr birds");
						for (int i = 0; i < birds_rs.size(); i++)
							if (Math.abs(tacticManager.getSling().getCenterX() - birds_rs.get(i).getCenterX()) <= 5) {
								ordered_birds.add(birds_rs.get(i));
								HexAgent.Log.info("Birds on sling is " + ordered_birds.peek());
								i = birds_rs.size();
							}
					} else if (rs) {
						HexAgent.Log.info("Check a sling bird among rs birds");
						for (int i = 0; i < birds_mbr.size(); i++)
							if (Math.abs(tacticManager.getSling().getCenterX() - birds_mbr.get(i).getCenterX()) <= 5) {
								ordered_birds.add(birds_mbr.get(i));
								HexAgent.Log.info("Birds on sling is " + ordered_birds.peek());
								i = birds_mbr.size();
							}
					}
					for (int i = birds.size() - 1; i > 0; i--)
						ordered_birds.add(birds.get(i));

				}
			}

			// Only a single bird was recognized
			else if (!ordered_birds.isEmpty())
				ordered_birds.add(birds.get(0));
		}

		// The sling has not been recognized; load the new level
		else {
			HexAgent.Log.info("Sling is null");
			// store 0 score for this level and load a new one
			final int scor[] = ar.checkMyScore();
			for (int i = 0; i < scor.length; i++)
				HexAgent.Log.info("The score of " + i + " is: " + scor[i]);
		}

		// Log.info("Ordered birdset: ");

		// for (int i=0;i<ordered_birds.size();i++)
		// Log.info(i+": "+ordered_birds.get(i).getCenterX()+":
		// "+ordered_birds.get(i).type);
		return ordered_birds;
	}

	/**
	 * Gets bird type, side effect updates currentBird.
	 */
	private ABType getBirdType(final Vision vision, final Rectangle slingshot) {
		ABType type = null;

		// commented out because it's not used
		// Rectangle closest = null;
		double bestDist = 0;
		final List<ABObject> birds = vision.findBirdsMBR();
		// Log.info(b.name() + ":" + birds.size() + " ");
		HexAgent.Log.info("ZGS: list of birds - " + birds);

		if (birds.isEmpty()) {
			HexAgent.Log.info("ZGS: list of birds is empty");
			return null;
		}
		for (final ABObject bird : birds) {
			//
			// Purposely considers the (centerX, minY) coordinate for
			// the slingshot.
			//
			final double val = Point2D.distance(bird.getCenterX(), bird.getCenterY(), slingshot.getCenterX(),
					slingshot.getY());
			if (val < bestDist || bestDist == 0) {
				type = bird.type;
				bestDist = val;
				// commented out because it's not used
				// closest = bird;
			}
		}
		// commented out because it's not used
		// currentBirdRect = closest;
		HexAgent.Log.info("BT:" + type.name());

		return type;
	}

	private void initAgent() {
		tacticManager = new TacticManager();
		currentTactic = new DeclarativeTactic(ar, tacticManager);

		DebugUtils.init(strategyManager, tacticManager);
		Memory.init();
	}

	// run the thread
	public void playStrategy() {

		int failure = 0;

		preparation();

		DebugUtils.addTime("RunBeforeLoop", System.nanoTime());
		DebugUtils.saveBenchmark();

		GameState state = null;
		while (failure < HexAgent.MAX_FAILURES_ALLOWED)
			try {

				// TODO Make a new trajectory planner whenever a new level is
				// entered
				HexAgent.Log.info("Loading level number " + strategyManager.getCurrentLevel());

				//if (state == GameState.WON || state == GameState.LOST)
				strategyManager.loadNewLevel(state);

				tacticManager.reset();

				extractInfo();
				playTactic();
				finalizeLevel();

				DebugUtils.addTime("End", System.nanoTime());
				DebugUtils.saveBenchmark();

				failure = 0;
				state = ar.checkState();

			} catch (final Exception e) {
				failure++;
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				HexAgent.Log.severe("agent run Thread terminated by exception " + sw.toString());
			}

	}

	private void playTactic() {

		int failures = 0;

		DebugUtils.initBenchmarkParametersValues();
		DebugUtils.addTime("LoopStart", System.nanoTime());
		DebugUtils.setCurrentLevel(strategyManager.getCurrentLevel());

		GameState state = ar.checkState();
		while (state == GameState.PLAYING && failures < HexAgent.MAX_FAILURES_ALLOWED)
			try {
				DebugUtils.setCurrentTurn(tacticManager.getCurrentTurn());

				tacticManager.setScreenshot(ar.doScreenShot());
				if (HexAgent.gse.getGameState(tacticManager.getScreenshot()) != GameState.PLAYING) {
					failures++;
					continue;
				}

				if (tacticManager.getBirdsQueue().isEmpty())
					emptyQueueException();
				else
					tacticManager.setNextBird();

				HexAgent.Log.info("Solve step.");
				state = currentTactic.solve(strategyManager.getCurrentLevel());

				// TODO why we need this??
				tacticManager.setFirstShoot(false);
				tacticManager.initForNextTurn();

				DebugUtils.addTime("played turn", System.nanoTime());

				failures = 0;

			} catch (final WrongScreenshotException e) {
				failures++;
				state = ar.checkState();
				// If the recognition failed again, use the default bird
				if (tacticManager.getCurrentBird() == null)
					tacticManager.setCurrentBird(Tactic.DEFAULT_BIRD_ON_SLINGSHOT);
				HexAgent.Log.severe("screenshot error");
			}
	}

	/**
	 * this method is executed at the beginning of the run method of the agent
	 */
	private void preparation() {
		DebugUtils.initBenchmarkParametersValues();
		DebugUtils.addTime("RunStart", System.nanoTime());
	}

	@Override
	public void run() {
		while (true)
			try {
				initAgent();
				playStrategy();
			} catch (final Exception e) {
				HexAgent.Log.severe("AGENT CRASHED...RESTARTING");
			}
	}

}
