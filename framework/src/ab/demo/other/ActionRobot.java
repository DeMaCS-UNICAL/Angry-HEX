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
package ab.demo.other;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyDragMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

/**
 * Util class for basic functions
 * 
 */
public class ActionRobot {
	public static Proxy proxy;
	public String level_status = "UNKNOWN";
	public int current_score = 0;
	private LoadLevelSchema lls;
	private RestartLevelSchema rls;
	static {
		if (proxy == null) {
			try {
				proxy = new Proxy(9000) {
					@Override
					public void onOpen() {
						System.out.println("Client connected");
					}

					@Override
					public void onClose() {
						System.out.println("Client disconnected");
					}
				};
				proxy.start();

				System.out
						.println("Server started on port: " + proxy.getPort());

				System.out.println("Waiting for client to connect");
				proxy.waitForClients(1);

			} catch (UnknownHostException e) {

				e.printStackTrace();
			}
		}
	}

	// A java util class for the standalone version. It provides common
	// functions an agent would use. E.g. get the screenshot
	public ActionRobot() {
		lls = new LoadLevelSchema(proxy);
		rls = new RestartLevelSchema(proxy);
	}

	public void restartLevel() {
		rls.restartLevel();
	}

	public static void GoFromMainMenuToLevelSelection() {
		// --- go from the main menu to the episode menu
		GameState state = StateUtil.getGameState(proxy);
		while (state == GameState.MAIN_MENU) {

			System.out.println("Go to the Episode Menu");
			proxy.send(new ProxyClickMessage(305, 277));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.getGameState(proxy);
		}
		// --- go from the episode menu to the level selection menu
		while (state == GameState.EPISODE_MENU) {
			System.out.println("Select the Poached Eggs Episode");
			proxy.send(new ProxyClickMessage(150, 300));
			state = StateUtil.getGameState(proxy);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			state = StateUtil.getGameState(proxy);
		}

	}

	public GameState shootWithStateInfoReturned(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();
		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		GameState state = StateUtil.getGameState(proxy);
		return state;

	}

	public synchronized GameState getState() {
		GameState state = StateUtil.getGameState(proxy);
		return state;
	}

	public void shoot(List<Shot> csc) {
		ShootingSchema ss = new ShootingSchema();

		ss.shoot(proxy, csc);
		System.out.println("Shooting Completed");
		System.out
				.println("wait 15 seconds to ensure all objects in the scene static");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void cshoot(Shot shot) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();
		shots.add(shot);
		ss.shoot(proxy, shots);
		System.out.println("Shooting Completed");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}

	public void cFastshoot(Shot shot) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();
		shots.add(shot);
		ss.shoot(proxy, shots);
	}

	public void fshoot(Shot shot) {
		ShootingSchema ss = new ShootingSchema();
		LinkedList<Shot> shots = new LinkedList<Shot>();
		shots.add(shot);
		ss.shoot(proxy, shots);
		// System.out.println(" tap time : " + shot.getT_tap());
		System.out.println("Shooting Completed");

	}

	public void click() {
		proxy.send(new ProxyClickMessage(100, 100));
	}

	public void drag() {
		proxy.send(new ProxyDragMessage(0, 0, 0, 0));
	}

	public void loadLevel(int... i) {
		int level = 1;
		if (i.length > 0) {
			level = i[0];
		}

		lls.loadLevel(level);
	}

	public static void fullyZoomOut() {
		for (int k = 0; k < 15; k++) {

			proxy.send(new ProxyMouseWheelMessage(-1));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void fullyZoomIn() {
		for (int k = 0; k < 15; k++) {

			proxy.send(new ProxyMouseWheelMessage(1));
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Modified by Angry-HEX Team
	 * Added "synchronized"
	 */
	public static synchronized BufferedImage doScreenShot() {
		byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
		BufferedImage image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {

		}

		return image;
	}
	/*
	 * @return the type of the bird on the sling.
	 * 
	 * **/
	public ABType getBirdTypeOnSling()
	{
		fullyZoomIn();
		BufferedImage screenshot = doScreenShot();
		Vision vision = new Vision(screenshot);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		fullyZoomOut();
		List<ABObject> _birds = vision.findBirdsMBR();
		if(_birds.isEmpty())
			return ABType.Unknown;
		Collections.sort(_birds, new Comparator<Rectangle>(){

			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				
				return ((Integer)(o1.y)).compareTo((Integer)(o2.y));
			}	
		});
		return _birds.get(0).getType();
	}

	public static void main(String args[]) {

		long time = System.currentTimeMillis();
		ActionRobot.doScreenShot();
		time = System.currentTimeMillis() - time;
		System.out.println(" cost: " + time);
		time = System.currentTimeMillis();
		int count = 0;
		while (count < 40) {
			ActionRobot.doScreenShot();
			count++;
		}

		System.out.println(" time to take 40 screenshots"
				+ (System.currentTimeMillis() - time));
		System.exit(0);

	}

	public int getScore() {
		return StateUtil.getScore(ActionRobot.proxy);
	}
}
