/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/
/*
 * Modified by Angry-HEX Team
 * Now implements Runnable
 * Removed unused "import"
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
package ab.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;

import ab.demo.other.ClientActionRobotJava;
import ab.server.Proxy;

/* GameImageRecorder ------------------------------------------------------ */

public class GameImageRecorder implements Runnable {
	
	/*
	 * Modified by Angry-HEX Team
	 * We kept the main method only for compatibility reasons
	 */
    static public void main(String[] args) {

        // check command line arguments
        if (args.length != 1) {
            System.err.println("USAGE: java GameImageRecorder <directory>");
            System.exit(1);
        }
        if (!(new File(args[0])).isDirectory()) {
            System.err.println("ERROR: directory " + args[0] + " does not exist");
            System.exit(1);
        }
    }
	
	/*
	 * Modified by Angry-HEX Team
	 * Added new class members
	 */
	ClientActionRobotJava ar;
	String path;
	public BufferedImage currentScreenshot;
	BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<BufferedImage>(100);
	int frameCount;
	Proxy proxy;
	public static GameImageRecorder theRecorder;
	/*
	 * Modified by Angry-HEX Team
	 * Added method "captureImage"
	 */
	//commented out because it's not used
//	private BufferedImage captureImage()
//	{
//		byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
//	    BufferedImage image = null;
//	    try {
//	        image = ImageIO.read(new ByteArrayInputStream(imageBytes));
//	    }
//	    catch (IOException e) { e.printStackTrace(); }
//	    return image;
//	}
	/*
	 * Modified by Angry-HEX Team
	 * Added method "save"
	 */
    public void save(String inputFile)

    {
    	  frameCount += 1;
  		
    	 String destFile = String.format(path + File.separator + "program%04d.hex", frameCount);
    	  Process p;
		try {
			p = Runtime.getRuntime().exec("cp -v "+inputFile+" "+destFile);
		  p.waitFor();
    	 
    	  BufferedReader reader = 
    	     new BufferedReader(new InputStreamReader(
    		 p.getInputStream()));
    	  String line = reader.readLine();
    	  while (line != null) {
    		line = reader.readLine();
    		System.out.println(line);
    	  }
    	  saveScreenshot(currentScreenshot);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
	/*
	 * Modified by Angry-HEX Team
	 * Added constructor
	 */
	public GameImageRecorder(String p,ClientActionRobotJava a)
	{
		path = p;
		ar = a;
		//ar = new ClientActionRobotJava("localhost");
		//ar.configure(ClientActionRobot.intToByteArray(1040));
		/*
        try {
            proxy = new Proxy(9001) {
                @Override
                public void onOpen() {
                    System.out.println("Connected to game proxy");
                }

                @Override
                public void onClose() {
                    System.out.println("Disconnected from game proxy");
                }
                };
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        proxy.start();
        System.out.println("Proxy Started"); 
        // proxy.waitForClients(1);
        */

		/*
		Thread t = new Thread() { public void run () { 
		//							try {
		
		                            //ar.waitConfiguration();
									/*
									 * while(true) {
									 
										 System.out.println("Taking Timed Screenshot");
										 //scheduleScreenshot(ar.doScreenShot());
										 BufferedImage im = ar.doScreenShot_();
										 System.out.println("Scheduling Timed Screenshot");
										 scheduleScreenshot(im);
										 System.out.println("Scheduled Timed Screenshot");
										 Thread.sleep(50);
									}
									}
										 catch (InterruptedException e) {
											e.printStackTrace();
										}
										
											}
									};
									t.start();
									*/
		// new Thread(this).start();							
	}
	public void run()
	{
		while(true)
		{
			
			try {
				  //System.out.println("Waiting screenshot request");	
				  BufferedImage im = queue.take();
				  //System.out.println("Performing screenshot PRETAKEN="+(im != null));	
				  saveScreenshot(im == null ? ar.doScreenShot() : im);
			      //System.out.println("Processed screenshot");		
				} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * Modified by Angry-HEX Team
	 * Added method "scheduleScreenshot"
	 */
	public void scheduleScreenshot(BufferedImage image)
	{
		try {
			queue.put(image);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Modified by Angry-HEX Team
	 * Added method "saveScreenshot"
	 */
    private void saveScreenshot(BufferedImage image) {
		        
        // enter game loop
//        ar.waitConfiguration();
        	/*
        	// capture screenshot
            byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
            BufferedImage image = null;
            try {
                image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        	//System.out.println("Saving Screenshot...");
            // write image to disk
            //if ((screenshot == null) ||
            //    (VisionUtils.numPixelsDifferent(screenshot, image) > 2048)) {
                final String imgFilename = String.format(path + File.separator + "program%04d.png", frameCount);
                //System.out.println("saving image to " + imgFilename);
                try {
                    ImageIO.write(image, "png", new File(imgFilename));
                } catch (IOException e) {
                    System.err.println("failed to save image " + imgFilename);
                    e.printStackTrace();
                }

                // update frame count
            //    screenshot = image;
            //}

            // sleep for a while
    }
};
