/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
/*
 * Modified by Angry-HEX Team
 * Commented because it no longer works due to changes in the framework
 */
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
//package ab.vision;
//
//import java.awt.Color;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.net.UnknownHostException;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//
//import Jama.Matrix;
//import ab.demo.other.ActionRobot;
//import ab.demo.util.StateUtil;
//import ab.server.Proxy;
//import ab.server.proxy.message.ProxyScreenshotMessage;
//import ab.utils.ShowDebuggingImage;
//
///* TestVision ------------------------------------------------------------- */
//
//public class TestVision implements Runnable {
//
//	static public Proxy getGameConnection(int port) {
//		Proxy proxy = null;
//		try {
//			proxy = new Proxy(port) {
//				@Override
//				public void onOpen() {
//					System.out.println("...connected to game proxy");
//				}
//
//				@Override
//				public void onClose() {
//					System.out.println("...disconnected from game proxy");
//				}
//			};
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
//		proxy.start();
//		System.out.println("Waiting for proxy to connect...");
//		proxy.waitForClients(1);
//
//		return proxy;
//	}
//
//	static public int[][] computeMetaInformation(BufferedImage screenshot) {
//		// image size
//		final int nHeight = screenshot.getHeight();
//		final int nWidth = screenshot.getWidth();
//
//		// meta debugging information
//		int[][] meta = new int[nHeight][nWidth];
//		for (int y = 0; y < nHeight; y++) {
//			for (int x = 0; x < nWidth; x++) {
//				final int colour = screenshot.getRGB(x, y);
//				meta[y][x] = ((colour & 0x00e00000) >> 15)
//						| ((colour & 0x0000e000) >> 10)
//						| ((colour & 0x000000e0) >> 5);
//			}
//		}
//
//		return meta;
//	}
//
//	static public BufferedImage analyseScreenShot(BufferedImage screenshot) {
//
//
//		// get game state
//		GameStateExtractor game = new GameStateExtractor();
//		GameStateExtractor.GameState state = game.getGameState(screenshot);
//	//	System.out.println(state.toString());
//
//		if (state != GameStateExtractor.GameState.PLAYING) {
//			System.out.println("End game score : " + game.getScoreEndGame(screenshot));
//			screenshot = VisionUtils.convert2grey(screenshot);
//			return screenshot;
//		}
//
//		System.out.println("In game score : " + game.getScoreInGame(screenshot));
//		// process image
//		Vision vision = new Vision(screenshot);
//		List<Rectangle> pigs = vision.findPigs();
//		List<Rectangle> redBirds = vision.findRedBirds();
//		List<Rectangle> blueBirds = vision.findBlueBirds();
//		List<Rectangle> yellowBirds = vision.findYellowBirds();
//		List<Rectangle> woodBlocks = vision.findWoodAsRectangles();
//		List<Rectangle> stoneBlocks = vision.findStonesAsRectangles();
//		List<Rectangle> iceBlocks = vision.findIceAsRectangles();
//		List<Rectangle> whiteBirds = vision.findWhiteBirds();
//		List<Rectangle> blackBirds = vision.findBlackBirds();
//		List<Rectangle> TNTs = vision.findTNTs();
//		List<Point> trajPoints = vision.findTrajPoints();
//
//		Rectangle sling = vision.findSlingshot();
//
//
//		// draw objects
//		screenshot = VisionUtils.convert2grey(screenshot);
//		VisionUtils.drawBoundingBoxes(screenshot, pigs, Color.GREEN);
//		VisionUtils.drawBoundingBoxes(screenshot, redBirds, Color.RED);
//		VisionUtils.drawBoundingBoxes(screenshot, blueBirds, Color.BLUE);
//		VisionUtils.drawBoundingBoxes(screenshot, yellowBirds, Color.YELLOW);
//		VisionUtils.drawBoundingBoxes(screenshot, woodBlocks, Color.WHITE,
//				Color.ORANGE);
//		VisionUtils.drawBoundingBoxes(screenshot, stoneBlocks, Color.WHITE,
//				Color.GRAY);
//		VisionUtils.drawBoundingBoxes(screenshot, iceBlocks, Color.WHITE,
//				Color.CYAN);
//		VisionUtils.drawBoundingBoxes(screenshot, whiteBirds, Color.WHITE,
//				Color.lightGray);
//		VisionUtils.drawBoundingBoxes(screenshot, TNTs, Color.WHITE,
//				Color.PINK);
//		VisionUtils.drawBoundingBoxes(screenshot, blackBirds,
//				Color.BLACK);
//		if (sling != null) {
//			VisionUtils.drawBoundingBox(screenshot, sling, Color.ORANGE,
//					Color.BLACK);
//
//			// generate traj points using estimated parameters
//			Matrix W = vision.fitParabola(trajPoints);
//			int p[][] = new int[2][100];
//			int startx = (int) sling.getCenterX();
//			for (int i = 0; i < 100; i++) {
//				p[0][i] = startx;
//				double tem = W.get(0, 0) * Math.pow(p[0][i], 2) + W.get(1, 0)
//						* p[0][i] + W.get(2, 0);
//				p[1][i] = (int) tem;
//				startx += 10;
//			}
//			if (W.get(0, 0) > 0)
//				VisionUtils.drawtrajectory(screenshot, p, Color.RED);
//
//		}
//
//		return screenshot;
//	}
//
//	static public void main(String[] args) {
//
//		ShowDebuggingImage frame = null;
//		BufferedImage screenshot = null;
//		StateUtil stateUtil = new StateUtil();
//		// check command line arguments
//		if (args.length > 1) {
//			System.err.println("  USAGE: java TestVision [(<directory> | <image>)]");
//			System.exit(1);
//		}
//
//		// connect to game proxy if no arguments given
//		if (args.length == 0) {
//			GameStateExtractor gameStateExtractor = new GameStateExtractor();
//			Proxy game = getGameConnection(9000);
//
//			while (true) {
//				// capture an image
//				byte[] imageBytes = game.send(new ProxyScreenshotMessage());
//				try {
//					screenshot = ImageIO.read(new ByteArrayInputStream(
//							imageBytes));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				System.out.println(" The game state is : " + gameStateExtractor.getGameState(screenshot));
//
//				// analyse and show image
//				int[][] meta = computeMetaInformation(screenshot);
//				screenshot = analyseScreenShot(screenshot);
//				
//				if (frame == null) {
//					frame = new ShowDebuggingImage("TestVision", screenshot,
//							meta);
//				} else {
//					frame.refresh(screenshot, meta);
//				}
//
//				// sleep for 100ms
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					// do nothing
//				}
//			}
//		}
//
//		// get list of images to process
//		File[] images = null;
//
//		// check if argument is a directory or an image
//		if ((new File(args[0])).isDirectory()) {
//			images = new File(args[0]).listFiles(new FilenameFilter() {
//				@Override
//				public boolean accept(File directory, String fileName) {
//					return fileName.endsWith(".png");
//				}
//			});
//		} else {
//			images = new File[1];
//			images[0] = new File(args[0]);
//		}
//
//		// iterate through the images
//		Arrays.sort(images);
//		for (File filename : images) {
//			if (filename.isDirectory()) {
//				continue;
//			}
//
//			// load the screenshot
//			try {
//				screenshot = ImageIO.read(filename);
//			} catch (IOException e) {
//				System.err.println("ERROR: could not load image " + filename);
//				System.exit(1);
//			}
//
//			// analyse and show image
//			int[][] meta = computeMetaInformation(screenshot);
//			screenshot = analyseScreenShot(screenshot);
//			if (frame == null) {
//				frame = new ShowDebuggingImage("TestVision", screenshot, meta);
//			} else {
//				frame.refresh(screenshot, meta);
//			}
//			frame.waitForKeyPress();
//		}
//
//		frame.close();
//	}
//	
//	//add for LoadLevel Agent
//
//	@Override
//	public void run() {
//		ShowDebuggingImage frame = null;
//		BufferedImage screenshot = null;
//		
//		while (true) {
//			// capture an image
//		    screenshot = ActionRobot.doScreenShot();
//			// analyse and show image
//			int[][] meta = computeMetaInformation(screenshot);
//			screenshot = analyseScreenShot(screenshot);
//			if (frame == null) {
//
//				frame = new ShowDebuggingImage("TestVision", screenshot,
//						meta);
//			} else {
//				frame.refresh(screenshot, meta);
//			}
//
//			// sleep for 100ms
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
//		}
//	}
//	
//}
