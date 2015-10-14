package ab.utils;

import java.awt.image.BufferedImage;

import ab.demo.other.ActionRobot;
import ab.vision.Vision;

public class VisionPerformanceTest {

	private static void log(String message)
	{
		System.out.println(message);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		new ActionRobot();
		while(true)
		{
			long time = System.nanoTime();
			BufferedImage image = ActionRobot.doScreenShot();
			Vision vision = new Vision(image);
			vision.findBlocksMBR();
			log((System.nanoTime() - time) + "");
		}
	}

}
