/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import Jama.Matrix;

/* Vision ----------------------------------------------------------------- */

public class VisionMBR {

	private int _nHeight; // height of the scene
	private int _nWidth; // width of the scene
	public int _scene[][]; // quantized scene colours
	private int _nSegments; // number of segments
	private int _segments[][]; // connected components (0 to _nSegments)
	private int _colours[]; // colour for each segment
	private Rectangle _boxes[]; // bounding box for each segment
	private int _regionThreshold = 10; // minimal pixels in a region

	// create a vision object for processing a given screenshot
	public VisionMBR(BufferedImage screenshot) {

		//Reset the ID counter for each segmentation
		ABObject.resetCounter();
		processScreenShot(screenshot);
	}

	//find slingshot
	//only return one rectangle
	public Rectangle findSlingshotMBR() {
		Rectangle obj;


		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 345) || ignorePixel[i][j])
					continue;
				obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();

				LinkedList<Point> pointsinRec = new LinkedList<Point>();

				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to slingshot
					
					//check underneath pixel
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 345
								|| _scene[p.y + 1][p.x] == 418
								|| _scene[p.y + 1][p.x] == 273
								|| _scene[p.y + 1][p.x] == 281
								|| _scene[p.y + 1][p.x] == 209
								|| _scene[p.y + 1][p.x] == 346
								|| _scene[p.y + 1][p.x] == 354
								|| _scene[p.y + 1][p.x] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}
					
					//check right pixel
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 345
								|| _scene[p.y][p.x + 1] == 418
								|| _scene[p.y][p.x + 1] == 346
								|| _scene[p.y][p.x + 1] == 354
								|| _scene[p.y][p.x + 1] == 273
								|| _scene[p.y][p.x + 1] == 281
								|| _scene[p.y][p.x + 1] == 209
								|| _scene[p.y][p.x + 1] == 282 || _scene[p.y][p.x + 1] == 351)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					//check upper pixel
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 345
								|| _scene[p.y - 1][p.x] == 418
								|| _scene[p.y - 1][p.x] == 346
								|| _scene[p.y - 1][p.x] == 354
								|| _scene[p.y - 1][p.x] == 273
								|| _scene[p.y - 1][p.x] == 281
								|| _scene[p.y - 1][p.x] == 209
								|| _scene[p.y - 1][p.x] == 282 || _scene[p.y - 1][p.x] == 351)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					//check left pixel
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 345
								|| _scene[p.y][p.x - 1] == 418
								|| _scene[p.y][p.x - 1] == 346
								|| _scene[p.y][p.x - 1] == 354
								|| _scene[p.y][p.x - 1] == 273
								|| _scene[p.y][p.x - 1] == 281
								|| _scene[p.y][p.x - 1] == 209
								|| _scene[p.y][p.x - 1] == 282 || _scene[p.y][p.x - 1] == 351)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					//ignore checked pixels
					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				int[] hist = histogram(obj);

				// abandon shelf underneath
				if (obj.height > 10) {
					Rectangle col = new Rectangle(obj.x, obj.y, 1, obj.height);
					int[] histCol = histogram(col);
				

					if (_scene[obj.y][obj.x] == 511
							|| _scene[obj.y][obj.x] == 447) {
						for (int m = obj.y; m < obj.y + obj.height; m++) {
							if (_scene[m][obj.x] == 345
									|| _scene[m][obj.x] == 418
									|| _scene[m][obj.x] == 346
									|| _scene[m][obj.x] == 354
									|| _scene[m][obj.x] == 273
									|| _scene[m][obj.x] == 281
									|| _scene[m][obj.x] == 209
									|| _scene[m][obj.x] == 282
									|| _scene[m][obj.x] == 351) {
								obj.setSize(obj.width, m - obj.y);
								break;
							}
						}
					}

					while (histCol[511] >= obj.height * 0.8) {
						obj.setBounds(obj.x + 1, obj.y, obj.width - 1,
								obj.height);
						col = new Rectangle(obj.x + 1, obj.y, 1, obj.height);
						histCol = histogram(col);
					}

					col = new Rectangle(obj.x + obj.width, obj.y, 1, obj.height);
					histCol = histogram(col);
					while (histCol[511] >= obj.height * 0.8 && obj.height > 10) {
						obj.setSize(obj.width - 1, obj.height);
						col = new Rectangle(obj.x + obj.width, obj.y, 1,
								obj.height);
						histCol = histogram(col);
					}
				}

				if (obj.width > obj.height)
					continue;

				if ((hist[345] > Math.max(32, 0.1 * obj.width * obj.height))
						&& (hist[64] != 0)) {
					obj.add(new Rectangle(obj.x - obj.width / 10, obj.y
							- obj.height / 3, obj.width / 10 * 12,
							obj.height / 3 * 4));
					return obj;
				}
			}
		}
		return null;
	}

	// find pigs in the current scene
	public List<Rectangle> findPigsMBR() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// find candidates
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 376) || ignore[n])
				continue;

			// dilate bounding box of colour 376
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n],
					_boxes[n].width / 2 + 1, _boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 376
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 376)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], _boxes[m].width / 2 + 1,
						_boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colour 250
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (_colours[m] != 250)
					continue;
				if (bounds.intersects(_boxes[m])) {
					bValidObject = true;
					break;
				}
			}

			// add object if valid
			if (bValidObject) {
				obj = VisionUtils.dialateRectangle(obj, obj.width / 2 + 1,
						obj.height / 2 + 1);
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	// find birds in the current scene
	public List<Rectangle> findRedBirdsMBRs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for red birds (385, 488, 501)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 385) || ignore[n])
				continue;

			// dilate bounding box around colour 385
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 385
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 385)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 1, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488 and 501
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if ((_colours[m] != 488) && (_colours[m] != 501))
					continue;
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findBlueBirdsMBRs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (238)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 238) || ignore[n])
				continue;

			// dilate bounding box around colour 238
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 238, 165, 280,
			// 344, 488, 416
			for (int m = n + 1; m < _nSegments; m++) {
				if ((_colours[m] != 238) && (_colours[m] != 165)
						&& (_colours[m] != 280) && (_colours[m] != 344)
						&& (_colours[m] != 488) && (_colours[m] != 416))
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 238)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (_colours[m] != 488)
					continue;
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject && (obj.width > 3)) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findYellowBirdsMBRs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (497)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 497) || ignore[n])
				continue;

			// dilate bounding box around colour 497
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 497
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 497)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colours 288
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			int[] hist = histogram(obj);
			if (hist[288] > 0) {
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findWhiteBirdsMBRs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for white birds (490)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 490) || ignore[n])
				continue;

			// dilate bounding box around colour 490
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 490
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 490
						&& _colours[m] != 508
						&& _colours[m] != 510)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colour 510
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			   // remove objects too high or too low in the image 
			// (probably false positives)
			if ((obj.y < 60) || (obj.y > 385)) {
				continue;
		                     }
			int[] hist = histogram(obj);
			if (hist[510] > 0 && hist[508] > 0) {
				objects.add(obj);
			}
		}

		return objects;
	}
	public List<ABObject> findBlocks(){
		List<Rectangle> stone = findStonesMBR();
		List<Rectangle> wood = findWoodMBR();
		List<Rectangle> ice = findIceMBR();
		List<ABObject> objects = new LinkedList<ABObject>();
		objects.addAll(constructABObjects(stone,ABType.Stone));
		objects.addAll(constructABObjects(wood,ABType.Wood));
		objects.addAll(constructABObjects(ice,ABType.Ice));
		return objects;
	}
	public List<ABObject> findBirds(){
		
		List<Rectangle> rbirds = findRedBirdsMBRs();
		List<Rectangle> ybirds = findYellowBirdsMBRs();
		List<Rectangle> blbirds = findBlueBirdsMBRs();
		List<Rectangle> blackbirds = findBlackBirdsMBRs();
		List<Rectangle> wbirds = findWhiteBirdsMBRs();
		List<ABObject> objects = new LinkedList<ABObject>();
		objects.addAll(constructABObjects(rbirds, ABType.RedBird));
		objects.addAll(constructABObjects(ybirds,ABType.YellowBird));
		objects.addAll(constructABObjects(blbirds,ABType.BlueBird));
		objects.addAll(constructABObjects(blackbirds,ABType.BlackBird));
		objects.addAll(constructABObjects(wbirds,ABType.WhiteBird));
		return objects;
	}
	
	public List<ABObject> findPigs(){
	
		return constructABObjects(findPigsMBR(), ABType.Pig);
		}

	private List<ABObject> constructABObjects(List<Rectangle> mbrs, ABType type)
	{
	
		List<ABObject> objects = new LinkedList<ABObject>();
		if(type == ABType.Wood || type == ABType.Ice || type == ABType.Stone || type == ABType.TNT)
			for(Rectangle rec: mbrs)
				objects.add(new ABObject(rec, type));
		else 
			if(type == ABType.Pig)
				for(Rectangle rec: mbrs)
					objects.add(new ABObject(rec, type));
		else
			for(Rectangle rec: mbrs)
				objects.add(new ABObject(rec, type));
		return objects;
	}
	public List<Rectangle> findBlackBirdsMBRs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for white birds (488)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 488) || ignore[n])
				continue;

			// dilate bounding box around colour 488
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 488
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 488
						&& _colours[m] != 146
						&& _colours[m] != 64
						&& _colours[m] != 0)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colour
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			int[] hist = histogram(obj);
			if ((hist[0] > Math.max(32, 0.1 * obj.width * obj.height))&& hist[64] > 0 && hist[385] == 0) {
				objects.add(obj);
			}
		}

		return objects;
	}
	public List<Rectangle> findStonesMBR() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 365) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to stone
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 365)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 365)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 365)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 365)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findIceMBR() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

	
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 311) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to ice
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 311
								|| _scene[p.y + 1][p.x] == 247 || _scene[p.y + 1][p.x] == 183)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 311
								|| _scene[p.y][p.x + 1] == 247 || _scene[p.y][p.x + 1] == 183)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 311
								|| _scene[p.y - 1][p.x] == 247 || _scene[p.y - 1][p.x] == 183)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 311
								|| _scene[p.y][p.x - 1] == 247 || _scene[p.y][p.x - 1] == 183)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findWoodMBR() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		
		// Boolean ignore[] = new Boolean[_nSegments];
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 481) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				List<Point> pointBag = new ArrayList<Point>();
				l.add(new Point(j, i));
				pointBag.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to wood
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 481
								|| _scene[p.y + 1][p.x] == 408 || _scene[p.y + 1][p.x] == 417)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
							pointBag.add(new Point(p.x, p.y + 1));
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 481
								|| _scene[p.y][p.x + 1] == 408 || _scene[p.y][p.x + 1] == 417)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
							pointBag.add(new Point(p.x + 1, p.y));
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 481
								|| _scene[p.y - 1][p.x] == 408 || _scene[p.y - 1][p.x] == 417)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
							pointBag.add(new Point(p.x, p.y - 1));
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 481
								|| _scene[p.y][p.x - 1] == 408 || _scene[p.y][p.x - 1] == 417)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
							pointBag.add(new Point(p.x - 1, p.y));
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}

		return objects;
	}
	
	public List<ABObject> findTNTs()
	{
		List<Rectangle> tnts = findTNTsMBR();
		return constructABObjects(tnts, ABType.TNT);
	}
	public List<Rectangle> findTNTsMBR() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 410) || ignore[n])
				continue;

			// dilate bounding box around colour 410
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 410
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 410
						&& _colours[m] != 418)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			
			//check secondary colour
			int[] hist = histogram(obj);
			if (hist[457] > 0 && hist[511] > 0) {
				objects.add(obj);
			}
		}
		
		return objects;
	}



	// find trajectory points
	@SuppressWarnings("unchecked")
	public ArrayList<Point> findTrajPoints() {
		ArrayList<Point> objects = new ArrayList<Point>();
		ArrayList<Point> objectsRemovedNoise;

		
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 365 && _scene[i][j] != 366 && _scene[i][j] != 438)
						|| ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to traj Points
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0) {
						if ((_scene[p.y + 1][p.x] == 365
								|| _scene[p.y + 1][p.x] == 366 || _scene[p.y + 1][p.x] == 438)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}

						if ((_scene[p.y][p.x + 1] == 365
								|| _scene[p.y][p.x + 1] == 366 || _scene[p.y][p.x + 1] == 438)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

						if ((_scene[p.y - 1][p.x] == 365
								|| _scene[p.y - 1][p.x] == 366 || _scene[p.y - 1][p.x] == 438)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

						if ((_scene[p.y][p.x - 1] == 365
								|| _scene[p.y][p.x - 1] == 366 || _scene[p.y][p.x - 1] == 438)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

						if ((_scene[p.y - 1][p.x - 1] == 365
								|| _scene[p.y - 1][p.x - 1] == 366 || _scene[p.y - 1][p.x - 1] == 438)
								&& !ignorePixel[p.y - 1][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y - 1));
							obj.add(p.x - 1, p.y - 1);
						}

						if ((_scene[p.y - 1][p.x + 1] == 365
								|| _scene[p.y - 1][p.x + 1] == 366 || _scene[p.y - 1][p.x + 1] == 438)
								&& !ignorePixel[p.y - 1][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y - 1));
							obj.add(p.x + 1, p.y - 1);
						}

						if ((_scene[p.y + 1][p.x + 1] == 365
								|| _scene[p.y + 1][p.x + 1] == 366 || _scene[p.y + 1][p.x + 1] == 438)
								&& !ignorePixel[p.y + 1][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y + 1));
							obj.add(p.x + 1, p.y + 1);
						}

						if ((_scene[p.y + 1][p.x - 1] == 365
								|| _scene[p.y + 1][p.x - 1] == 366 || _scene[p.y + 1][p.x - 1] == 438)
								&& !ignorePixel[p.y + 1][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y + 1));
							obj.add(p.x - 1, p.y + 1);
						}

					}
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0) {
						ignorePixel[p.y + 1][p.x] = true;
						ignorePixel[p.y][p.x + 1] = true;

						ignorePixel[p.y - 1][p.x] = true;
						ignorePixel[p.y][p.x - 1] = true;

						ignorePixel[p.y + 1][p.x + 1] = true;
						ignorePixel[p.y - 1][p.x + 1] = true;
						ignorePixel[p.y + 1][p.x - 1] = true;
						ignorePixel[p.y - 1][p.x - 1] = true;
					}
				}

				
				if (obj.height * obj.width <= 25)
					objects.add(new Point((int) obj.getCenterX(), (int) obj
							.getCenterY()));
			}
		}

		objectsRemovedNoise = (ArrayList<Point>) objects.clone();

		// remove noise points
		Matrix W = fitParabola(objects);
		double maxError = 10;
		Rectangle menu = new Rectangle(0, 0, 205, 60);

		for (Point o : objects) {
			if (Math.abs(W.get(0, 0) * Math.pow(o.x, 2) + W.get(1, 0) * o.x
					+ W.get(2, 0) - o.y) > maxError) {
				objectsRemovedNoise.remove(o);
			}

			if (menu.contains(o)) {
				objectsRemovedNoise.remove(o);
			}
		}

		return objectsRemovedNoise;
	}

	//fit parabola using maximum likelihood
	// vector W = (w0,w1,w2)T , y = w0*x^2 + w1*x + w2
	public Matrix fitParabola(List<Point> objects) {
		int trainingSize = 60;
		double arrayPhiX[][] = new double[trainingSize][3]; // Training set
		double arrayY[][] = new double[trainingSize][1];

		Rectangle sling = this.findSlingshotMBR();

		Matrix PhiX, Y;
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);
		int i = 0;
		for (Point p : objects) {
			
			//if slingshot not detected, abandon side noises 
			if (sling == null) {
				if (Math.abs(p.x - _nWidth / 2) <= _nWidth / 6
						&& p.y <= _nHeight / 5 * 3 && i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			} 
			
			// if slingshot detected, abandon noises to the left of slingshot
			else {
				if (p.x >= sling.getCenterX() + sling.width * 2
						&& p.x <= sling.getCenterX() + _nWidth / 3
						&& p.y <= sling.getCenterY() && i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			}
		}

		PhiX = new Matrix(arrayPhiX);
		Y = new Matrix(arrayY);

		// Maximum likelihood
		try {
			W = PhiX.transpose().times(PhiX).inverse().times(PhiX.transpose())
					.times(Y);
		} catch (Exception e) {
			// if Matrix is singular
			// do nothing
		}
		return W;
	}

	// train parabola using gradient descent
	public Matrix trainParabola(ArrayList<Rectangle> objects) {

		double points[][] = new double[objects.size()][2];
		double alpha = 1e-10;
		int trainingSize = 100;

		double trainingSet[][] = new double[trainingSize][2];
		double SquareError;
		Matrix deltaError;

		int i = 0, j = 0;
		for (Rectangle p : objects) {
			points[i][0] = p.getCenterX();
			points[i][1] = p.getCenterY();
			if (Math.abs(p.getCenterX() - _nWidth / 2) <= _nWidth / 4
					&& Math.abs(p.getCenterY() - _nHeight / 2) <= _nHeight / 5
					&& j < trainingSize) {
				trainingSet[j][0] = points[i][0];
				trainingSet[j][1] = points[i][1];
				j++;
			}
			i++;
		}

	
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);// parabola
															// parameters
		
		Matrix phiX;
		for (int x = -50; x < 50; x++) {
			if (x + 50 < trainingSize) {
				trainingSet[x + 50][0] = x;
				trainingSet[x + 50][1] = -x * x + 20 * x + 1;
			}
		}

		for (int it = 0; it < 50000; it++) {
			SquareError = 0.;
			for (int n = 0; n < trainingSize; n++) {
				if (trainingSet[n][0] > 0) {
					double xn = trainingSet[n][0];
					double yn = trainingSet[n][1];
					phiX = new Matrix(new double[] { Math.pow(xn, 2), xn, 1. },
							3);

					deltaError = phiX.times((yn - W.transpose().times(phiX)
							.get(0, 0)));
					

					W = W.plus(deltaError.times(alpha));
					SquareError += Math.pow(
							yn - phiX.transpose().times(W).get(0, 0), 2);

				}
			}
			if (it % 1000 == 0) {
				System.out.print(SquareError + "\n");
				W.print(1, 30);
			}
		}

		return W;
	}

	// find bounding boxes around an arbitrary colour code
	public List<Rectangle> findColour(int colourCode) {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		for (int n = 0; n < _nSegments; n++) {
			if (_colours[n] == colourCode) {
				objects.add(_boxes[n]);
			}
		}

		return objects;
	}

	// query the colour at given pixel
	public Integer query(Point p) {
		if ((p.x >= _nWidth) || (p.y >= _nHeight)) {
			System.err.println("pixel (" + p.x + ", " + p.y
					+ ") is out of range");
			return null;
		}

		return _colours[_segments[p.y][p.x]];
	}

	// query colours within given bounding box
	public Set<Integer> query(Rectangle r) {
		Set<Integer> s = new HashSet<Integer>();
		for (int n = 0; n < _nSegments; n++) {
			if (r.contains(_boxes[n])) {
				s.add(_colours[n]);
			}
		}
		return s;
	}

	// compute a histogram of colours within a given bounding box
	public int[] histogram(Rectangle r) {
		int[] h = new int[512];
		Arrays.fill(h, 0);

		for (int y = r.y; y < r.y + r.height; y++) {
			if ((y < 0) || (y >= _nHeight))
				continue;
			for (int x = r.x; x < r.x + r.width; x++) {
				if ((x < 0) || (x >= _nWidth))
					continue;
				h[_colours[_segments[y][x]]] += 1;
			}
		}

		return h;
	}

	// perform preprocessing of a new screenshot
	private void processScreenShot(BufferedImage screenshot) {
		// extract width and height
		_nHeight = screenshot.getHeight();
		_nWidth = screenshot.getWidth();
		if ((_nHeight != 480) && (_nWidth != 840)) {
			System.err.println("ERROR: expecting 840-by-480 image");
			System.exit(1);
		}

		// quantize to 3-bit colour
		_scene = new int[_nHeight][_nWidth];
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				final int colour = screenshot.getRGB(x, y);
				_scene[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
				//System.out.println(" x " + x + " y " + y + " 3-bit rgb " + _scene[y][x]);
			}
		}

		// find connected components
		_segments = VisionUtils.findConnectedComponents(_scene);
		_nSegments = VisionUtils.countComponents(_segments);
		// System.out.println("...found " + _nSegments + " components");

		_colours = new int[_nSegments];
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				_colours[_segments[y][x]] = _scene[y][x];
			}
		}

		// find bounding boxes and segment colours
		_boxes = VisionUtils.findBoundingBoxes(_segments);
	}


}
