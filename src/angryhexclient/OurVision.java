/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, 2015,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/

package angryhexclient;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import Jama.Matrix;
import ab.vision.BirdType;
import ab.vision.VisionUtils;

/* OurVision ----------------------------------------------------------------- */

public class OurVision
{

	private int _nHeight; // height of the scene
	private int _nWidth; // width of the scene
	private int _scene[][]; // quantized scene colours
	private int _nSegments; // number of segments
	private int _segments[][]; // connected components (0 to _nSegments)
	private int _colours[]; // colour for each segment
	private Rectangle _boxes[]; // bounding box for each segment
	private int _regionThreshold = 10; // minimal pixels in a region

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	// create a vision object for processing a given screenshot
	public OurVision(BufferedImage screenshot)
	{
		processScreenShot(screenshot);
	}

	// find slingshot
	// only return one rectangle
	public Rectangle findSlingshot()
	{
		Rectangle obj;

		// test for slingshot (mainly 345)
		//commented out because it's not used
//		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				if ((_scene[i][j] != 345) || ignorePixel[i][j])
					continue;
				obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();

				LinkedList<Point> pointsinRec = new LinkedList<Point>();

				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true)
				{
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to slingshot

					// check underneath pixel
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 345
								|| _scene[p.y + 1][p.x] == 418
								|| _scene[p.y + 1][p.x] == 273
								|| _scene[p.y + 1][p.x] == 281
								|| _scene[p.y + 1][p.x] == 209
								|| _scene[p.y + 1][p.x] == 346
								|| _scene[p.y + 1][p.x] == 354
								|| _scene[p.y + 1][p.x] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y + 1][p.x])
						{
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					// check right pixel
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 345
								|| _scene[p.y][p.x + 1] == 418
								|| _scene[p.y][p.x + 1] == 346
								|| _scene[p.y][p.x + 1] == 354
								|| _scene[p.y][p.x + 1] == 273
								|| _scene[p.y][p.x + 1] == 281
								|| _scene[p.y][p.x + 1] == 209
								|| _scene[p.y][p.x + 1] == 282 || _scene[p.y][p.x + 1] == 351)
								&& !ignorePixel[p.y][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					// check upper pixel
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 345
								|| _scene[p.y - 1][p.x] == 418
								|| _scene[p.y - 1][p.x] == 346
								|| _scene[p.y - 1][p.x] == 354
								|| _scene[p.y - 1][p.x] == 273
								|| _scene[p.y - 1][p.x] == 281
								|| _scene[p.y - 1][p.x] == 209
								|| _scene[p.y - 1][p.x] == 282 || _scene[p.y - 1][p.x] == 351)
								&& !ignorePixel[p.y - 1][p.x])
						{
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					// check left pixel
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 345
								|| _scene[p.y][p.x - 1] == 418
								|| _scene[p.y][p.x - 1] == 346
								|| _scene[p.y][p.x - 1] == 354
								|| _scene[p.y][p.x - 1] == 273
								|| _scene[p.y][p.x - 1] == 281
								|| _scene[p.y][p.x - 1] == 209
								|| _scene[p.y][p.x - 1] == 282 || _scene[p.y][p.x - 1] == 351)
								&& !ignorePixel[p.y][p.x - 1])
						{
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					// ignore checked pixels
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
				if (obj.height > 10)
				{
					Rectangle col = new Rectangle(obj.x, obj.y, 1, obj.height);
					int[] histCol = histogram(col);
					//commented out because it's not used
//					int ColColour = histCol[345] + histCol[418] + histCol[346]
//							+ histCol[354] + histCol[273] + histCol[281]
//							+ histCol[209] + histCol[280] + histCol[351];

					if (_scene[obj.y][obj.x] == 511
							|| _scene[obj.y][obj.x] == 447)
					{
						for (int m = obj.y; m < obj.y + obj.height; m++)
						{
							if (_scene[m][obj.x] == 345
									|| _scene[m][obj.x] == 418
									|| _scene[m][obj.x] == 346
									|| _scene[m][obj.x] == 354
									|| _scene[m][obj.x] == 273
									|| _scene[m][obj.x] == 281
									|| _scene[m][obj.x] == 209
									|| _scene[m][obj.x] == 282
									|| _scene[m][obj.x] == 351)
							{
								obj.setSize(obj.width, m - obj.y);
								break;
							}
						}
					}

					while (histCol[511] >= obj.height * 0.8)
					{
						obj.setBounds(obj.x + 1, obj.y, obj.width - 1,
								obj.height);
						col = new Rectangle(obj.x + 1, obj.y, 1, obj.height);
						histCol = histogram(col);
					}

					col = new Rectangle(obj.x + obj.width, obj.y, 1, obj.height);
					histCol = histogram(col);
					while (histCol[511] >= obj.height * 0.8 && obj.height > 10)
					{
						obj.setSize(obj.width - 1, obj.height);
						col = new Rectangle(obj.x + obj.width, obj.y, 1,
								obj.height);
						histCol = histogram(col);
					}
				}

				if (obj.width > obj.height)
					continue;

				if ((hist[345] > Math.max(32, 0.1 * obj.width * obj.height))
						&& (hist[64] != 0))
				{
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
	public List<Rectangle> findPigs()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// find candidates
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 376) || ignore[n])
				continue;

			// dilate bounding box of colour 376
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n],
					_boxes[n].width / 2 + 1, _boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 376
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 376)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], _boxes[m].width / 2 + 1,
						_boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colour 250
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++)
			{
				if (_colours[m] != 250)
					continue;
				if (bounds.intersects(_boxes[m]))
				{
					bValidObject = true;
					break;
				}
			}

			// add object if valid
			if (bValidObject)
			{
				obj = VisionUtils.dialateRectangle(obj, obj.width / 2 + 1,
						obj.height / 2 + 1);
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findBirds(BirdType b)
	{
		//System.out.println("Finding "+b.name()+" birds.");
		switch (b)
		{
			case red:
				return findRedBirds();
			case yellow:
				return findYellowBirds();
			case blue:
				return findBlueBirds();
			case white:
				return findWhiteBirds();
			case black:
				return findBlackBirds();
			default:
				// very useful to understand if someone (perhaps the organizers)
				// changes the enum BirdType and we forgot to update this switch
				Log.warning("Unknown Bird Type");
				return null;
		}
	}
	
	// find birds in the current scene
	public List<Rectangle> findRedBirds()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for red birds (385, 488, 501)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 385) || ignore[n])
				continue;

			// dilate bounding box around colour 385
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 385
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 385)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 1, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488 and 501
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++)
			{
				if ((_colours[m] != 488) && (_colours[m] != 501))
					continue;
				if (bounds.intersects(_boxes[m]))
				{
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject)
			{
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findBlueBirds()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (238)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 238) || ignore[n])
				continue;

			// dilate bounding box around colour 238
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 238, 165, 280,
			// 344, 488, 416
			for (int m = n + 1; m < _nSegments; m++)
			{
				if ((_colours[m] != 238) && (_colours[m] != 165)
						&& (_colours[m] != 280) && (_colours[m] != 344)
						&& (_colours[m] != 488) && (_colours[m] != 416))
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 238)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2))
				{
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++)
			{
				if (_colours[m] != 488)
					continue;
				if (bounds.intersects(_boxes[m]))
				{
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject && (obj.width > 3))
			{
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findYellowBirds()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (497)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 497) || ignore[n])
				continue;

			// dilate bounding box around colour 497
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 497
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 497)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colours 288
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			int[] hist = histogram(obj);
			if (hist[288] > 0)
			{
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findWhiteBirds()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for white birds (490)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 490) || ignore[n])
				continue;

			// dilate bounding box around colour 490
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 490
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 490 && _colours[m] != 508
						&& _colours[m] != 510)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colour 510
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			
			// Jochen's patch July 25th 2013
            // remove objects too high or too low in the image 
            // (probably false positives)
            if ((obj.y < 60) || (obj.y > 385)) {
                    continue;
                         }

			
			int[] hist = histogram(obj);
			if (hist[510] > 0 && hist[508] > 0)
			{
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findBlackBirds()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for white birds (488)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 488) || ignore[n])
				continue;

			// dilate bounding box around colour 488
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 488
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 488 && _colours[m] != 146
						&& _colours[m] != 64 && _colours[m] != 0)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colour
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			int[] hist = histogram(obj);
			if ((hist[0] > Math.max(32, 0.1 * obj.width * obj.height))
					&& hist[64] > 0 && hist[385] == 0)
			{
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findStonesAsRectangles()
	{
		List<Rectangle> retValue = new ArrayList<Rectangle>();
		for (Block b : findStones())
		{
			retValue.add(b.rectangle);
		}
		return retValue;

	}

	public List<Block> findStones()
	{
		ArrayList<Block> objects = new ArrayList<Block>();

		// test for stone (mainly 365)
		//commented out because it's not used
//		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				if ((_scene[i][j] != 365) || ignorePixel[i][j])
					continue;
				Block obj = new Block(j, i);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true)
				{
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to stone
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 365)
								&& !ignorePixel[p.y + 1][p.x])
						{
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 365)
								&& !ignorePixel[p.y][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 365)
								&& !ignorePixel[p.y - 1][p.x])
						{
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 365)
								&& !ignorePixel[p.y][p.x - 1])
						{
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
				if (obj.rectangle.width * obj.rectangle.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55)
								.contains(obj.rectangle)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findIceAsRectangles()
	{
		List<Rectangle> retValue = new ArrayList<Rectangle>();
		for (Block b : findIce())
		{
			retValue.add(b.rectangle);
		}
		return retValue;

	}

	
	public List<Block> findIce()
	{
		ArrayList<Block> objects = new ArrayList<Block>();

		// test for ice (mainly 311)
		//commented out because it's not used
//		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				if ((_scene[i][j] != 311) || ignorePixel[i][j])
					continue;
				Block obj = new Block(j, i);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true)
				{
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to ice
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 311
								|| _scene[p.y + 1][p.x] == 247 || _scene[p.y + 1][p.x] == 183)
								&& !ignorePixel[p.y + 1][p.x])
						{
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 311
								|| _scene[p.y][p.x + 1] == 247 || _scene[p.y][p.x + 1] == 183)
								&& !ignorePixel[p.y][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 311
								|| _scene[p.y - 1][p.x] == 247 || _scene[p.y - 1][p.x] == 183)
								&& !ignorePixel[p.y - 1][p.x])
						{
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 311
								|| _scene[p.y][p.x - 1] == 247 || _scene[p.y][p.x - 1] == 183)
								&& !ignorePixel[p.y][p.x - 1])
						{
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
				if (obj.rectangle.width * obj.rectangle.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55)
								.contains(obj.rectangle)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findWoodAsRectangles()
	{
		List<Rectangle> retValue = new ArrayList<Rectangle>();
		for (Block b : findWood())
		{
			retValue.add(b.rectangle);
		}
		return retValue;
	}
	
	public List<Block> findWood()
	{
		ArrayList<Block> objects = new ArrayList<Block>();

		// test for wood (mainly 481)
		//commented out because it's not used
//		int nPixel = _nWidth * _nHeight;
		// Boolean ignore[] = new Boolean[_nSegments];
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				if ((_scene[i][j] != 481) || ignorePixel[i][j])
					continue;
				Block obj = new Block(j, i);
				LinkedList<Point> l = new LinkedList<Point>();
				List<Point> pointBag = new ArrayList<Point>();
				l.add(new Point(j, i));
				pointBag.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true)
				{
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to wood
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 481
								|| _scene[p.y + 1][p.x] == 408 || _scene[p.y + 1][p.x] == 417)
								&& !ignorePixel[p.y + 1][p.x])
						{
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
							pointBag.add(new Point(p.x, p.y + 1));
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 481
								|| _scene[p.y][p.x + 1] == 408 || _scene[p.y][p.x + 1] == 417)
								&& !ignorePixel[p.y][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
							pointBag.add(new Point(p.x + 1, p.y));
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 481
								|| _scene[p.y - 1][p.x] == 408 || _scene[p.y - 1][p.x] == 417)
								&& !ignorePixel[p.y - 1][p.x])
						{
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
							pointBag.add(new Point(p.x, p.y - 1));
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 481
								|| _scene[p.y][p.x - 1] == 408 || _scene[p.y][p.x - 1] == 417)
								&& !ignorePixel[p.y][p.x - 1])
						{
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
				if (obj.rectangle.width * obj.rectangle.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55)
								.contains(obj.rectangle)))
					objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findTNTs()
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++)
		{
			if ((_colours[n] != 410) || ignore[n])
				continue;

			// dilate bounding box around colour 410
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			
			// look for overlapping bounding boxes of colour 410
			for (int m = n + 1; m < _nSegments; m++)
			{
				if (_colours[m] != 410 && _colours[m] != 418)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2))
				{
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);

			// check secondary colour
			int[] hist = histogram(obj);
			if (hist[457] > 0 && hist[511] > 0)
			{
				objects.add(obj);
			}
		}

		return objects;
	}

	// find trajectory points
	public ArrayList<Point> findTrajPoints()
	{
		ArrayList<Point> objects = new ArrayList<Point>();
		ArrayList<Point> objectsRemovedNoise;

		// test for trajectory points
		//commented out because it's not used
//		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++)
		{
			for (int j = 0; j < _nWidth; j++)
			{
				if ((_scene[i][j] != 365 && _scene[i][j] != 366 && _scene[i][j] != 438)
						|| ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true)
				{
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to traj Points
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0)
					{
						if ((_scene[p.y + 1][p.x] == 365
								|| _scene[p.y + 1][p.x] == 366 || _scene[p.y + 1][p.x] == 438)
								&& !ignorePixel[p.y + 1][p.x])
						{
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}

						if ((_scene[p.y][p.x + 1] == 365
								|| _scene[p.y][p.x + 1] == 366 || _scene[p.y][p.x + 1] == 438)
								&& !ignorePixel[p.y][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

						if ((_scene[p.y - 1][p.x] == 365
								|| _scene[p.y - 1][p.x] == 366 || _scene[p.y - 1][p.x] == 438)
								&& !ignorePixel[p.y - 1][p.x])
						{
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

						if ((_scene[p.y][p.x - 1] == 365
								|| _scene[p.y][p.x - 1] == 366 || _scene[p.y][p.x - 1] == 438)
								&& !ignorePixel[p.y][p.x - 1])
						{
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

						if ((_scene[p.y - 1][p.x - 1] == 365
								|| _scene[p.y - 1][p.x - 1] == 366 || _scene[p.y - 1][p.x - 1] == 438)
								&& !ignorePixel[p.y - 1][p.x - 1])
						{
							l.add(new Point(p.x - 1, p.y - 1));
							obj.add(p.x - 1, p.y - 1);
						}

						if ((_scene[p.y - 1][p.x + 1] == 365
								|| _scene[p.y - 1][p.x + 1] == 366 || _scene[p.y - 1][p.x + 1] == 438)
								&& !ignorePixel[p.y - 1][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y - 1));
							obj.add(p.x + 1, p.y - 1);
						}

						if ((_scene[p.y + 1][p.x + 1] == 365
								|| _scene[p.y + 1][p.x + 1] == 366 || _scene[p.y + 1][p.x + 1] == 438)
								&& !ignorePixel[p.y + 1][p.x + 1])
						{
							l.add(new Point(p.x + 1, p.y + 1));
							obj.add(p.x + 1, p.y + 1);
						}

						if ((_scene[p.y + 1][p.x - 1] == 365
								|| _scene[p.y + 1][p.x - 1] == 366 || _scene[p.y + 1][p.x - 1] == 438)
								&& !ignorePixel[p.y + 1][p.x - 1])
						{
							l.add(new Point(p.x - 1, p.y + 1));
							obj.add(p.x - 1, p.y + 1);
						}

					}
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0)
					{
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

				//commented out because it's not used
//				Rectangle menu = new Rectangle(0, 0, 205, 60);
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

		for (Point o : objects)
		{
			if (Math.abs(W.get(0, 0) * Math.pow(o.x, 2) + W.get(1, 0) * o.x
					+ W.get(2, 0) - o.y) > maxError)
			{
				objectsRemovedNoise.remove(o);
			}

			if (menu.contains(o))
			{
				objectsRemovedNoise.remove(o);
			}
		}

		return objectsRemovedNoise;
	}

	// fit parabola using maximum likelihood
	// vector W = (w0,w1,w2)T , y = w0*x^2 + w1*x + w2
	public Matrix fitParabola(List<Point> objects)
	{
		int trainingSize = 60;
		double arrayPhiX[][] = new double[trainingSize][3]; // Training set
		double arrayY[][] = new double[trainingSize][1];

		Rectangle sling = this.findSlingshot();

		Matrix PhiX, Y;
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);
		int i = 0;
		for (Point p : objects)
		{

			// if slingshot not detected, abandon side noises
			if (sling == null)
			{
				if (Math.abs(p.x - _nWidth / 2) <= _nWidth / 6
						&& p.y <= _nHeight / 5 * 3 && i < trainingSize)
				{
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			}

			// if slingshot detected, abandon noises to the left of slingshot
			else
			{
				if (p.x >= sling.getCenterX() + sling.width * 2
						&& p.x <= sling.getCenterX() + _nWidth / 3
						&& p.y <= sling.getCenterY() && i < trainingSize)
				{
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
		try
		{
			W = PhiX.transpose().times(PhiX).inverse().times(PhiX.transpose())
					.times(Y);
		}
		catch (Exception e)
		{
			// if Matrix is singular
			// do nothing
		}
		return W;
	}

	// train parabola using gradient descent
	public Matrix trainParabola(ArrayList<Rectangle> objects)
	{

		double points[][] = new double[objects.size()][2];
		double alpha = 1e-10;
		int trainingSize = 100;

		double trainingSet[][] = new double[trainingSize][2];
		double SquareError;
		Matrix deltaError;

		int i = 0, j = 0;
		for (Rectangle p : objects)
		{
			points[i][0] = p.getCenterX();
			points[i][1] = p.getCenterY();
			if (Math.abs(p.getCenterX() - _nWidth / 2) <= _nWidth / 4
					&& Math.abs(p.getCenterY() - _nHeight / 2) <= _nHeight / 5
					&& j < trainingSize)
			{
				trainingSet[j][0] = points[i][0];
				trainingSet[j][1] = points[i][1];
				j++;
			}
			i++;
		}

		//commented out because it's not used
//		Matrix T = new Matrix(trainingSet);// possible traj points matrix
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);// parabola
															// parameters
		//commented out because it's not used
//		Matrix oldW;
		Matrix phiX;
		for (int x = -50; x < 50; x++)
		{
			if (x + 50 < trainingSize)
			{
				trainingSet[x + 50][0] = x;
				trainingSet[x + 50][1] = -x * x + 20 * x + 1;
			}
		}

		for (int it = 0; it < 50000; it++)
		{
			SquareError = 0.;
			for (int n = 0; n < trainingSize; n++)
			{
				if (trainingSet[n][0] > 0)
				{
					double xn = trainingSet[n][0];
					double yn = trainingSet[n][1];
					phiX = new Matrix(new double[] { Math.pow(xn, 2), xn, 1. },
							3);

					deltaError = phiX.times((yn - W.transpose().times(phiX)
							.get(0, 0)));
					//commented out because it's not used
//					oldW = W;

					W = W.plus(deltaError.times(alpha));
					SquareError += Math.pow(
							yn - phiX.transpose().times(W).get(0, 0), 2);

				}
			}
			if (it % 1000 == 0)
			{
				System.out.print(SquareError + "\n");
				W.print(1, 30);
			}
		}

		return W;
	}

	// find bounding boxes around an arbitrary colour code
	public List<Rectangle> findColour(int colourCode)
	{
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		for (int n = 0; n < _nSegments; n++)
		{
			if (_colours[n] == colourCode)
			{
				objects.add(_boxes[n]);
			}
		}

		return objects;
	}

	// query the colour at given pixel
	public Integer query(Point p)
	{
		if ((p.x >= _nWidth) || (p.y >= _nHeight))
		{
			System.err.println("pixel (" + p.x + ", " + p.y
					+ ") is out of range");
			return null;
		}

		return _colours[_segments[p.y][p.x]];
	}

	// query colours within given bounding box
	public Set<Integer> query(Rectangle r)
	{
		Set<Integer> s = new HashSet<Integer>();
		for (int n = 0; n < _nSegments; n++)
		{
			if (r.contains(_boxes[n]))
			{
				s.add(_colours[n]);
			}
		}
		return s;
	}

	// compute a histogram of colours within a given bounding box
	public int[] histogram(Rectangle r)
	{
		int[] h = new int[512];
		Arrays.fill(h, 0);

		for (int y = r.y; y < r.y + r.height; y++)
		{
			if ((y < 0) || (y >= _nHeight))
				continue;
			for (int x = r.x; x < r.x + r.width; x++)
			{
				if ((x < 0) || (x >= _nWidth))
					continue;
				h[_colours[_segments[y][x]]] += 1;
			}
		}

		return h;
	}

	// perform preprocessing of a new screenshot
	private void processScreenShot(BufferedImage screenshot)
	{
		// extract width and height
		_nHeight = screenshot.getHeight();
		_nWidth = screenshot.getWidth();
		if ((_nHeight != 480) && (_nWidth != 840))
		{
			System.err.println("ERROR: expecting 840-by-480 image");
			System.exit(1);
		}

		// quantize to 3-bit colour
		_scene = new int[_nHeight][_nWidth];
		for (int y = 0; y < _nHeight; y++)
		{
			for (int x = 0; x < _nWidth; x++)
			{
				final int colour = screenshot.getRGB(x, y);
				_scene[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
			}
		}

		// find connected components
		_segments = VisionUtils.findConnectedComponents(_scene);
		_nSegments = VisionUtils.countComponents(_segments);
		// System.out.println("...found " + _nSegments + " components");

		_colours = new int[_nSegments];
		for (int y = 0; y < _nHeight; y++)
		{
			for (int x = 0; x < _nWidth; x++)
			{
				_colours[_segments[y][x]] = _scene[y][x];
			}
		}

		// find bounding boxes and segment colours
		_boxes = VisionUtils.findBoundingBoxes(_segments);
	}

	/**
	 * Detects the ground in the image.
	 * @return A list of blocks representing the ground.
	 */
	public List<Block> detectGround()
	{
		Mat binaryImage = new Mat(new Size(_nWidth, _nHeight), CvType.CV_8U,
				new Scalar(1));

		// We only detect right of this margin. The slingshot has some ground
		// colors and would partly be detected as ground. This is not what we
		// want. Trajectories originate at the slingshot, and if there is ground
		// detected at the slingshot, the agent will think, that none of its
		// trajectories are valid. Therefore we start with detecting due right
		// of the slingshot.
		int startAtX = findSlingshot().x + findSlingshot().width * 2;

		// Now we create a binary image of the ground areas. White where there
		// is ground, black otherwise.
		for (int y = 0; y < _nHeight; y++)
		{
			for (int x = 0; x < _nWidth; x++)
			{
				if (x > startAtX && isGround(x, y))
					binaryImage.put(y, x, 255);
				else
					binaryImage.put(y, x, 0);
			}
		}

		Mat smoothedImage = new Mat(new Size(_nWidth, _nHeight), CvType.CV_8U,
				new Scalar(1));

		// This median filter improves the detection tremendously. There are a
		// whole lot of single pixels that carry ground colors spread all over
		// the image. We remove them here.
		Imgproc.medianBlur(binaryImage, smoothedImage, 7);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		// We use OpenCV to find the contours. Contours are lines, that
		// represent the boundaries of the objects in the binary image.
		Imgproc.findContours(smoothedImage, contours, new Mat(),
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		ArrayList<Block> result = new ArrayList<Block>();

		//Now for every contour, we convert it to blocks for communicating them to DLV.
		for (MatOfPoint mp : contours)
		{
			org.opencv.core.Point[] pts = mp.toArray();

			for (int i = 0; i < pts.length - 1; i++)
			{
				Block b = new Block((int) pts[i].x, (int) pts[i].y);
				b.add((int) pts[i + 1].x, (int) pts[i + 1].y);
				result.add(b);
			}

			//One block for the first vertex to the last vertex.
			Block b = new Block((int) pts[pts.length - 1].x,
					(int) pts[pts.length - 1].y);
			b.add((int) pts[0].x, (int) pts[0].y);
			result.add(b);
		}

		return result;
	}

	/**
	 * Returns true if the pixel at the given location has typical ground
	 * colors. Colors have been determined manually by retrieving the color of
	 * ground pixels.
	 */
	private boolean isGround(int x, int y)
	{
		return _scene[y][x] == 64 || _scene[y][x] == 72 || _scene[y][x] == 136
				|| _scene[y][x] == 209 || _scene[y][x] == 210
				|| _scene[y][x] == 282 || _scene[y][x] == 346;
	}

	/**
	 * Information about a block, contains not only the bounding rectangle, but
	 * also all the pixels that make it up, so that they can also be used.
	 */
	public class Block
	{
		/**
		 * The bounding rectangle of the block.
		 */
		public Rectangle rectangle;

		/**
		 * All the pixels belonging to the block.
		 */
		public List<org.opencv.core.Point> pixels = new LinkedList<org.opencv.core.Point>();

		/**
		 * Adds the point to the rectangle as well as the pixel list.
		 */
		public void add(int x, int y)
		{
			rectangle.add(x, y);
			pixels.add(new org.opencv.core.Point(x, y));
		}

		public MatOfPoint2f getMatOfPoint2f()
		{
			MatOfPoint2f matrix = new MatOfPoint2f();
			matrix.fromList(pixels);
			return matrix;
		}

		public RotatedRect getRBoundingBox()
		{
			return Imgproc.minAreaRect(getMatOfPoint2f());
		}

		public Block(int x, int y)
		{
			rectangle = new Rectangle(x, y, 0, 0);
			pixels.add(new org.opencv.core.Point(x, y));
		}
	}

}
