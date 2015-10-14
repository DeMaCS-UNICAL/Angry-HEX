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

	/**
	 * Run the Client (Naive Agent)
	 */
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

	/**
	 * Solve a particular level by shooting birds directly to pigs
	 * 
	 * @return GameState: the game state after shots.
	 */
	public GameState solve() {

		// capture Image
		screenshot = ar.doScreenShot();
		// process image
                Vision vision = new Vision(screenshot);
		//just for ground
		OurVision ourVision = new OurVision(screenshot);
		
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
						DebugUtils.setN_hills(ourVision.detectGround().size());
						DebugUtils.setN_tnt(vision.findTNTs().size());
					}
					
					if (DebugUtils.DEBUG)
						DebugUtils.addTime(System.nanoTime());
					
					try {
						// invoke reasoner
						result = reason(vision, ourVision, tp, currentBird);
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
	private Target reason(Vision vision, OurVision ourVision, TrajectoryPlanner tp, ABType currentBird)
			throws UnsupportedOperationException, IOException, InterruptedException {
		Log.info("Reasoning...");
		
		VisionFact visionFacts = new VisionFact(vision, ourVision, TargetReasoner.getInstance(),tp);
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
