/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/

package ab.vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import Jama.Matrix;
import ab.demo.other.ActionRobot;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.utils.ImageSegFrame;

/* TestVision ------------------------------------------------------------- */

public class ShowSeg implements Runnable {
	private static List<Rectangle> pigs, redBirds, blueBirds, yellowBirds, blackBirds, whiteBirds, iceBlocks, woodBlocks, stoneBlocks, TNTs;
	private static List<Point> trajPoints;
	public  static boolean useRealshape = false;
	private static VisionRealShape vision;
	static public Proxy getGameConnection(int port) {
		Proxy proxy = null;
		try {
			proxy = new Proxy(port) {
				@Override
				public void onOpen() {
					System.out.println("...connected to game proxy");
				}

				@Override
				public void onClose() {
					System.out.println("...disconnected from game proxy");
				}
			};
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		proxy.start();
		System.out.println("Waiting for proxy to connect...");
		proxy.waitForClients(1);

		return proxy;
	}
	
	static public int[][] computeMetaInformation(BufferedImage screenshot) {
		// image size
		final int nHeight = screenshot.getHeight();
		final int nWidth = screenshot.getWidth();

		// meta debugging information
		int[][] meta = new int[nHeight][nWidth];
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				final int colour = screenshot.getRGB(x, y);
				meta[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
			}
		}

		return meta;
	}
	public static BufferedImage drawRealshape(BufferedImage screenshot)
	{
		// get game state
		GameStateExtractor game = new GameStateExtractor();
		GameStateExtractor.GameState state = game.getGameState(screenshot);
		if (state != GameStateExtractor.GameState.PLAYING) 
		{
			screenshot = VisionUtils.convert2grey(screenshot);
			return screenshot;
		}
	    vision = new VisionRealShape(screenshot);
		
	    vision.findObjects();
		vision.findPigs();
		vision.findHills();
		vision.findBirds();
		
		vision.findSling();
	
		vision.findTrajectory();
		vision.drawObjects(screenshot, true);
		
		
		return screenshot;
			
	}
	public static BufferedImage drawMBRs(BufferedImage screenshot) {


		// get game state
		GameStateExtractor game = new GameStateExtractor();
		GameStateExtractor.GameState state = game.getGameState(screenshot);
		if (state != GameStateExtractor.GameState.PLAYING) {
			screenshot = VisionUtils.convert2grey(screenshot);
			return screenshot;
		}

		
		// process image
		Vision vision = new Vision(screenshot);
		pigs = vision.getMBRVision().findPigsMBR();
		redBirds = vision.getMBRVision().findRedBirdsMBRs();
		blueBirds = vision.getMBRVision().findBlueBirdsMBRs();
		yellowBirds = vision.getMBRVision().findYellowBirdsMBRs();
		woodBlocks = vision.getMBRVision().findWoodMBR();
		stoneBlocks = vision.getMBRVision().findStonesMBR();
		iceBlocks = vision.getMBRVision().findIceMBR();
		whiteBirds = vision.getMBRVision().findWhiteBirdsMBRs();
		blackBirds = vision.getMBRVision().findBlackBirdsMBRs();
		TNTs = vision.getMBRVision().findTNTsMBR();
		trajPoints = vision.findTrajPoints();

		Rectangle sling = vision.findSlingshotMBR();


		// draw objects
		screenshot = VisionUtils.convert2grey(screenshot);
		VisionUtils.drawBoundingBoxes(screenshot, pigs, Color.GREEN);
		VisionUtils.drawBoundingBoxes(screenshot, redBirds, Color.RED);
		VisionUtils.drawBoundingBoxes(screenshot, blueBirds, Color.BLUE);
		VisionUtils.drawBoundingBoxes(screenshot, yellowBirds, Color.YELLOW);
		VisionUtils.drawBoundingBoxes(screenshot, woodBlocks, Color.WHITE,
				Color.ORANGE);
		VisionUtils.drawBoundingBoxes(screenshot, stoneBlocks, Color.WHITE,
				Color.GRAY);
		VisionUtils.drawBoundingBoxes(screenshot, iceBlocks, Color.WHITE,
				Color.CYAN);
		VisionUtils.drawBoundingBoxes(screenshot, whiteBirds, Color.WHITE,
				Color.lightGray);
		VisionUtils.drawBoundingBoxes(screenshot, TNTs, Color.WHITE,
				Color.PINK);
		VisionUtils.drawBoundingBoxes(screenshot, blackBirds,
				Color.BLACK);
		if (sling != null) {
			VisionUtils.drawBoundingBox(screenshot, sling, Color.ORANGE,
					Color.BLACK);

			// generate traj points using estimated parameters
			Matrix W = vision.getMBRVision().fitParabola(trajPoints);
			int p[][] = new int[2][100];
			int startx = (int) sling.getCenterX();
			for (int i = 0; i < 100; i++) {
				p[0][i] = startx;
				double tem = W.get(0, 0) * Math.pow(p[0][i], 2) + W.get(1, 0)
						* p[0][i] + W.get(2, 0);
				p[1][i] = (int) tem;
				startx += 10;
			}
			if (W.get(0, 0) > 0)
				VisionUtils.drawtrajectory(screenshot, p, Color.RED);

		}

		return screenshot;
	}

	public static void main(String[] args)
	{

		ImageSegFrame frame = null;
		BufferedImage screenshot = null;
		
		// check command line arguments
		if (args.length > 1) {
			System.err.println("  USAGE: java TestVision [(<directory> | <image>)]");
			System.exit(1);
		}

		// connect to game proxy if no arguments given
		if (args.length == 0) {
			
			Proxy game = getGameConnection(9000);

			while (true) {
				// Capture an image
				byte[] imageBytes = game.send(new ProxyScreenshotMessage());
				try {
					screenshot = ImageIO.read(new ByteArrayInputStream(
							imageBytes));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Analyze and show image
				screenshot = drawMBRs(screenshot);
				//screenshot = drawRealshape(screenshot);
				if (frame == null) {
					frame = new ImageSegFrame("Vision", screenshot,
							null);
				} else {
					frame.refresh(screenshot, null);
				}
				// sleep for 50ms
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					System.err.println("Thread Interrupted");
				}
			}
		}

		// Get list of images to process
		File[] images = null;

		// Check if argument is a directory or an image
		if ((new File(args[0])).isDirectory()) {
			images = new File(args[0]).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".png");
				}
			});
		} else {
			images = new File[1];
			images[0] = new File(args[0]);
		}

		// Iterate through the images
		Arrays.sort(images);
		for (File filename : images) {
			if (filename.isDirectory()) {
				continue;
			}

			// Load the screenshot
			try {
				screenshot = ImageIO.read(filename);
			} catch (IOException e) {
				System.err.println("ERROR: could not load image " + filename);
				System.exit(1);
			}

			// analyse and show image
			int[][] meta = computeMetaInformation(screenshot);
			screenshot = drawMBRs(screenshot);
			if (frame == null) {
				frame = new ImageSegFrame("Image Segementation", screenshot, meta);
			} else {
				frame.refresh(screenshot, meta);
			}
			frame.waitForKeyPress();
		}

		frame.close();
	}
	//add for LoadLevel Agent

	@Override
	public void run() {
		
		ImageSegFrame frame = null;
		BufferedImage screenshot = null;
       	while (true) {
			// capture an image
		    screenshot = ActionRobot.doScreenShot();
			// analyse and show image
			//int[][] meta = computeMetaInformation(screenshot);
		    
		    if(!useRealshape)
		    	screenshot = drawMBRs(screenshot);
		    else
		    	screenshot = drawRealshape(screenshot);
		    
			if (frame == null) {

				frame = new ImageSegFrame("Image Segmentation", screenshot,
						null);
			} else {
				frame.refresh(screenshot, null);
			}

			// sleep for 50ms
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.err.println(" Thread Interrupt");
			}
		}
	}
	
}
