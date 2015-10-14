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
package angryhexclient.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.VisionMBR;
import ab.vision.VisionRealShape;
import ab.vision.VisionUtils;
import angryhexclient.Configuration;
import angryhexclient.strategy.StrategyManager;

public class DebugUtils {
	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	final public static String DEBUG_DIR = System.getProperty("user.dir")
			+ File.separator + "debugutils";
	final public static String SCREENSHOT_DIR = DEBUG_DIR + File.separator
			+ "screenshots" + File.separator;
	final public static String HEX_DIR = DEBUG_DIR + File.separator + "hex"
			+ File.separator;
	final public static String BENCHMARK_FILE = DEBUG_DIR + File.separator + "benchmark.csv";

	final public static boolean DEBUG;

	private static ImageSegFrame imgFrame;
	
	private static StrategyManager strategyManager;

	private static byte currentLevel;
	private static int currentTurn;
	private static List<Long> timeList = new LinkedList<Long>();
	private static String currentBird;
	private static int n_redBirds;
	private static int n_yellowBirds;
	private static int n_blueBirds;
	private static int n_blackBirds;
	private static int n_whiteBirds;
	private static int n_pigs;
	private static int n_ice;
	private static int n_stone;
	private static int n_wood;
	private static int n_hills;
	private static int n_tnt;
	private static int n_unknown;

	static {
		if (Configuration.isDebugMode()) {
			DEBUG = true;
			try {
				Utils.deleteDir(DEBUG_DIR);
				Utils.createDir(DEBUG_DIR);
				Utils.createDir(SCREENSHOT_DIR);
				Utils.createDir(HEX_DIR);
			} catch (IOException e) {
				Log.warning("cannot create " + DEBUG_DIR);
			} catch (InterruptedException e) {
				Log.warning("cannot create " + DEBUG_DIR);
			}
		} else {
			DEBUG = false;
		}
		
		PrintWriter log = null;
		try {
			log = new PrintWriter(new FileWriter(BENCHMARK_FILE, true));
			log.print("Level;");
			log.print("Turn;");
			log.print("Current Bird;");
			log.print("Number of Red Birds;");
			log.print("Number of Yellow Birds;");
			log.print("Number of Blue Birds;");
			log.print("Number of Black Birds;");
			log.print("Number of White Birds;");
			log.print("Number of Birds;");
			log.print("Number of Pigs;");
			log.print("Number of Ice Blocks;");
			log.print("Number of Stone Blocks;");
			log.print("Number of Wood Blocks;");
			log.print("Number of Hills (ex Ground) objects;");
			log.print("Number of TNT objects;");
			log.print("Number of Unknown objects;");
			log.print("Estimated Total Time (nanoseconds);");
			log.println();
		} catch (IOException e) {
			Log.warning("cannot write " + BENCHMARK_FILE);
		} finally {
			if (log != null)
				log.close();
		}

	}
	
	public static void init(StrategyManager smanager){
	    System.out.println("Init");
	    strategyManager = smanager;
	}
	
	public static void saveScreenshot(BufferedImage img, String fileName) {
		if (!DEBUG) {
			return;
		}
		
		String file = SCREENSHOT_DIR + fileName + ".png";
		try {
			File dest = new File(file);
			ImageIO.write(img, "png", dest);
			Log.fine("saved " + dest.getName());
		} catch (IOException e) {
			Log.warning("cannot save " + file);
		}
	}

	public static void saveScreenshot(String file) {
		if (!DEBUG) {
			return;
		}
		File src = new File(file);
		File dest = new File(SCREENSHOT_DIR + src.getName());
		try {
			Utils.copy(src, dest);
			Log.fine("saved " + dest.getName());
		} catch (IOException e) {
			Log.warning("cannot copy " + file);
		}
	}

	public static void saveHex(String file) {
		if (!DEBUG) {
			return;
		}
		File src = new File(file);
		File dest = new File(HEX_DIR + src.getName());
		try {
			Utils.copy(src, dest);
			Log.fine("saved " + dest.getName());
		} catch (IOException e) {
			Log.warning("cannot copy " + file);
		}
	}
	
	public static void saveHexWithInfo (String originalFile, String reasoner){
	    if (!DEBUG){
		return;
	    }
	    File src = new File(originalFile);
	    File dest = new File(HEX_DIR + 
		String.format("level[%s]%d_%d_#%d.hex",reasoner,(int)strategyManager.getCurrentLevel(),strategyManager.getCurrentTurn(),
				strategyManager.getHowManyTimesCurrentLevel()));
	    try {
		    Utils.copy(src, dest);
		    Log.fine("saved " + dest.getName());
	    } catch (IOException e) {
		    Log.warning("cannot copy " + originalFile);
	    }
	}
	

	public static void showImage(BufferedImage img) {
		if (!DEBUG) {
			return;
		}
		if (imgFrame == null) {
			imgFrame = new ImageSegFrame("Rotated rectangles.", img);
		}
		imgFrame.refresh(img);
	}

	public static BufferedImage drawMBRs(BufferedImage img) {
		if (!DEBUG) {
			return img;
		}
		VisionMBR v = new VisionMBR(img);
		img = VisionUtils.convert2grey(img);
		VisionUtils.drawBoundingBoxes(img, v.findPigsMBR(), Color.GREEN);
		VisionUtils.drawBoundingBoxes(img, v.findRedBirdsMBRs(), Color.RED);
		VisionUtils.drawBoundingBoxes(img, v.findBlueBirdsMBRs(), Color.BLUE);
		VisionUtils.drawBoundingBoxes(img, v.findYellowBirdsMBRs(),
				Color.YELLOW);
		VisionUtils.drawBoundingBoxes(img, v.findWoodMBR(), Color.WHITE,
				Color.ORANGE);
		VisionUtils.drawBoundingBoxes(img, v.findStonesMBR(), Color.WHITE,
				Color.GRAY);
		VisionUtils.drawBoundingBoxes(img, v.findIceMBR(), Color.WHITE,
				Color.CYAN);
		VisionUtils.drawBoundingBoxes(img, v.findWhiteBirdsMBRs(), Color.WHITE,
				Color.lightGray);
		VisionUtils.drawBoundingBoxes(img, v.findTNTsMBR(), Color.WHITE,
				Color.PINK);
		VisionUtils.drawBoundingBoxes(img, v.findBlackBirdsMBRs(), Color.BLACK);

		Rectangle sling = v.findSlingshotMBR();
		if (sling != null) {
			VisionUtils.drawBoundingBox(img, sling, Color.ORANGE, Color.BLACK);
		}
		return img;
	}

	public static BufferedImage drawRealShapes(BufferedImage img) {
		if (!DEBUG) {
			return img;
		}
		BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics g = copy.createGraphics();
		g.drawImage(img, 0, 0, null);
		VisionRealShape v = new VisionRealShape(copy);
		v.findSling();
		v.findPigs();
		v.findBirds();
		v.findObjects();
		v.findHills();
		v.findTrajectory();
		v.drawObjects(copy, false);
		return copy;
	}

	public static BufferedImage drawObjectsWithID(BufferedImage image,
			List<ABObject> objects) {
		if (!DEBUG) {
			return image;
		}
		BufferedImage imgMBR = VisionUtils.convert2grey(image);
		VisionUtils.drawBoundingBoxesWithID(imgMBR, objects, Color.WHITE);
		BufferedImage imgRS = drawRealShapes(image);

		BufferedImage img = new BufferedImage(imgMBR.getWidth(),
				imgMBR.getHeight() + imgRS.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();

		g.drawImage(imgMBR, 0, 0, null);
		g.drawImage(imgRS, 0, imgMBR.getHeight(), null);
		// TODO maybe add MBR image as well
		return img;
	}

	public static void initBenchmarkParametersValues() {
		currentLevel = 0;
		currentTurn = 0;
		timeList.clear();
		currentBird = "";
		n_redBirds = 0;
		n_yellowBirds = 0;
		n_blueBirds = 0;
		n_blackBirds = 0;
		n_whiteBirds = 0;
		n_pigs = 0;
		n_ice = 0;
		n_stone = 0;
		n_wood = 0;
		n_hills = 0;
		n_tnt = 0;
		n_unknown = 0;
	}
	
	/**
	 * Save some useful benchmark parameters in a file
	 */
	public static void saveBenchmark() {
		
		PrintWriter log = null;
		try {
			log = new PrintWriter(new FileWriter(BENCHMARK_FILE, true));
			log.print(currentLevel + ";");
			log.print(currentTurn + ";");
			log.print(currentBird + ";");
			log.print(n_redBirds + ";");
			log.print(n_yellowBirds + ";");
			log.print(n_blueBirds + ";");
			log.print(n_blackBirds + ";");
			log.print(n_whiteBirds + ";");
			log.print((n_redBirds + n_yellowBirds + n_blueBirds + n_blackBirds + n_whiteBirds) + ";");
			log.print(n_pigs + ";");
			log.print(n_ice + ";");
			log.print(n_stone + ";");
			log.print(n_wood + ";");
			log.print(n_hills + ";");
			log.print(n_tnt + ";");
			log.print(n_unknown + ";");
			log.print((timeList.get(timeList.size() - 1)- timeList.get(0)) + ";");
			Long tempTime = null;
			for (Long time : timeList)
				if (tempTime == null)
					tempTime = time;
				else {
					log.print((time - tempTime) + ";");
					tempTime = time;
				}
			log.println();
		} catch (IOException e) {
			Log.warning("cannot write " + BENCHMARK_FILE);
		} finally {
			if (log != null)
				log.close();
		}
		
	}
	
	/**
	 * @param currentLevel the currentLevel to set
	 */
	public static void setCurrentLevel(byte currentLevel) {
		DebugUtils.currentLevel = currentLevel;
	}

	/**
	 * @param currentTurn the currentTurn to set
	 */
	public static void setCurrentTurn(int currentTurn) {
		DebugUtils.currentTurn = currentTurn;
	}

	/**
	 * @param currentBird the currentBird to set
	 */
	public static void setCurrentBird(String currentBird) {
		DebugUtils.currentBird = currentBird;
	}

	/**
	 * @param n_pigs the n_pigs to set
	 */
	public static void setN_pigs(int n_pigs) {
		DebugUtils.n_pigs = n_pigs;
	}

	/**
	 * @param n_tnt the n_tnt to set
	 */
	public static void setN_tnt(int n_tnt) {
		DebugUtils.n_tnt = n_tnt;
	}

	/**
	 * @param n_hills the n_hills to set
	 */
	public static void setN_hills(int n_hills) {
		DebugUtils.n_hills = n_hills;
	}

	public static void addBirds(List<ABObject> birds) {
		for(ABObject bird : birds)
			switch(bird.getType()) {
			case RedBird:
				n_redBirds++;
				break;
			case YellowBird:
				n_yellowBirds++;
				break;
			case BlueBird:
				n_blueBirds++;
				break;
			case BlackBird:
				n_blackBirds++;
				break;
			case WhiteBird:
				n_whiteBirds++;
				break;
			default:
				break;
			}
			
	}

	public static void addBlocks(List<ABObject> bloks) {
		for(ABObject block : bloks)
			switch(block.getType()) {
			case Ice:
				n_ice++;
				break;
			case Stone:
				n_stone++;
				break;
			case Wood:
				n_wood++;
				break;
			case Unknown:
				n_unknown++;
			default:
				break;
			}
		
	}

	public static void addTime(long nanoTime) {
		timeList.add(nanoTime);
	}
	
}
