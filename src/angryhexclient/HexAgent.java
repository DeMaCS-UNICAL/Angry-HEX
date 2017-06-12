/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, 2015, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************//*
package angryhexclient;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import angryhexclient.strategy.BenchmarkStrategy;
import angryhexclient.strategy.DeclarativeStrategy;
import angryhexclient.strategy.StrategyManager;
import angryhexclient.util.DebugUtils;
//commented out because it's not used
//import org.opencv.core.RotatedRect;
import ab.demo.other.ClientActionRobot;
import ab.demo.other.Env;

public class HexAgent implements Runnable {
	
	private static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final GameStateExtractor gse = new GameStateExtractor();
	
	private final ABType DEFAULT_BIRD_ON_SLINGSHOT = ABType.BlueBird; 
	
	// focus point
	private int focus_x;
	private int focus_y;
	// Wrapper of the communicating messages
	private HexActionRobot ar;
	
	private TrajectoryPlanner tp;
	
	private StrategyManager strategyManager;
	
	private boolean firstShoot;
	
	private BufferedImage screenshot;

	//commented out because it's not used
//	private Rectangle currentBirdRect;

	public HexAgent(HexActionRobot ar) throws Exception {
		this(ar, (byte)1);
	}

	public HexAgent(HexActionRobot ar, byte startingLevel) throws Exception {
		this.ar = ar;
		this.tp = new TrajectoryPlanner();
		this.firstShoot = true;
		
		byte[] configureData = ar.configure(ClientActionRobot
				.intToByteArray(Configuration.getTeamID()));
		if (BenchmarkStrategy.BENCHMARK) {
			this.strategyManager = new BenchmarkStrategy(ar, startingLevel, configureData);
		} else {
			this.strategyManager = new DeclarativeStrategy(ar, startingLevel, configureData);
		}
		
		DebugUtils.init(this.strategyManager);

		Memory.init();
	}

	*//**
	 * Run the Client (Naive Agent)
	 *//*
	@Override
	public void run() {

		Log.info("Starting client...");

		// load the initial level (default 1)
		strategyManager.loadCurrentLevel();
		
		while (true) {
			
			if (DebugUtils.DEBUG) {
				DebugUtils.initBenchmarkParametersValues();
				DebugUtils.addTime(System.nanoTime());
				DebugUtils.setCurrentLevel(strategyManager.getCurrentLevel());
				DebugUtils.setCurrentTurn(strategyManager.getCurrentTurn());
			}
			
			Log.info("Solve step.");
			GameState state = solve();
			strategyManager.increaseCurrentTurn();
			firstShoot = false;
			
			if (DebugUtils.DEBUG)
				DebugUtils.addTime(System.nanoTime());

			if (state == GameState.WON || state == GameState.LOST) {

				if (BenchmarkStrategy.BENCHMARK) {
					if (state == GameState.WON) {
						int score = gse.getScoreEndGame(ar.doScreenShot());
						strategyManager.updateScore(score);
						Log.fine("WON|score: " + score);
					} else {
						strategyManager.updateScore(0);
						Log.fine("LOST|score: 0");
					}
				}
				
				strategyManager.loadNewLevel(state);

				if (state == GameState.WON && DebugUtils.DEBUG) {
					try {
						strategyManager.saveScores("scores.csv");
					} catch (IOException e) {
						Log.warning("cannot save scores: " + e.getMessage());
					}
					
				}
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();
				firstShoot = true;
			} else if (state == GameState.LEVEL_SELECTION) {
				Log.info("unexpected level selection page, go to the last current level : "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			} else if (state == GameState.MAIN_MENU) {
				Log.info("unexpected main menu page, reload the level : "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			} else if (state == GameState.EPISODE_MENU) {
				Log.info("unexpected episode menu page, reload the level: "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			}
			
			if (DebugUtils.DEBUG) {
				DebugUtils.addTime(System.nanoTime());
				DebugUtils.saveBenchmark();
			}
			
		}
	}

	*//**
	 * Solve a particular level by shooting birds directly to pigs
	 * 
	 * @return GameState: the game state after shots.
	 *//*
	public GameState solve() {

		// capture Image
		screenshot = ar.doScreenShot();
		// process image
                Vision vision = new Vision(screenshot);
		//just for ground
		// OurVision ourVision = new OurVision(screenshot);
		
                Rectangle sling = vision.findSlingshotRealShape();
		ABType currentBird;
		//
		// Tries to use more precision for understanding the current Bird Color
		//
		// ar.clickInCenter();
		ar.fullyZoomIn();
                Vision birdVision = new Vision(ar.doScreenShot());
		Rectangle sling2 = birdVision.findSlingshotRealShape();

		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		//
		// Determines the bird type at hand.
		//
		currentBird = getBirdType(birdVision, sling2);
		//If the first recognition failed, try to recognize the bird again
		if (currentBird == null){
		    ar.fullyZoomOut();
		    try {
			Thread.sleep(1500);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    Log.info("Re-do bird recognition.");
		    ar.fullyZoomIn();
		    birdVision = new Vision(ar.doScreenShot());
		    sling2 = birdVision.findSlingshotRealShape();
		    currentBird = getBirdType(birdVision, sling2);
		    if (currentBird == null){
			currentBird = DEFAULT_BIRD_ON_SLINGSHOT;
		    }
		}
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		// If the level is loaded (in PLAYING state)but no slingshot detected,
		// then the agent will request to fully zoom out.
		Log.info("Zooming out again..");
		ar.fullyZoomOut();
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		while (sling == null && ar.checkState() == GameState.PLAYING) {
			Log.info("no slingshot detected. Please remove pop up or zoom out");
			ar.fullyZoomOut();
			screenshot = ar.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotRealShape();
		}
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		Log.info("Checking state..");
		GameState state = ar.checkState();
		Log.info("State is.." + state.name());
		int tap_time = 0;
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		// if there is a sling, then play, otherwise skip.
		if (sling != null) {
			
			ar.fullyZoomOut();
			
			if (DebugUtils.DEBUG)
				DebugUtils.addTime(System.nanoTime());

			if (!vision.findPigsMBR().isEmpty()) {
				Point releasePoint;
				{
					Target result;

					if (DebugUtils.DEBUG) {
						DebugUtils.setCurrentBird(currentBird.name());
						DebugUtils.addBirds(birdVision.findBirdsMBR());
						DebugUtils.setN_pigs(vision.findPigsMBR().size());
						DebugUtils.addBlocks(vision.findBlocksRealShape());
						DebugUtils.setN_hills(vision.findHills().size());
						DebugUtils.setN_tnt(vision.findTNTs().size());
					}
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					try {
						// invoke reasoner
						result = reason(vision, tp, currentBird);
					}
					// If there is an exception just return the game state,
					// so the shot will be attempted again.
					catch (UnsupportedOperationException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					} catch (IOException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					} catch (InterruptedException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					}
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());

					Point _tpt = result.target;

					Log.info(String.format("TGT: %s TR: %s", _tpt, (result.trajectory == 0) ? "low" : "high"));

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					if (pts.size() == 1) {
						releasePoint = pts.get(0);
					} else {
						releasePoint = pts.get(result.trajectory);
					}
					
					Point refPoint = tp.getReferencePoint(sling);
					// focus_x = (int) currentBirdRect.getCenterX();
					// focus_y = (int) currentBirdRect.getCenterY();
					// Get the center of the active bird as focus point
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(strategyManager.getCurrentLevel())) ? Env
							.getFocuslist()
							.get(strategyManager.getCurrentLevel()).getX()
							: refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(strategyManager.getCurrentLevel())) ? Env
							.getFocuslist()
							.get(strategyManager.getCurrentLevel()).getY()
							: refPoint.y);
					// System.out.print("FX:"+focus_x+" FY:"+focus_y+" RP: " +
					// releasePoint+" ");

					// Get the release point from the trajectory prediction
					// module
					if (releasePoint != null) {
						//commented out because it's not used
//						double releaseAngle = tp.getReleaseAngle(sling,
//								releasePoint);
						//System.out.print("ANG : "
						//		+ Math.toDegrees(releaseAngle)+ " Tap Coeff:"+result.tapCoeff+" ");
						
						// Tap Coefficient now comes from reasoning
						tap_time = getTapTime(_tpt.x, tp, sling,
								releasePoint, result.tapCoeff / 100.0,
								currentBird);
						Log.info("Tap time: " + tap_time);
					} else {
						Log.severe("releasePoint is null!");
					}
				}

				// checks whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					
//					ar.fullyZoomOut();
//					screenshot = ar.doScreenShot();
//					vision = new Vision(screenshot);
//					Rectangle _sling = vision.findSlingshot();
//					if (sling.equals(_sling))
//					{
//
//						// make the shot
//						ar.shoot(focus_x, focus_y, (int) releasePoint.getX()
//								- focus_x, (int) releasePoint.getY() - focus_y,
//								0, tap_time, false);
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					// New method, tries to improve performance by not waiting
					// 15 still not losing shoots.
					quickShoot(focus_x, focus_y, (int) releasePoint.getX()
							- focus_x, (int) releasePoint.getY() - focus_y, 0,
							tap_time, false);
					Log.info("Shot done.");
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					// additional screenshot
					screenshot = ar.doScreenShot();
					// check the state after the shot
					state = ar.checkState();
					//screenshot = ar.doScreenShot();
					// update parameters after a shot is made
					if (state == GameState.PLAYING) {
						vision = new Vision(screenshot);
						List<Point> traj = vision.findTrajPoints();
						tp.adjustTrajectory(traj, sling, releasePoint);
					}
//					else
//						System.out
//								.println("scale is changed, can not execute the shot, will re-segement the image");
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
				}
			}
		}
		return state;
	}

	private void quickShoot(int focus_x2, int focus_y2, int i, int j, int k,
			int tap_time, boolean polar) {
		ar.fastshoot(focus_x2, focus_y2, i, j, k, tap_time, polar);
		BufferedImage screenshot = ar.doScreenShot();
		BufferedImage image = null;
		int waitSeconds = 0;
		while ((image == null)
				|| (waitSeconds <= 15 && VisionUtils.numPixelsDifferent(
						screenshot, image) > Configuration
						.getFastshootThreshold())) {

			// update frame count
			image = screenshot;
			try {
				Thread.sleep(1000);
				waitSeconds++;
			} catch (InterruptedException e) {
				Log.severe(e.getMessage());
			}
			screenshot = ar.doScreenShot_();
		}
	}

	//commented out because it's not used
//	private static final double speedRatio = 3.1115090723;
	//commented out because it's not used
//	private static final double speedRatioX = Configuration.getSpeedRatio();
  
	*//**
	 * This function gets the tap time. It calculates the waiting time needed so
	 * that the bird will be tapped at approximately the position tapX.
	 *//*
	private int getTapTime(int targetX, TrajectoryPlanner tp,
			Rectangle slingshot, Point launchPoint, double fraction, ABType b) {
		//double dist = getDistance(tp, slingshot, launchPoint, targetX);
		Point tapPoint = getAnticipatedPoint(tp,slingshot, launchPoint, targetX, fraction);
		//return (int) ((dist / (tp.getSceneScale(slingshot) * speedRatio)) * 1000);
		//commented out because it's not used
//		double angle = tp.getReleaseAngle(slingshot,launchPoint);
		//
		// Our estimate
		//
		//return (int) (tapPoint.getX() / (Configuration.getSpeedRatioByBird(b) * Math.cos(angle) * tp.getVelocity(angle)) * 10);
		//
		// Organizers estimate
		//
		return tp.getTapTime(slingshot, launchPoint, tapPoint);
		
	}

	private Point getAnticipatedPoint(TrajectoryPlanner tp2, Rectangle slingshot,
			Point launchPoint, int targetX, double fraction) {
		//
		// Follows the parabola perimeter and find the X coordinate closer to the fraction*parabola 
		//
		
		List<Point> ps = tp
				.predictTrajectory(slingshot, launchPoint);
		double dist = 0;
		for (int i = 0; i < ps.size() - 1 && ps.get(i).x < targetX; i++) {
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));
		}
		double targetDist = dist*fraction;
		Log.info("Points:"+ps.size()+" Dist: "+dist+" TargetDist:"+targetDist);
		dist = 0;
		Point lastX = new Point();
		for (int i = 0; i < ps.size() - 1 && dist < targetDist; i++) {
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));

			// FIXME Check if precision is enough without averaging between x and x+1
			lastX = ps.get(i);
		}
		System.out.println("TargetX: "+targetX+" TapX:"+lastX);
		return lastX;
	}

	//commented out because it's not used
//	private double getDistance(TrajectoryPlanner tp, Rectangle slingshot,
//			Point launchPoint, int xPosition) {
//		List<Point> ps = tp
//				.predictTrajectory(slingshot, launchPoint);
//		double dist = 0;
//		for (int i = 0; i < ps.size() - 1 && ps.get(i).x < xPosition; i++) {
//			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
//					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));
//		}
//		return dist;
//	}

	*//**
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
	 *//*
	private Target reason(Vision vision, TrajectoryPlanner tp, ABType currentBird)
			throws UnsupportedOperationException, IOException, InterruptedException {
		Log.info("Reasoning...");
		
		VisionFact visionFacts = new VisionFact(vision, TargetReasoner.getInstance(),tp);
		visionFacts.createFacts(currentBird);

		BufferedImage img = DebugUtils.drawObjectsWithID(screenshot, visionFacts.getABObjects());
		DebugUtils.showImage(img);
		DebugUtils.saveScreenshot(img, String.format("level_%d_%d", (int)strategyManager.getCurrentLevel(), strategyManager.getCurrentTurn()));
		
		// If it's the first shoot it load the targets selected before
		if (firstShoot) {
			// load memory for current level
			List<Point.Double> targets = Memory.load(strategyManager.getCurrentLevel());
			// It finds the objects more similar to the previous targets
			for (Point.Double point : targets) {
				for (int i = 0; i < visionFacts.getABObjects().size(); i++) {
					ABObject rr = visionFacts.getABObject(i);
					if (rr.contains(point)) {
						String ss = String.format(":- target(%s,_).", i);
						TargetReasoner.getInstance().addFact(ss);
						break;
					}
				}
			}
		}
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		Log.info("Calling HEX...");
		// Now we call HEX to evaluate our program.
		TargetReasoner.getInstance().reason(currentBird);
		Log.info("Called HEX");
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());

		// We extract the solution(s) of the program and select a random one.
		// All solutions to the hex program should be reasonably reasonable. No
		// solution should be significantly worse than the others and the best
		// solutions should be included. Therefore we can just select any one of
		// them at random.
		List<TargetReasoner.TargetData> answers = TargetReasoner.getInstance()
				.getTargets();

		if (answers.size() == 0) {
			Log.warning("No answer set found");
			List<ABObject> pigs = vision.findPigsMBR();

			ABObject pig = pigs.get((new Random()).nextInt(pigs.size()));
			return new Target(new Point((int) pig.getCenterX(),
					(int) pig.getCenterY()), (new Random()).nextInt(2),
					80 + (new Random()).nextInt(31));
		}

		TargetReasoner.TargetData answer = answers.get(new Random()
				.nextInt(answers.size()));

		ABObject answerObject = visionFacts.getABObject(answer.id);
		Log.info("///////////");
		Log.info(answer.toString());
		Log.info("///////////");

		// If it's the first shoot it saves on file the target
		if (firstShoot) {
			Memory.store(strategyManager.getCurrentLevel(),
					answerObject.getCenter());
		}
		
		//
		// Builds target point depending on trajectory and offset: right face for low traj, upper face for high traj
		//
//		double x_target = answer.trajectory == 0 ? answerRectangle.center.x - answerRectangle.size.width/2 : answerRectangle.center.x;
//		double y_target = answer.trajectory == 1 ? answerRectangle.center.y + answerRectangle.size.height/2 :  
//			                               Math.max(0,answerRectangle.center.y-answerRectangle.size.height*answer.yoffsetRatio/2);
//		
		double x_target;
		double y_target;
		if (answer.eggMode) {
			x_target = answerObject.getCenterX(); 
			y_target = answer.yoffsetRatio;
		} else {
			x_target = answer.trajectory == 0 ? answerObject.getCenterX() - answerObject.width/2 : answer.yoffsetRatio;
			y_target = answer.trajectory == 1 ? answerObject.getCenterY() - answerObject.height/2 : answer.yoffsetRatio;		
		}
		
		return new Target(new Point((int) x_target, (int) y_target), answer.trajectory, answer.tapCoeff);
	}
	
//	public boolean targetProbablyEquals(RotatedRect r1, RotatedRect r2) {
//
//		boolean equals = false;
//
//		if (Math.sqrt(Math.pow(r1.center.x - r2.center.x, 2)
//				+ Math.pow(r1.center.y - r2.center.y, 2)) < 3)
//			equals = true;
//
//		return equals;
//
//	}

	//
	// Gets bird type, side effect updates currentBird.
	//
	private ABType getBirdType(Vision vision, Rectangle slingshot) {
		ABType type = null;
		try {
			//commented out because it's not used
//			Rectangle closest = null;
			double bestDist = 0;
			List<ABObject> birds = vision.findBirdsMBR();
			// Log.info(b.name() + ":" + birds.size() + " ");

			for (ABObject bird : birds) {
				//
				// Purposely considers the (centerX, minY) coordinate for
				// the slingshot.
				//
				double val = Point2D.distance(bird.getCenterX(),
						bird.getCenterY(), slingshot.getCenterX(),
						slingshot.getY());
				if (val < bestDist || bestDist == 0) {
					type = bird.type;
					bestDist = val;
					//commented out because it's not used
//					closest = bird;
				}
			}
			//commented out because it's not used
//			currentBirdRect = closest;
			Log.info("BT:" + type.name());
		} catch (NullPointerException e) {
			// TODO catching a NullPointerException is very bad practice
			// check if null via 'if'
			Log.severe("No bird type recognized.");
		}
		return type;
	}
	
	*//**
	 * A class that serves as the return value of the reasoning function,
	 * because not only the target point but also the trajectory needs to be
	 * returned.
	 *//*
	class Target {
		*//**
		 * The trajectory, 0 for low, 1 for high.
		 *//*
		public int trajectory;
		*//**
		 * Tap coefficient, percentage variation on the tap time.
		 *//*
		public int tapCoeff;
		*//**
		 * The target point that the bird will be shot at.
		 *//*
		public Point target;
		
		//public double yoffsetRatio;

		public Target(Point ta, int tr, int tap) {
			trajectory = tr;
			target = ta;
			tapCoeff = tap;
			//yoffsetRatio = yoffset;
		}
	}

	//commented out because it's not used
//	private static RotatedRect convertRectangleToRotatedRect(Rectangle r) {
//		return new RotatedRect(new org.opencv.core.Point(r.getCenterX(),
//				r.getCenterY()), new org.opencv.core.Size(r.getSize()
//				.getWidth(), r.getSize().getHeight()), 0);
//	}
}
*/














/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, 2015, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package angryhexclient;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
//commented out because it's not used
//import org.opencv.core.RotatedRect;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.Env;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import angryhexclient.strategy.BenchmarkStrategy;
import angryhexclient.strategy.DeclarativeStrategy;
import angryhexclient.strategy.StrategyManager;
import angryhexclient.util.DebugUtils;

public class HexAgent implements Runnable {
	
	private static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final GameStateExtractor gse = new GameStateExtractor();
	
	private final ABType DEFAULT_BIRD_ON_SLINGSHOT = ABType.BlueBird; 
	
	// focus point
	private int focus_x;
	private int focus_y;
	// Wrapper of the communicating messages
	private HexActionRobot ar;
	
	private TrajectoryPlanner tp;
	
	private StrategyManager strategyManager;
	
	private boolean firstShoot;
	
	private BufferedImage screenshot;

	private Rectangle sling;

	private List<ABObject> birdsQueue;

	//commented out because it's not used
//	private Rectangle currentBirdRect;

	public HexAgent(HexActionRobot ar) throws Exception {
		this(ar, (byte)1);
	}

	public HexAgent(HexActionRobot ar, byte startingLevel) throws Exception {
		this.ar = ar;
		this.tp = new TrajectoryPlanner();
		this.firstShoot = true;
		
		byte[] configureData = ar.configure(ClientActionRobot
				.intToByteArray(Configuration.getTeamID()));
		if (BenchmarkStrategy.BENCHMARK) {
			this.strategyManager = new BenchmarkStrategy(ar, startingLevel, configureData);
		} else {
			this.strategyManager = new DeclarativeStrategy(ar, startingLevel, configureData);
		}
		
		DebugUtils.init(this.strategyManager);

		Memory.init();
	}



	// Get an ordered set of bird types for a given level
	private List<ABObject> getBirdsQueue(BufferedImage scr) {
		
		Log.info("Started birds queue construction");
		// Create visions for both MBR and RealShape algorithms
		Vision vision_mbr = new Vision(scr);	
		Vision vision_rs = new Vision(scr);	
		
		// Create lists for storing the identified birds
		List<ABObject> birds_mbr = new ArrayList<ABObject>();
		List<ABObject> birds_rs = new ArrayList<ABObject>();
		List<ABObject> birds = new ArrayList<ABObject>();
		List<ABObject> ordered_birds = new ArrayList<ABObject>();
		
		// variables for the number of recognized birds 
		int number_of_mbr_birds;
		int number_of_rs_birds;
		
		// flag defining the recognition algorithm to be used 
		boolean mbr=false;
		boolean rs=false;
		
		// By default determine a slingshot using MBR algorithm
		sling = vision_mbr.findSlingshotMBR();
		
		// If the sling was not properly recognized, try various possibilities to still find it
		if (sling == null && ar.checkState() == GameState.PLAYING) {
			BufferedImage screenshot2;
			Log.info("No slingshot was detected by MBR. Please remove pop up or zoom out");
			Log.info("Redo screenshot and try realShape");
			screenshot2 = ar.doScreenShot();
			vision_rs = new Vision(screenshot2);
			sling = vision_rs.findSlingshotRealShape();
			if (sling==null) {
				Log.info("Sling is null, try to zoomIn and run MBR");
				ar.fullyZoomIn();
				screenshot2 = ar.doScreenShot();
				vision_mbr = new Vision(screenshot2);
				sling = vision_mbr.findSlingshotMBR();
			}
			
			if (sling==null) {
				Log.info("Sling is still null, try to zoomIn and run RealShape");
				ar.fullyZoomIn();
				screenshot2 = ar.doScreenShot();
				vision_rs = new Vision(screenshot2);
				sling = vision_rs.findSlingshotRealShape();
			}
		}
		
		// If the sling was recognized, proceed with bird detection 
		if (sling!=null) {
			Log.info("Slingshot's X coordinate is: "+sling.getCenterX());

	      
		// Recognize birds using MBR and RealShape algorithms
        birds_mbr=vision_mbr.findBirdsMBR();		
        birds_rs=vision_rs.findBirdsRealShape();		
		
        
        // Store the number of identified birds
		number_of_mbr_birds=birds_mbr.size();
		number_of_rs_birds=birds_rs.size();
		
		
		Log.info("Found "+number_of_mbr_birds+" mbr birds");
		Log.info("Found "+number_of_rs_birds+" realshape birds");

		
		// Pick the algorithm that recognized more birds
		if (number_of_mbr_birds >= number_of_rs_birds) {
			Log.info("Choose mbr algorithm");
			birds=birds_mbr;
			mbr=true;
		}
		else {
			Log.info("Choose realshape algorithm");
			birds=birds_rs;
			rs=true;
		}
		Log.info("Original birdset");
		for (int i=0; i<birds.size();i++) {
			Log.info(i+": "+birds.get(i).getCenterX()+":  "+birds.get(i).type);
		}
		
		
		// Create an ordered set of bird types, the first bird in the one on the slingshot, the rest are ordered from right to left
	
		
			if (birds.size()>1) {
			
		
			// case when the most left bird is the closest to the slingshot 
			// sort birds based in centerX in the decsending order
				
			Log.info("Sorting birds in descending order..");	
			Collections.sort(birds, new Comparator<ABObject>() {
			    @Override
			    public int compare(ABObject ab1, ABObject ab2) {
			        return Double.compare(ab1.getCenterX(), ab2.getCenterX());
			    }
			});

					
			for (int i=0; i<birds.size();i++) {
				Log.info(i+": "+birds.get(i).getCenterX()+":  "+birds.get(i).type);
			}
			
			Log.info("Detecting the bird on the sling");
			Log.info("The distance between the first bird and the sling is "+Math.abs(birds.get(0).getCenterX()-sling.getCenterX()));
			Log.info("The distance between the last bird and the slingshot is "+Math.abs(birds.get(birds.size()-1).getCenterX()-sling.getCenterX()));


			// Determine the bird that is the closest to the slingshot 
			// Case when the most left bird is the closest to the slinshot:
			
			if (Math.abs(birds.get(0).getCenterX()-sling.getCenterX())<=5) {
				ordered_birds.add(birds.get(0));
				Log.info("The first bird is on the sling" +ordered_birds.get(0));
				for (int i=birds.size()-1;i>0;i--)
					ordered_birds.add(birds.get(i));
				}
			
			// Case when the most right bird is the closest to the slingshot
			else if (Math.abs(sling.getCenterX()-birds.get(birds.size()-1).getCenterX())<=5) {
				ordered_birds.add(birds.get(birds.size()-1));
				Log.info("The last bird is on the sling" +ordered_birds.get(0));
				for (int i=birds.size()-2;i>=0;i--)
					ordered_birds.add(birds.get(i));
			}
			else {Log.info("No sling bird was found");
				if(mbr) {
					Log.info("Check a sling bird among mbr birds");
					for (int i=0;i<birds_rs.size();i++)
						if (Math.abs(sling.getCenterX()-birds_rs.get(i).getCenterX())<=5) {
							ordered_birds.add(birds_rs.get(i));
							Log.info("Birds on sling is "+ordered_birds.get(0));
							i=birds_rs.size();
						}
				}
				else if (rs) {
					Log.info("Check a sling bird among rs birds");
					for (int i=0;i<birds_mbr.size();i++)
						if (Math.abs(sling.getCenterX()-birds_mbr.get(i).getCenterX())<=5) {
							ordered_birds.add(birds_mbr.get(i));
							Log.info("Birds on sling is "+ordered_birds.get(0));
							i=birds_mbr.size();
						}
				}
				for (int i=birds.size()-1;i>0;i--)
					ordered_birds.add(birds.get(i));
					
			}
		}
			
		// Only a single bird was recognized	
		else if (!ordered_birds.isEmpty()) {
			ordered_birds.add(0,birds.get(0));
		}
	}
		
		// The sling has not been recognized; load the new level
		else {
			Log.info("Sling is null");
			// store 0 score for this level and load a new one
			int scor[] = ar.checkMyScore();
			for (int i=0;i<scor.length;i++)
				Log.info("The score of "+i+" is: "+scor[i]);		
		} 


		//Log.info("Ordered birdset: ");

		//for (int i=0;i<ordered_birds.size();i++)
		//	Log.info(i+": "+ordered_birds.get(i).getCenterX()+": "+ordered_birds.get(i).type);
		return ordered_birds;
	}
	


	/**
	 * Run the Client (Naive Agent)
	 */
	public void run() {
		Log.info("Loading level number " + strategyManager.getCurrentLevel());
		// Load the initial level (default 1)
		strategyManager.loadCurrentLevel();
		
		// Set first shoot to true, as at the beginning of the game no shoots have yet been performed
		firstShoot=true;
		
		while (true) {
			
			if (DebugUtils.DEBUG) {
				DebugUtils.initBenchmarkParametersValues();
				DebugUtils.addTime(System.nanoTime());
				DebugUtils.setCurrentLevel(strategyManager.getCurrentLevel());
				DebugUtils.setCurrentTurn(strategyManager.getCurrentTurn());
			}
			
			// If no shoots were done for this level, create the birds queue
			if (firstShoot) {
				boolean hopelessLevel = true;
				sling = null;
				while ((sling==null)&&(hopelessLevel)) {
					Log.info("Making screenshot..");
					screenshot=ar.doScreenShot();	
					Log.info("Building birds queue..");
					birdsQueue=getBirdsQueue(screenshot);
					if (sling==null) {
						hopelessLevel = true;
						strategyManager.loadNewLevel(GameState.LOST);
					}
					else 
						hopelessLevel=false;
				}
			}

			// Solve the level
			Log.info("First shoot is "+firstShoot);
			Log.info("Solve step.");
			GameState state = solve();
			
			// Increase the turn
			strategyManager.increaseCurrentTurn();
			
			// Shoot must have been performed at this point
			firstShoot = false;

			
			if (DebugUtils.DEBUG)
				DebugUtils.addTime(System.nanoTime());

			// If the level was processed completely, i.e. either success or fail, then update the scores according to the strategy
			if (state == GameState.WON || state == GameState.LOST) {

				if (BenchmarkStrategy.BENCHMARK) {
					if (state == GameState.WON) {
						int score = gse.getScoreEndGame(ar.doScreenShot());
						strategyManager.updateScore(score);
						Log.fine("WON|score: " + score);
					} else {
						strategyManager.updateScore(0);
						Log.fine("LOST|score: 0");
					}
				}
				
				// Load a (new) level depending on the current state
				strategyManager.loadNewLevel(state);

				// Store the scores in the csv file if we are in the debug mode
				if (state == GameState.WON && DebugUtils.DEBUG) {
					try {
						strategyManager.saveScores("scores.csv");
					} catch (IOException e) {
						Log.warning("cannot save scores: " + e.getMessage());
					}
					
				}
				// Make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();
				firstShoot = true;
				
			} else if (state == GameState.LEVEL_SELECTION) {
				Log.info("unexpected level selection page, go to the last current level : "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			} else if (state == GameState.MAIN_MENU) {
				Log.info("unexpected main menu page, reload the level : "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			} else if (state == GameState.EPISODE_MENU) {
				Log.info("unexpected episode menu page, reload the level: "
						+ strategyManager.getCurrentLevel());
				ar.loadLevel(strategyManager.getCurrentLevel());

				firstShoot = true;
			}
			
			if (DebugUtils.DEBUG) {
				DebugUtils.addTime(System.nanoTime());
				DebugUtils.saveBenchmark();
			}
			
		}
	}

	/**
	 * Solve a particular level by shooting birds directly to pigs
	 * 
	 * @return GameState: the game state after shots.
	 */
	public GameState solve() {

		ABType currentBird;
		
		// capture Image
		screenshot = ar.doScreenShot();
		// process image
        		Vision vision = new Vision(screenshot);                


		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		//
		// Set the current bird (the first in the birdsQueue)
		//

		// If the queue is empty, then try to do recognition of the bird on a slingshot again 
		if (birdsQueue.size()==0||birdsQueue.get(0).type==null) {
			ar.fullyZoomIn();
			if (sling==null)
				sling = vision.findSlingshotRealShape();
			
		    	currentBird = getBirdType(vision, sling);		
				ar.fullyZoomOut();
		}
		else 
			currentBird = birdsQueue.get(0).type;

		// If the recognition failed again, use the default bird
		if (currentBird == null){
			currentBird = DEFAULT_BIRD_ON_SLINGSHOT;
		}
		
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());		
		
		Log.info("Checking state..");
		GameState state = ar.checkState();
		Log.info("State is.." + state.name());
		int tap_time = 0;
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		// If there is a sling, then play, otherwise skip.
		if (sling != null) {
					
			if (DebugUtils.DEBUG)
				DebugUtils.addTime(System.nanoTime());

			if (!vision.findPigsMBR().isEmpty()) {
				Point releasePoint;
				{
					Target result;

					if (DebugUtils.DEBUG) {
						DebugUtils.setCurrentBird(currentBird.name());
						DebugUtils.addBirds(birdsQueue);
						DebugUtils.setN_pigs(vision.findPigsMBR().size());
						DebugUtils.addBlocks(vision.findBlocksRealShape());
						DebugUtils.setN_hills(vision.findHills().size());
						DebugUtils.setN_tnt(vision.findTNTs().size());
					}
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					try {
						// invoke reasoner
						result = reason(vision, tp, currentBird);
					}
					// If there is an exception just return the game state,
					// so the shot will be attempted again.
					catch (UnsupportedOperationException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					} catch (IOException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					} catch (InterruptedException e) {
						Log.severe("error calling reasoner: " + e.getMessage());
						return ar.checkState();
					}
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());

					Point _tpt = result.target;

					Log.info(String.format("TGT: %s TR: %s", _tpt, (result.trajectory == 0) ? "low" : "high"));

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					if (pts.size() == 1) {
						releasePoint = pts.get(0);
					} else {
						releasePoint = pts.get(result.trajectory);
					}
					
					Point refPoint = tp.getReferencePoint(sling);
					 
					 //focus_x = (int) currentBirdRect.getCenterX();
					 //focus_y = (int) currentBirdRect.getCenterY();
					// Get the center of the active bird as focus point
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(strategyManager.getCurrentLevel())) ? Env
							.getFocuslist()
							.get(strategyManager.getCurrentLevel()).getX()
							: refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(strategyManager.getCurrentLevel())) ? Env
							.getFocuslist()
							.get(strategyManager.getCurrentLevel()).getY()
							: refPoint.y);
					

					// System.out.print("FX:"+focus_x+" FY:"+focus_y+" RP: " +
					// releasePoint+" ");

					// Get the release point from the trajectory prediction
					// module
					if (releasePoint != null) {
						//commented out because it's not used
//						double releaseAngle = tp.getReleaseAngle(sling,
//								releasePoint);
						//System.out.print("ANG : "
						//		+ Math.toDegrees(releaseAngle)+ " Tap Coeff:"+result.tapCoeff+" ");
						
						// Tap Coefficient now comes from reasoning
						tap_time = getTapTime((int) _tpt.x, tp, sling,
								releasePoint, result.tapCoeff / 100.0,
								currentBird);
						Log.info("Tap time: " + tap_time);
					} else {
						Log.severe("releasePoint is null!");
					}
				}

				// checks whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					
//					ar.fullyZoomOut();
//					screenshot = ar.doScreenShot();
//					vision = new Vision(screenshot);
//					Rectangle _sling = vision.findSlingshot();
//					if (sling.equals(_sling))
//					{
//
//						// make the shot
//						ar.shoot(focus_x, focus_y, (int) releasePoint.getX()
//								- focus_x, (int) releasePoint.getY() - focus_y,
//								0, tap_time, false);
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					// New method, tries to improve performance by not waiting
					// 15 still not losing shoots.
					String par1 = focus_x+"";
					String par2 = focus_y+"";
					String par3 = (int) releasePoint.getX()- focus_x+"";
					String par4 = (int) releasePoint.getY()- focus_y+"";
					String par5 = 0+"";
					String par6 = tap_time+"";
					String par7 = false+"";
					
					quickShoot(focus_x, focus_y, (int) releasePoint.getX()
							- focus_x, (int) releasePoint.getY() - focus_y, 0,
							tap_time, false);
					Log.info("Shot done.");
					Log.info("Parameters  of the shot: "+par1+"; "+par2+"; "+par3+"; "+par4+"; "+par5+"; "+par6+"; "+par7);
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					// additional screenshot
					screenshot = ar.doScreenShot();
					Log.info("Doing a screenshot.");
					// check the state after the shot
					state = ar.checkState();
					Log.info("The state is "+state);
					//screenshot = ar.doScreenShot();
					// update parameters after a shot is made
					if (state == GameState.PLAYING) {
						vision = new Vision(screenshot);
						List<Point> traj = vision.findTrajPoints();
						tp.adjustTrajectory(traj, sling, releasePoint);
					}
//					else
//						System.out
//								.println("scale is changed, can not execute the shot, will re-segement the image");
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
				}
			}
		}
		else {
			Log.info("Sling was not recognized, skipping the level");
			//quickShoot(193, 328, -625, 209, 0, 1081, false);
			
		}
			
		return state;
	}

	private void quickShoot(int focus_x2, int focus_y2, int i, int j, int k,
			int tap_time, boolean polar) {
		ar.fastshoot(focus_x2, focus_y2, i, j, k, tap_time, polar);
		BufferedImage screenshot = ar.doScreenShot();
		BufferedImage image = null;
		int waitSeconds = 0;
		while ((image == null)
				|| (waitSeconds <= 15 && VisionUtils.numPixelsDifferent(
						screenshot, image) > Configuration
						.getFastshootThreshold())) {

			// update frame count
			image = screenshot;
			try {
				Thread.sleep(1000);
				waitSeconds++;
			} catch (InterruptedException e) {
				Log.severe(e.getMessage());
			}
			screenshot = ar.doScreenShot_();
		}
	}

	//commented out because it's not used
//	private static final double speedRatio = 3.1115090723;
	//commented out because it's not used
//	private static final double speedRatioX = Configuration.getSpeedRatio();
  
	/**
	 * This function gets the tap time. It calculates the waiting time needed so
	 * that the bird will be tapped at approximately the position tapX.
	 */
	private int getTapTime(int targetX, TrajectoryPlanner tp,
			Rectangle slingshot, Point launchPoint, double fraction, ABType b) {
		//double dist = getDistance(tp, slingshot, launchPoint, targetX);
		Point tapPoint = getAnticipatedPoint(tp,slingshot, launchPoint, targetX, fraction);
		//return (int) ((dist / (tp.getSceneScale(slingshot) * speedRatio)) * 1000);
		//commented out because it's not used
//		double angle = tp.getReleaseAngle(slingshot,launchPoint);
		//
		// Our estimate
		//
		//return (int) (tapPoint.getX() / (Configuration.getSpeedRatioByBird(b) * Math.cos(angle) * tp.getVelocity(angle)) * 10);
		//
		// Organizers estimate
		//
		return tp.getTapTime(slingshot, launchPoint, tapPoint);
		
	}

	private Point getAnticipatedPoint(TrajectoryPlanner tp2, Rectangle slingshot,
			Point launchPoint, int targetX, double fraction) {
		//
		// Follows the parabola perimeter and find the X coordinate closer to the fraction*parabola 
		//
		
		List<Point> ps = tp
				.predictTrajectory(slingshot, launchPoint);
		double dist = 0;
		for (int i = 0; i < ps.size() - 1 && ps.get(i).x < targetX; i++) {
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));
		}
		double targetDist = dist*fraction;
		Log.info("Points:"+ps.size()+" Dist: "+dist+" TargetDist:"+targetDist);
		dist = 0;
		Point lastX = new Point();
		for (int i = 0; i < ps.size() - 1 && dist < targetDist; i++) {
			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));

			// FIXME Check if precision is enough without averaging between x and x+1
			lastX = ps.get(i);
		}
		System.out.println("TargetX: "+targetX+" TapX:"+lastX);
		return lastX;
	}

	//commented out because it's not used
//	private double getDistance(TrajectoryPlanner tp, Rectangle slingshot,
//			Point launchPoint, int xPosition) {
//		List<Point> ps = tp
//				.predictTrajectory(slingshot, launchPoint);
//		double dist = 0;
//		for (int i = 0; i < ps.size() - 1 && ps.get(i).x < xPosition; i++) {
//			dist += Math.sqrt(Math.pow(ps.get(i).x - ps.get(i + 1).x, 2)
//					+ Math.pow(ps.get(i).y - ps.get(i + 1).y, 2));
//		}
//		return dist;
//	}

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
	private Target reason(Vision vision, TrajectoryPlanner tp, ABType currentBird)
			throws UnsupportedOperationException, IOException, InterruptedException {
		Log.info("Reasoning...");
		
		VisionFact visionFacts = new VisionFact(vision, TargetReasoner.getInstance(),tp);
		visionFacts.createFacts(currentBird);

		BufferedImage img = DebugUtils.drawObjectsWithID(screenshot, visionFacts.getABObjects());
		DebugUtils.showImage(img);
		DebugUtils.saveScreenshot(img, String.format("level_%d_%d", (int)strategyManager.getCurrentLevel(), strategyManager.getCurrentTurn()));
		
		// If it's the first shoot it load the targets selected before
		if (firstShoot) {
			// load memory for current level
			List<Point.Double> targets = Memory.load(strategyManager.getCurrentLevel());
			// It finds the objects more similar to the previous targets
			for (Point.Double point : targets) {
				for (int i = 0; i < visionFacts.getABObjects().size(); i++) {
					ABObject rr = visionFacts.getABObject(i);
					if (rr.contains(point)) {
						String ss = String.format(":- target(%s,_).", i);
						TargetReasoner.getInstance().addFact(ss);
						break;
					}
				}
			}
		}
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());
		
		Log.info("Calling HEX...");
		// Now we call HEX to evaluate our program.
		TargetReasoner.getInstance().reason(currentBird);
		Log.info("Called HEX");
		
		if (DebugUtils.DEBUG)
			DebugUtils.addTime(System.nanoTime());

		// We extract the solution(s) of the program and select a random one.
		// All solutions to the hex program should be reasonably reasonable. No
		// solution should be significantly worse than the others and the best
		// solutions should be included. Therefore we can just select any one of
		// them at random.
		List<TargetReasoner.TargetData> answers = TargetReasoner.getInstance()
				.getTargets();

		if (answers.size() == 0) {
			Log.warning("No answer set found");
			List<ABObject> pigs = vision.findPigsMBR();

			ABObject pig = pigs.get((new Random()).nextInt(pigs.size()));
			return new Target(new Point((int) pig.getCenterX(),
					(int) pig.getCenterY()), (new Random()).nextInt(2),
					80 + (new Random()).nextInt(31));
		}

		TargetReasoner.TargetData answer = answers.get(new Random()
				.nextInt(answers.size()));

		ABObject answerObject = visionFacts.getABObject(answer.id);
		Log.info("///////////");
		Log.info(answer.toString());
		Log.info("///////////");

		// If it's the first shoot it saves on file the target
		if (firstShoot) {
			Memory.store(strategyManager.getCurrentLevel(),
					answerObject.getCenter());
		}
		
		//
		// Builds target point depending on trajectory and offset: right face for low traj, upper face for high traj
		//
//		double x_target = answer.trajectory == 0 ? answerRectangle.center.x - answerRectangle.size.width/2 : answerRectangle.center.x;
//		double y_target = answer.trajectory == 1 ? answerRectangle.center.y + answerRectangle.size.height/2 :  
//			                               Math.max(0,answerRectangle.center.y-answerRectangle.size.height*answer.yoffsetRatio/2);
//		
		double x_target;
		double y_target;
		if (answer.eggMode) {
			x_target = answerObject.getCenterX(); 
			y_target = answer.yoffsetRatio;
		} else {
			x_target = answer.trajectory == 0 ? answerObject.getCenterX() - answerObject.width/2 : answer.yoffsetRatio;
			y_target = answer.trajectory == 1 ? answerObject.getCenterY() - answerObject.height/2 : answer.yoffsetRatio;		
		}
		
		return new Target(new Point((int) x_target, (int) y_target), answer.trajectory, answer.tapCoeff);
	}
	
//	public boolean targetProbablyEquals(RotatedRect r1, RotatedRect r2) {
//
//		boolean equals = false;
//
//		if (Math.sqrt(Math.pow(r1.center.x - r2.center.x, 2)
//				+ Math.pow(r1.center.y - r2.center.y, 2)) < 3)
//			equals = true;
//
//		return equals;
//
//	}

	//
	// Gets bird type, side effect updates currentBird.
	//
	private ABType getBirdType(Vision vision, Rectangle slingshot) {
		ABType type = null;
		try {
			//commented out because it's not used
//			Rectangle closest = null;
			double bestDist = 0;
			List<ABObject> birds = vision.findBirdsMBR();
			// Log.info(b.name() + ":" + birds.size() + " ");

			for (ABObject bird : birds) {
				//
				// Purposely considers the (centerX, minY) coordinate for
				// the slingshot.
				//
				double val = Point2D.distance(bird.getCenterX(),
						bird.getCenterY(), slingshot.getCenterX(),
						slingshot.getY());
				if (val < bestDist || bestDist == 0) {
					type = bird.type;
					bestDist = val;
					//commented out because it's not used
//					closest = bird;
				}
			}
			//commented out because it's not used
//			currentBirdRect = closest;
			Log.info("BT:" + type.name());
		} catch (NullPointerException e) {
			// TODO catching a NullPointerException is very bad practice
			// check if null via 'if'
			Log.severe("No bird type recognized.");
		}
		return type;
	}
	
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
		
		//public double yoffsetRatio;

		public Target(Point ta, int tr, int tap) {
			trajectory = tr;
			target = ta;
			tapCoeff = tap;
			//yoffsetRatio = yoffset;
		}
	}

	//commented out because it's not used
//	private static RotatedRect convertRectangleToRotatedRect(Rectangle r) {
//		return new RotatedRect(new org.opencv.core.Point(r.getCenterX(),
//				r.getCenterY()), new org.opencv.core.Size(r.getSize()
//				.getWidth(), r.getSize().getHeight()), 0);
//	}
}












