/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013, XiaoYu (Gary) Ge, Jochen Renz, Stephen Gould,
**  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
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
//package ab.demo;
//
//import java.awt.Color;
//import java.awt.Rectangle;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.net.UnknownHostException;
//import java.util.List;
//
//import javax.imageio.ImageIO;
//
//import ab.planner.TrajectoryPlanner;
//import ab.server.Proxy;
//import ab.server.proxy.message.ProxyScreenshotMessage;
//import ab.utils.ShowDebuggingImage;
//import ab.vision.GameStateExtractor;
//import ab.vision.Vision;
//import ab.vision.VisionUtils;
//// User Interface of the trajectory module
//public class abTrajectory {
//    private static Proxy server;
//
//    public abTrajectory() {
//        if (server == null) {
//            try {
//		server = new Proxy(9000) {
//			@Override
//                        public void onOpen() {
//                            System.out.println("Client connected");
//			}
//
//			@Override
//			public void onClose() {
//                            System.out.println("Client disconnected");
//			}
//                    };
//		server.start();
//
//		System.out.println("Server started on port: " + server.getPort());
//
//		System.out.println("Waiting for client to connect");
//		server.waitForClients(1);
//
//            } catch (UnknownHostException e) {
//		e.printStackTrace();
//            }
//        }
//    }
//
//    public BufferedImage doScreenShot() {
//	byte[] imageBytes = server.send(new ProxyScreenshotMessage());
//        BufferedImage image = null;
//        try {
//            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
//        } catch (IOException e) {
//            // do something
//        }
//
//        return image;
//    }
//
//    public static void main(String args[]) {
//        abTrajectory ar = new abTrajectory();
//
//        ShowDebuggingImage frame = null;
//        GameStateExtractor gameStateExtractor = new GameStateExtractor();
//        TrajectoryPlanner trajectory = new TrajectoryPlanner();
//
//        while (true) {
//            // capture image
//            BufferedImage screenshot = ar.doScreenShot();
//            final int nHeight = screenshot.getHeight();
//            final int nWidth = screenshot.getWidth();
//
//            System.out.println("captured image of size " + nWidth + "-by-" + nHeight);
//
//            // extract game state
//            GameStateExtractor.GameState state = gameStateExtractor.getGameState(screenshot);
//            if (state != GameStateExtractor.GameState.PLAYING) {
//                continue;
//            }
//
//            // process image
//            Vision vision = new Vision(screenshot);
//            List<Rectangle> pigs = vision.findPigs();
//            List<Rectangle> redBirds = vision.findRedBirds();
//
//            Rectangle sling = vision.findSlingshot();
//            if (sling == null) {
//                System.out.println("...could not find the slingshot");
//                continue;
//            }
//            System.out.println("...found " + pigs.size() + " pigs and " + redBirds.size() + " birds");
//            System.out.println("...found slingshot at " + sling.toString());
//
//            // convert screenshot to grey scale and draw bounding boxes
//            screenshot = VisionUtils.convert2grey(screenshot);
//            VisionUtils.drawBoundingBoxes(screenshot, pigs, Color.GREEN);
//            VisionUtils.drawBoundingBoxes(screenshot, redBirds, Color.PINK);
//            VisionUtils.drawBoundingBox(screenshot, sling, Color.ORANGE);
//
//            // find active bird
//            Rectangle activeBird = trajectory.findActiveBird(redBirds);
//            if (activeBird == null) {
//                System.out.println("...could not find active bird");
//                continue;
//            }
//
//            trajectory.plotTrajectory(screenshot, sling, activeBird);
//
//            // show image
//            if (frame == null) {
//                frame = new ShowDebuggingImage("trajectory", screenshot);
//            } else {
//                frame.refresh(screenshot);
//            }
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//            }
//        }
//    }
//}
