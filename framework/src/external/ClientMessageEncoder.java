/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package external;

/*encode the messages to byte[]*/
public class ClientMessageEncoder {

	//encode screenshot message
	public static byte[] encodeDoScreenShot() {
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.doScreenShot) };
		return message;

	}
  
	//encode configure message
	public static byte[] configure(byte[] id) {
		byte[] message = new byte[1 + id.length];
		message = mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.configure) },
				id);
	  
		return message;
	}
	
	//encode loadlevel message
	//allow 0 or 1 input argument
	public static byte[] loadLevel(byte... level) {
		byte[] message = {
				ClientMessageTable.getValue(ClientMessageTable.loadLevel),
				((level.length == 0) ? 0 : level[0]) };
		return message;
	}

	//encode restart message
	public static byte[] restart() {
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.restartLevel) };
		return message;
	}

	//encode cshoot message (safe mode)
	public static byte[] cshoot(byte[] fx, byte[] fy, byte[] dx, byte[] dy,
			byte[] t1, byte[] t2) {
		byte[] message = new byte[1 + fx.length + fy.length + dx.length
				+ dy.length + t1.length + t2.length];
		message = mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.cshoot) },
				fx, fy, dx, dy, t1, t2);
		return message;

	}

	//encode pshoot message (safe mode)
	public static byte[] pshoot(byte[] fx, byte[] fy, byte[] dx, byte[] dy,
			byte[] t1, byte[] t2) {
		byte[] message = new byte[1 + fx.length + fy.length + dx.length
				+ dy.length + t1.length + t2.length];
		message = mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.pshoot) },
				fx, fy, dx, dy, t1, t2);
		return message;
	}
	//encode pshoot message in fast mode
	public static byte[] pFastshoot(byte[] fx, byte[] fy, byte[] dx, byte[] dy,
			byte[] t1, byte[] t2) {
		byte[] message = new byte[1 + fx.length + fy.length + dx.length
				+ dy.length + t1.length + t2.length];
		message = mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.pFastshoot) },
				fx, fy, dx, dy, t1, t2);
		return message;
	}
	//encode cshoot message in fast mode
	public static byte[] cFastshoot(byte[] fx, byte[] fy, byte[] dx, byte[] dy,
			byte[] t1, byte[] t2) {
		byte[] message = new byte[1 + fx.length + fy.length + dx.length
				+ dy.length + t1.length + t2.length];
		message = mergeArray(
				new byte[] { ClientMessageTable
						.getValue(ClientMessageTable.cFastshoot) },
				fx, fy, dx, dy, t1, t2);
		return message;

	}
	
	//encode fully zoom out message 
	public static byte[] fullyZoomOut() {
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.fullyZoomOut) };
		return message;

	}
	public static byte[] fullyZoomIn() {
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.fullyZoomIn) };
		return message;

	}
	public static byte[] clickInCenter()
	{
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.clickInCentre) };
		return message;
	}
	//encode getState message
	public static byte[] getState() {
		byte[] message = { ClientMessageTable
				.getValue(ClientMessageTable.getState) };
		return message;
	}
	//encode  get best scores message 
	public static byte[] getBestScores() 
	{
		byte[] message = {ClientMessageTable.getValue(ClientMessageTable.getBestScores)};
		return message;
	} 
	//get my score message
	public static byte[] getMyScore()
	{
		byte[] message = {ClientMessageTable.getValue(ClientMessageTable.getMyScore)};
		return message;
	}
	//merge byte arrays into one array
	public static byte[] mergeArray(final byte[]... arrays) {
		int size = 0;
		for (byte[] a : arrays) {
			size += a.length;
		}
		byte[] res = new byte[size];

		int destPos = 0;
		for (int i = 0; i < arrays.length; i++) {
			if (i > 0)
				destPos += arrays[i - 1].length;
			int length = arrays[i].length;
			System.arraycopy(arrays[i], 0, res, destPos, length);
		}
		return res;
	}
}
