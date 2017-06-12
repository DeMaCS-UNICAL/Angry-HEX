/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
/*
 * Modified by Angry-HEX Team
 * Added GameImageRecorder
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
package ab.demo.other;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import external.ClientMessageEncoder;
import external.ClientMessageTable;

import ab.utils.GameImageRecorder;


/**
 * A server/client version of the java util class that encodes client messages and decodes 
 * the corresponding server messages complying with the protocols. Its subclass is ClientActionRobotJava.java 
 * which decodes the received server messages into java objects.
 * */
public class ClientActionRobot {
	Socket requestSocket;
	OutputStream out;
	InputStream in;
	String message;

	/*
	 * Modified by Angry-HEX Team
	 * Added GameImageRecorder
	 */
	GameImageRecorder rec;

	public ClientActionRobot(GameImageRecorder r, String... ip)
	{
		this(ip);
		rec = r;
	}

	
	public ClientActionRobot(String... ip) {
		String _ip = "localhost";
		if (ip.length > 0) {
			_ip = ip[0];
		}
		try {
			// 1. creating a socket to connect to the server
			requestSocket = new Socket(_ip, 2004);
			requestSocket.setReceiveBufferSize(100000);
			System.out.println("Connected to " + _ip + " in port 2004");
			out = requestSocket.getOutputStream();
			out.flush();
			in = requestSocket.getInputStream();
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	
	public synchronized BufferedImage doScreenShot_() {
		BufferedImage bfImage = null;
		try {
			// 2. get Input and Output streams
			byte[] doScreenShot = ClientMessageEncoder.encodeDoScreenShot();
			out.write(doScreenShot);
			out.flush();
			// System.out.println("client executes command: screen shot");

			//Read the message head : 4-byte width and 4-byte height, respectively
			byte[] bytewidth = new byte[4];
			byte[] byteheight = new byte[4];
			int width, height;
			in.read(bytewidth);
			width = bytesToInt(bytewidth);
			in.read(byteheight);
			height = bytesToInt(byteheight);
			
			//initialize total bytes of the screenshot message
			//not include the head
			int totalBytes = width * height * 3;

			//read the raw RGB data
			byte[] bytebuffer;
			//System.out.println(width + "  " + height);
			byte[] imgbyte = new byte[totalBytes];
			int hasReadBytes = 0;
			while (hasReadBytes < totalBytes) {
				bytebuffer = new byte[2048];
				int nBytes = in.read(bytebuffer);
				if (nBytes != -1)
					System.arraycopy(bytebuffer, 0, imgbyte, hasReadBytes,
							nBytes);
				else
					break;
				hasReadBytes += nBytes;
			}
			
			//set RGB data using BufferedImage  
			bfImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int R = imgbyte[(y * width + x) * 3] & 0xff;
					int G = imgbyte[(y * width + x) * 3 + 1] & 0xff;
					int B = imgbyte[(y * width + x) * 3 + 2] & 0xff;
					Color color = new Color(R, G, B);
					int rgb;
					rgb = color.getRGB();
					bfImage.setRGB(x, y, rgb);
				}
			}
			
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
		    e.printStackTrace();
		}
		return bfImage;

	}

	public BufferedImage doScreenShot()
	{
		BufferedImage bfImage = doScreenShot_();
		//if (rec != null) rec.scheduleScreenshot(bfImage);
		return bfImage;
	}
	
	
	//convert a byte[4] array to int value
	public int bytesToInt(byte... b) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}

	//convert an int value to byte[4] array
	public static byte[] intToByteArray(int a) {
		byte[] ret = new byte[4];
		ret[3] = (byte) (a & 0xFF);
		ret[2] = (byte) ((a >> 8) & 0xFF);
		ret[1] = (byte) ((a >> 16) & 0xFF);
		ret[0] = (byte) ((a >> 24) & 0xFF);
		return ret;
	}

	//send message to fully zoom out
	public synchronized byte fullyZoomOut() {
		try {
			out.write(ClientMessageEncoder.fullyZoomOut());
			out.flush();
			return (byte) in.read();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return 0;

	}
	
	//send message to fully zoom in
	public synchronized byte fullyZoomIn() {
		try {
			out.write(ClientMessageEncoder.fullyZoomIn());
			out.flush();
			return (byte) in.read();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return 0;

	}
	public byte clickInCenter() {
		try {
			out.write(ClientMessageEncoder.clickInCenter());
			out.flush();
			return (byte) in.read();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return 0;

	}
	//register team id
	public synchronized byte[] configure(byte[] team_id) {
		try {
			out.write(ClientMessageEncoder.configure(team_id));
			out.flush();
			byte[] result = new byte[4];
			in.read(result);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	//load a certain level
	public synchronized byte loadLevel(byte... i) {
		try {
			
			out.write(ClientMessageEncoder.loadLevel(i));
			return (byte) in.read();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return 0;
	}

	
	//send a message to restart the level
	public synchronized byte restartLevel() {
		try {
			out.write(ClientMessageEncoder.restart());
			return (byte) in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;

	}

	//send a shot message to execute a shot in the safe mode
	public byte[] shoot(byte[] fx, byte[] fy, byte[] dx, byte[] dy, byte[] t1,
			byte[] t2, boolean polar) {
		byte[] inbuffer = new byte[16];
		try {
			if (polar)
				out.write(ClientMessageEncoder.pshoot(fx, fy, dx, dy, t1, t2));
			else
				out.write(ClientMessageEncoder.cshoot(fx, fy, dx, dy, t1, t2));
			out.flush();
			in.read(inbuffer);
			return inbuffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] { 0 };
	}
	
	//send a shot message to execute a shot in the fast mode
	public byte[] shootFast(byte[] fx, byte[] fy, byte[] dx, byte[] dy, byte[] t1,
			byte[] t2, boolean polar) {
		byte[] inbuffer = new byte[16];
		try {
			if (polar)
				out.write(ClientMessageEncoder.pFastshoot(fx, fy, dx, dy, t1, t2));
			else
				out.write(ClientMessageEncoder.cFastshoot(fx, fy, dx, dy, t1, t2));
			out.flush();
			in.read(inbuffer);
			return inbuffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] { 0 };
	}

	//send a sequence of shots message
	public byte[] cshootSequence(byte[]... shots) {
		byte[] inbuffer = new byte[16];

		byte[] msg = ClientMessageEncoder.mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.shootSeq) },
				new byte[] { (byte) shots.length });
		for (byte[] shot : shots) {
			msg = ClientMessageEncoder.mergeArray(msg,
					new byte[] { ClientMessageTable
							.getValue(ClientMessageTable.cshoot) }, shot);
		}

		try {
			out.write(msg);
			
			in.read(inbuffer);
			return inbuffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] { 0 };
	}

	
	//send a message to get the current state
	public byte getState() {
		try {
			out.write(ClientMessageEncoder.getState());
			out.flush();
			// System.out.println("IN READ  " + in.read());
			return (byte) in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}
	// send a message to score of each level
	public byte[] getBestScores()
	{
		int level = 21;
		int totalBytes = level * 4;
		byte[] buffer = new byte[totalBytes];
		try {
			out.write(ClientMessageEncoder.getBestScores());
			out.flush();
		
			in.read(buffer);
		    return buffer; 
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;	
	}
	// send a message to score of each level
	public byte[] getMyScore()
	{
		int level = 21;
		int totalBytes = level * 4;
		byte[] buffer = new byte[totalBytes];
		try {
			out.write(ClientMessageEncoder.getMyScore());
			out.flush();
		
			in.read(buffer);
		    return buffer; 
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;	
	}
	
	public static void main(String args[])
	{
		ClientActionRobot robot = new ClientActionRobot();
		byte[] id = {1,2,3,4};
		robot.configure(id);
		while(true)
			robot.doScreenShot();
	}

}
