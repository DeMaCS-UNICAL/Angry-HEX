/*
 * Modified by Angry-HEX Team
 * Commented because it no longer works due to changes in the framework
 * Removed Auto-generated TODOs
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
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//import javax.imageio.ImageIO;
//
//import ab.demo.other.ActionRobot;
//import ab.vision.TestVision;
//
//public class LoadLevelAgent {
//public static void main(String args[])
//{
//	ActionRobot ar = new ActionRobot();
//	Thread thre = new Thread(new TestVision());
//    thre.start();
//    //Start Load Level;  
//    for (int i = 1; i < 22; i++)
//    {
//    	System.out.println("load level " + i);
//    	ar.loadLevel(i);
//    	System.out.println("save screenshot ");
//    	BufferedImage image = ActionRobot.doScreenShot();
//    	try {
//			ImageIO.write(image, "png", new File("level_" + i  + ".png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	int[][] meta = TestVision.computeMetaInformation(image);
//		image = TestVision.analyseScreenShot(image);
//		try {
//			ImageIO.write(image, "png", new File("level_seg_" + i  + ".png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	
//    }
//		System.out.println("Finished");
//		System.exit(0);
//
//}
//}
