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
import java.util.Queue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import ab.utils.ImageSegFrame;
import ab.vision.ABObject;
import ab.vision.VisionMBR;
import ab.vision.VisionRealShape;
import ab.vision.VisionUtils;
import angryhexclient.Configuration;
import angryhexclient.strategy.StrategyManager;
import angryhexclient.tactic.TacticManager;

public class DebugUtils {
	// class for storing pairs of nanotime/info
	private static class TimePair {
		public String info;
		public long stamp;

		public TimePair(final String info_, final long stamp_) {
			info = info_;
			stamp = stamp_;
		}

	}

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	final public static String DEBUG_DIR = System.getProperty("user.dir") + File.separator + "debugutils";
	final public static String SCREENSHOT_DIR = DebugUtils.DEBUG_DIR + File.separator + "screenshots" + File.separator;
	final public static String HEX_DIR = DebugUtils.DEBUG_DIR + File.separator + "hex" + File.separator;
	final public static String BENCHMARK_FILE = DebugUtils.DEBUG_DIR + File.separator + "benchmark.csv";

	final public static boolean DEBUG;

	private static ImageSegFrame imgFrame;

	private static StrategyManager strategyManager;
	private static TacticManager tacticManager;

	private static byte currentLevel;
	private static int currentTurn;
	private static List<TimePair> timeList = new LinkedList<>();
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
				Utils.deleteDir(DebugUtils.DEBUG_DIR);
				Utils.createDir(DebugUtils.DEBUG_DIR);
				Utils.createDir(DebugUtils.SCREENSHOT_DIR);
				Utils.createDir(DebugUtils.HEX_DIR);
			} catch (final IOException e) {
				DebugUtils.Log.warning("cannot create " + DebugUtils.DEBUG_DIR);
			} catch (final InterruptedException e) {
				DebugUtils.Log.warning("cannot create " + DebugUtils.DEBUG_DIR);
			}
		} else
			DEBUG = false;

		PrintWriter log = null;
		try {
			log = new PrintWriter(new FileWriter(DebugUtils.BENCHMARK_FILE, true));
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
		} catch (final IOException e) {
			DebugUtils.Log.warning("cannot write " + DebugUtils.BENCHMARK_FILE);
		} finally {
			if (log != null)
				log.close();
		}

	}

	public static void addBirds(final Queue<ABObject> queue) {
		if (DebugUtils.DEBUG)
			for (final ABObject bird : queue)
				switch (bird.getType()) {
				case RedBird:
					DebugUtils.n_redBirds++;
					break;
				case YellowBird:
					DebugUtils.n_yellowBirds++;
					break;
				case BlueBird:
					DebugUtils.n_blueBirds++;
					break;
				case BlackBird:
					DebugUtils.n_blackBirds++;
					break;
				case WhiteBird:
					DebugUtils.n_whiteBirds++;
					break;
				default:
					break;
				}

	}

	public static void addBlocks(final List<ABObject> bloks) {

		if (DebugUtils.DEBUG)
			for (final ABObject block : bloks)
				switch (block.getType()) {
				case Ice:
					DebugUtils.n_ice++;
					break;
				case Stone:
					DebugUtils.n_stone++;
					break;
				case Wood:
					DebugUtils.n_wood++;
					break;
				case Unknown:
					DebugUtils.n_unknown++;
				default:
					break;
				}

	}

	public static void addTime(final long nanoTime) {
		if (DebugUtils.DEBUG)
			DebugUtils.timeList.add(new TimePair("unspecified", nanoTime));
	}

	public static void addTime(final String info, final long nanoTime) {
		if (DebugUtils.DEBUG)
			DebugUtils.timeList.add(new TimePair(info, nanoTime));
	}

	public static BufferedImage drawMBRs(BufferedImage img) {
		if (!DebugUtils.DEBUG)
			return img;
		final VisionMBR v = new VisionMBR(img);
		img = VisionUtils.convert2grey(img);
		VisionUtils.drawBoundingBoxes(img, v.findPigsMBR(), Color.GREEN);
		VisionUtils.drawBoundingBoxes(img, v.findRedBirdsMBRs(), Color.RED);
		VisionUtils.drawBoundingBoxes(img, v.findBlueBirdsMBRs(), Color.BLUE);
		VisionUtils.drawBoundingBoxes(img, v.findYellowBirdsMBRs(), Color.YELLOW);
		VisionUtils.drawBoundingBoxes(img, v.findWoodMBR(), Color.WHITE, Color.ORANGE);
		VisionUtils.drawBoundingBoxes(img, v.findStonesMBR(), Color.WHITE, Color.GRAY);
		VisionUtils.drawBoundingBoxes(img, v.findIceMBR(), Color.WHITE, Color.CYAN);
		VisionUtils.drawBoundingBoxes(img, v.findWhiteBirdsMBRs(), Color.WHITE, Color.lightGray);
		VisionUtils.drawBoundingBoxes(img, v.findTNTsMBR(), Color.WHITE, Color.PINK);
		VisionUtils.drawBoundingBoxes(img, v.findBlackBirdsMBRs(), Color.BLACK);

		final Rectangle sling = v.findSlingshotMBR();
		if (sling != null)
			VisionUtils.drawBoundingBox(img, sling, Color.ORANGE, Color.BLACK);
		return img;
	}

	public static BufferedImage drawObjectsWithID(final BufferedImage image, final List<ABObject> objects) {
		if (!DebugUtils.DEBUG)
			return image;
		final BufferedImage imgMBR = VisionUtils.convert2grey(image);
		VisionUtils.drawBoundingBoxesWithID(imgMBR, objects, Color.WHITE);
		final BufferedImage imgRS = DebugUtils.drawRealShapes(image);

		final BufferedImage img = new BufferedImage(imgMBR.getWidth(), imgMBR.getHeight() + imgRS.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = img.createGraphics();

		g.drawImage(imgMBR, 0, 0, null);
		g.drawImage(imgRS, 0, imgMBR.getHeight(), null);
		// TODO maybe add MBR image as well
		return img;
	}

	public static BufferedImage drawRealShapes(final BufferedImage img) {
		if (!DebugUtils.DEBUG)
			return img;
		final BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics g = copy.createGraphics();
		g.drawImage(img, 0, 0, null);
		final VisionRealShape v = new VisionRealShape(copy);
		v.findSling();
		v.findPigs();
		v.findBirds();
		v.findObjects();
		v.findHills();
		v.findTrajectory();
		v.drawObjects(copy, false);
		return copy;
	}

	public static void init(final StrategyManager smanager, final TacticManager tmanager) {
		System.out.println("Init");
		DebugUtils.strategyManager = smanager;
		DebugUtils.tacticManager = tmanager;
	}

	public static void initBenchmarkParametersValues() {

		if (DebugUtils.DEBUG) {
			DebugUtils.currentLevel = 0;
			DebugUtils.currentTurn = 0;
			DebugUtils.timeList.clear();
			DebugUtils.currentBird = "";
			DebugUtils.n_redBirds = 0;
			DebugUtils.n_yellowBirds = 0;
			DebugUtils.n_blueBirds = 0;
			DebugUtils.n_blackBirds = 0;
			DebugUtils.n_whiteBirds = 0;
			DebugUtils.n_pigs = 0;
			DebugUtils.n_ice = 0;
			DebugUtils.n_stone = 0;
			DebugUtils.n_wood = 0;
			DebugUtils.n_hills = 0;
			DebugUtils.n_tnt = 0;
			DebugUtils.n_unknown = 0;
		}
	}

	/**
	 * Save some useful benchmark parameters in a file
	 */
	public static void saveBenchmark() {

		if (DebugUtils.DEBUG) {
			PrintWriter log = null;
			try {
				log = new PrintWriter(new FileWriter(DebugUtils.BENCHMARK_FILE, true));
				log.print(DebugUtils.currentLevel + ";");
				log.print(DebugUtils.currentTurn + ";");
				log.print(DebugUtils.currentBird + ";");
				log.print(DebugUtils.n_redBirds + ";");
				log.print(DebugUtils.n_yellowBirds + ";");
				log.print(DebugUtils.n_blueBirds + ";");
				log.print(DebugUtils.n_blackBirds + ";");
				log.print(DebugUtils.n_whiteBirds + ";");
				log.print(DebugUtils.n_redBirds + DebugUtils.n_yellowBirds + DebugUtils.n_blueBirds
						+ DebugUtils.n_blackBirds + DebugUtils.n_whiteBirds + ";");
				log.print(DebugUtils.n_pigs + ";");
				log.print(DebugUtils.n_ice + ";");
				log.print(DebugUtils.n_stone + ";");
				log.print(DebugUtils.n_wood + ";");
				log.print(DebugUtils.n_hills + ";");
				log.print(DebugUtils.n_tnt + ";");
				log.print(DebugUtils.n_unknown + ";");
				log.print(DebugUtils.timeList.get(DebugUtils.timeList.size() - 1).stamp
						- DebugUtils.timeList.get(0).stamp + ";");
				Long tempTime = null;
				for (final TimePair timep : DebugUtils.timeList)
					if (tempTime == null)
						tempTime = timep.stamp;
					else {
						// get milliseconds from nanoseconds and do not make
						// higher resolution than microseconds
						log.print(timep.info + ";" + String.format("%.3f", (timep.stamp - tempTime) / 1000.0 / 1000.0)
								+ ";");
						tempTime = timep.stamp;
					}
				log.println();
			} catch (final IOException e) {
				DebugUtils.Log.warning("cannot write " + DebugUtils.BENCHMARK_FILE);
			} finally {
				if (log != null)
					log.close();
			}
		}

	}

	public static void saveHex(final String file) {
		if (!DebugUtils.DEBUG)
			return;
		final File src = new File(file);
		final File dest = new File(DebugUtils.HEX_DIR + src.getName());
		try {
			Utils.copy(src, dest);
			DebugUtils.Log.fine("saved " + dest.getName());
		} catch (final IOException e) {
			DebugUtils.Log.warning("cannot copy " + file);
		}
	}

	public static void saveHexWithInfo(final String originalFile, final String reasoner) {
		if (!DebugUtils.DEBUG)
			return;
		final File src = new File(originalFile);
		final String fname = String.format("level[%s]%d_%d_#%d.hex", reasoner,
				(int) DebugUtils.strategyManager.getCurrentLevel(), DebugUtils.tacticManager.getCurrentTurn(),
				DebugUtils.strategyManager.getHowManyTimesCurrentLevel());
		final File dest = new File(DebugUtils.HEX_DIR + fname);
		try {
			Utils.copy(src, dest);
			DebugUtils.Log.fine("saved " + dest.getName());
		} catch (final IOException e) {
			DebugUtils.Log.warning("cannot copy " + originalFile);
		}
	}

	public static void saveScreenshot(final BufferedImage img, final String fileName) {
		if (!DebugUtils.DEBUG)
			return;

		final String file = DebugUtils.SCREENSHOT_DIR + fileName + ".png";
		try {
			final File dest = new File(file);
			ImageIO.write(img, "png", dest);
			DebugUtils.Log.fine("saved " + dest.getName());
		} catch (final IOException e) {
			DebugUtils.Log.warning("cannot save " + file);
		}
	}

	public static void saveScreenshot(final String file) {
		if (!DebugUtils.DEBUG)
			return;
		final File src = new File(file);
		final File dest = new File(DebugUtils.SCREENSHOT_DIR + src.getName());
		try {
			Utils.copy(src, dest);
			DebugUtils.Log.fine("saved " + dest.getName());
		} catch (final IOException e) {
			DebugUtils.Log.warning("cannot copy " + file);
		}
	}

	/**
	 * @param currentBird
	 *            the currentBird to set
	 */
	public static void setCurrentBird(final String currentBird) {
		if (DebugUtils.DEBUG)
			DebugUtils.currentBird = currentBird;
	}

	/**
	 * @param currentLevel
	 *            the currentLevel to set
	 */
	public static void setCurrentLevel(final byte currentLevel) {
		if (DebugUtils.DEBUG)
			DebugUtils.currentLevel = currentLevel;
	}

	/**
	 * @param currentTurn
	 *            the currentTurn to set
	 */
	public static void setCurrentTurn(final int currentTurn) {
		if (DebugUtils.DEBUG)
			DebugUtils.currentTurn = currentTurn;
	}

	/**
	 * @param n_hills
	 *            the n_hills to set
	 */
	public static void setN_hills(final int n_hills) {
		if (DebugUtils.DEBUG)
			DebugUtils.n_hills = n_hills;
	}

	/**
	 * @param n_pigs
	 *            the n_pigs to set
	 */
	public static void setN_pigs(final int n_pigs) {
		if (DebugUtils.DEBUG)
			DebugUtils.n_pigs = n_pigs;
	}

	/**
	 * @param n_tnt
	 *            the n_tnt to set
	 */
	public static void setN_tnt(final int n_tnt) {
		if (DebugUtils.DEBUG)
			DebugUtils.n_tnt = n_tnt;
	}

	public static void showImage(final BufferedImage img) {
		if (!DebugUtils.DEBUG)
			return;
		if (DebugUtils.imgFrame == null)
			DebugUtils.imgFrame = new ImageSegFrame("Rotated rectangles.", img);
		DebugUtils.imgFrame.refresh(img);
	}

}
