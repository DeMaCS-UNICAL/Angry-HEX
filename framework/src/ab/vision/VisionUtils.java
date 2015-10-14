/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;

import Jama.Matrix;

/* VisionUtils ------------------------------------------------------------ */

public class VisionUtils {
	public static Color fontColor = Color.blue;
	public static Color boxesColor = Color.ORANGE;
      // crops a bounding box to be within an image
	public static Rectangle cropBoundingBox(Rectangle r, Image img) {
		return cropBoundingBox(r, img.getWidth(null), img.getHeight(null));
	}

	// crops a bounding box to be within an image of size width-by-height
	public static Rectangle cropBoundingBox(Rectangle r, int width, int height) {
		if (r.x < 0)
			r.x = 0;
		if (r.y < 0)
			r.y = 0;
		if ((r.x + r.width) > width)
			r.width = width - r.x;
		if ((r.y + r.height) > height)
			r.height = height - r.y;

		return r;
	}
	// compute the number of pixels different in two images
    public static int numPixelsDifferent(BufferedImage imgA, BufferedImage imgB) {

        int height = Math.min(imgA.getHeight(), imgB.getHeight());
        int width = Math.min(imgA.getWidth(), imgB.getWidth());
        int n = imgA.getWidth() * imgA.getHeight() + imgB.getWidth() * imgB.getHeight() - 2 * width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    n += 1;
                }
            }
        }

        return n;
    }


    // compute the absolute difference between two images
    public static int imageDifference(BufferedImage imgA, BufferedImage imgB) {

        int height = Math.min(imgA.getHeight(), imgB.getHeight());
        int width = Math.min(imgA.getWidth(), imgB.getWidth());

        int n = imgA.getWidth() * imgA.getHeight() + imgB.getWidth() * imgB.getHeight() - 2 * width * height;
        int diff = 3 * 255 * n;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int colourA = imgA.getRGB(x, y);
                final int colourB = imgB.getRGB(x, y);

                diff += Math.abs((int)((colourA & 0x00ff0000) >> 16) - (int)((colourB & 0x00ff0000) >> 16));
                diff += Math.abs((int)((colourA & 0x0000ff00) >> 8) - (int)((colourB & 0x0000ff00) >> 8));
                diff += Math.abs((int)(colourA & 0x000000ff) - (int)(colourB & 0x000000ff));
            }
        }

        return diff;
    }

	// compute an image digest
	public static String imageDigest(BufferedImage img) {

		// write image to byte stream
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", os);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		byte[] data = os.toByteArray();

		// compute md5 hash
		byte[] hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			hash = md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

		// convert to string
		String hexString = "";
		for (int i = 0; i < hash.length; i++) {
			hexString += Integer.toString((hash[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		return hexString;
	}

	// compute column checksums
	public static int[] columnChecksums(BufferedImage img) {

		int[] sums = new int[img.getWidth()];
		Arrays.fill(sums, 0);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				final int colour = img.getRGB(x, y);
				sums[x] += (int) ((colour & 0x00ff0000) >> 16);
				sums[x] += (int) ((colour & 0x0000ff00) >> 8);
				sums[x] += (int) (colour & 0x000000ff);
			}
		}

		for (int x = 0; x < img.getWidth(); x++) {
			sums[x] %= 256;
		}

		return sums;
	}

	// compute shape descriptor
	public static double[] shapeMoments(int[][] mask) {

		final int nHeight = mask.length;
		final int nWidth = mask[0].length;

		int nArea = 0;
		int xMoment = 0;
		int yMoment = 0;
		int xyMoment = 0;
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				if (mask[y][x] != 0) {
					nArea += 1;
					xMoment += (x - nWidth / 2);
					yMoment += (y - nHeight / 2);
					xyMoment += (x - nWidth / 2) * (y - nHeight / 2);
				}
			}
		}

		double[] moments = new double[5];

		moments[0] = (double) nWidth / (double) (nHeight + nWidth);
		moments[1] = (double) nArea / (double) (nHeight * nWidth);
		moments[2] = (double) xMoment / (double) (nArea * nWidth / 2);
		moments[3] = (double) yMoment / (double) (nArea * nHeight / 2);
		moments[4] = (double) xyMoment
				/ (double) (nArea * nWidth / 2 * nHeight / 2);

		return moments;
	}

	// converts an 2d integer array to image
	public static BufferedImage int2image(int[][] scene) {
		int maxValue = -1;
		int minValue = Integer.MAX_VALUE;
		for (int y = 0; y < scene.length; y++) {
			for (int x = 0; x < scene[y].length; x++) {
				maxValue = Math.max(maxValue, scene[y][x]);
				minValue = Math.min(minValue, scene[y][x]);
			}
		}

		if (maxValue == minValue)
			maxValue = minValue + 1;
		final double scale = 255.0 / (maxValue - minValue);

		BufferedImage image = new BufferedImage(scene[0].length, scene.length,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < scene.length; y++) {
			for (int x = 0; x < scene[y].length; x++) {
				final int c = (int) (scale * (scene[y][x] - minValue));
				image.setRGB(x, y, c << 16 | c << 8 | c);
			}
		}

		return image;
	}

	// convert a colour image to greyscale but retain 3-channels
	public static BufferedImage convert2grey(BufferedImage image) {
		BufferedImage grey = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = grey.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		BufferedImage rgb = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_RGB);
		g = rgb.getGraphics();
		g.drawImage(grey, 0, 0, null);
		g.dispose();
		return rgb;
	}

	// computes a hamming distance map to non-zero pixels
	public static int[][] computeDistanceMap(int[][] image) {

		// first pass: top-left to bottom-right
		for (int y = 0; y < image.length; y++) {
			for (int x = 0; x < image[y].length; x++) {
				if (image[y][x] != 0) {
					image[y][x] = 0;
				} else {
					image[y][x] = image.length + image[y].length;
					if (y > 0)
						image[y][x] = Math
								.min(image[y][x], image[y - 1][x] + 1);
					if (x > 0)
						image[y][x] = Math
								.min(image[y][x], image[y][x - 1] + 1);
				}
			}
		}

		// second pass: bottom-right to top-left
		for (int y = image.length - 1; y >= 0; y--) {
			for (int x = image[y].length - 1; x >= 0; x--) {
				if (y < image.length - 1)
					image[y][x] = Math.min(image[y][x], image[y + 1][x] + 1);
				if (x < image[y].length - 1)
					image[y][x] = Math.min(image[y][x], image[y][x + 1] + 1);
			}
		}

		return image;
	}

	// inverts binary image (i.e., swaps values zero and non-zero)
	public static int[][] invertRegions(int[][] image) {
		for (int y = 0; y < image.length; y++) {
			for (int x = 0; x < image[y].length; x++) {
				if (image[y][x] == 0) {
					image[y][x] = 1;
				} else {
					image[y][x] = 0;
				}
			}
		}
		return image;
	}

	// dilates non-zero regions image by k pixels
	public static int[][] dilateRegions(int[][] image, int k) {
		int[][] dimage = computeDistanceMap(image);
		for (int y = 0; y < dimage.length; y++) {
			for (int x = 0; x < dimage[y].length; x++) {
				dimage[y][x] = (dimage[y][x] <= k) ? 1 : 0;
			}
		}

		return dimage;
	}

	// erodes non-zero regions image by k pixels
	public static int[][] erodeRegions(int[][] image, int k) {
		return invertRegions(dilateRegions(invertRegions(image), k));
	}

	// dialates a bounding box by (dx, dy) pixels all around
	public static Rectangle dialateRectangle(Rectangle r, int dx, int dy) {
		return new Rectangle(r.x - dx, r.y - dy, r.width + 2 * dx, r.height + 2
				* dy);
	}

	// finds 4-connected components by breadth first search (and renumbers
	// from zero); pixels with negative value are ignored
	public static int[][] findConnectedComponents(int[][] image) {

		// renumbered components
		final int nHeight = image.length;
		final int nWidth = image[0].length;

		int n = -1;
		int[][] cc = new int[nHeight][nWidth];
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				cc[y][x] = -1;
			}
		}

		// iterate over all pixels
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				// skip negative pixels
				if (image[y][x] == -1)
					continue;

				// check if component was already numbered
				if (cc[y][x] != -1)
					continue;

				// number the new component
				n = n + 1;
				Queue<Point> q = new LinkedList<Point>();
				q.add(new Point(x, y));
				cc[y][x] = n;
				while (!q.isEmpty()) {
					Point p = q.poll();
					if ((p.y > 0) && (image[p.y - 1][p.x] == image[p.y][p.x])
							&& (cc[p.y - 1][p.x] == -1)) {
						q.add(new Point(p.x, p.y - 1));
						cc[p.y - 1][p.x] = n;
					}
					if ((p.x > 0) && (image[p.y][p.x - 1] == image[p.y][p.x])
							&& (cc[p.y][p.x - 1] == -1)) {
						q.add(new Point(p.x - 1, p.y));
						cc[p.y][p.x - 1] = n;
					}
					if ((p.y < nHeight - 1)
							&& (image[p.y + 1][p.x] == image[p.y][p.x])
							&& (cc[p.y + 1][p.x] == -1)) {
						q.add(new Point(p.x, p.y + 1));
						cc[p.y + 1][p.x] = n;
					}
					if ((p.x < nWidth - 1)
							&& (image[p.y][p.x + 1] == image[p.y][p.x])
							&& (cc[p.y][p.x + 1] == -1)) {
						q.add(new Point(p.x + 1, p.y));
						cc[p.y][p.x + 1] = n;
					}
				}
			}
		}

		return cc;
	}

	// returns number of components
	public static int countComponents(int[][] image) {
		int n = 0;
		for (int y = 0; y < image.length; y++) {
			for (int x = 0; x < image[y].length; x++) {
				n = Math.max(n, image[y][x] + 1);
			}
		}
		return n;
	}

	// returns bounding boxes for all connected components
	public static Rectangle[] findBoundingBoxes(int[][] image) {
		Rectangle[] boxes = new Rectangle[countComponents(image)];
		for (int y = 0; y < image.length; y++) {
			for (int x = 0; x < image[y].length; x++) {
				final int n = image[y][x];
				if (n < 0)
					continue;
				if (boxes[n] == null) {
					boxes[n] = new Rectangle(x, y, 1, 1);
				} else {
					boxes[n].add(x, y);
				}
			}
		}
		return boxes;
	}

	// draws a bounding box onto an image
	public static BufferedImage drawBoundingBox(BufferedImage canvas,
			Rectangle box, Color fgColour, Color bgColour) {
		Graphics2D g2d = canvas.createGraphics();
		g2d.setColor(bgColour);
		g2d.drawRect(box.x - 1, box.y - 1, box.width + 2, box.height + 2);
		g2d.drawRect(box.x + 1, box.y + 1, box.width - 2, box.height - 2);
		g2d.setColor(fgColour);
		g2d.drawRect(box.x, box.y, box.width, box.height);

		return canvas;
	}

	// draws a bounding box onto an image
	public static BufferedImage drawBoundingBox(BufferedImage canvas,
			Rectangle box, Color fgColour) {
		return drawBoundingBox(canvas, box, fgColour, Color.WHITE);
	}

	// draws bounding boxes onto an image
	public static BufferedImage drawBoundingBoxes(BufferedImage canvas,
			Rectangle[] boxes, Color fgColour, Color bgColour) {
		Graphics2D g2d = canvas.createGraphics();
		for (int i = 0; i < boxes.length; i++) {
			g2d.setColor(bgColour);
			g2d.drawRect(boxes[i].x - 1, boxes[i].y - 1, boxes[i].width + 2,
					boxes[i].height + 2);
			g2d.drawRect(boxes[i].x + 1, boxes[i].y + 1, boxes[i].width - 2,
					boxes[i].height - 2);
			g2d.setColor(fgColour);
			g2d.drawRect(boxes[i].x, boxes[i].y, boxes[i].width,
					boxes[i].height);
		}

		return canvas;
	}

	// draws bounding boxes onto an image
	public static BufferedImage drawBoundingBoxes(BufferedImage canvas,
			Rectangle[] boxes, Color fgColour) {
		return drawBoundingBoxes(canvas, boxes, fgColour, Color.WHITE);
	}

	// draw trajectory parabola
	public static BufferedImage drawtrajectory(BufferedImage canvas,
			int parabola[][], Color bgColour) {
		Graphics2D g2d = canvas.createGraphics();
		g2d.setColor(bgColour);
		g2d.drawPolyline(parabola[0], parabola[1], parabola[0].length);
		return canvas;
	}

	// draws bounding boxes onto an image
	public static BufferedImage drawBoundingBoxes(BufferedImage canvas,
			List<Rectangle> boxes, Color fgColour, Color bgColour) {
		Graphics2D g2d = canvas.createGraphics();
		for (Rectangle r : boxes) {
			g2d.setColor(bgColour);
			g2d.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
			g2d.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
			g2d.setColor(fgColour);
			g2d.drawRect(r.x, r.y, r.width, r.height);
		}

		return canvas;
	}
	//draw bounding boxes with IDs onto an image
	public static BufferedImage drawBoundingBoxesWithID(BufferedImage canvas, List<ABObject>boxes, 
			Color bgColour)
	{
		Graphics2D g2d = canvas.createGraphics();
		for (ABObject r : boxes) {
			g2d.setColor(bgColour);
			g2d.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
			g2d.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
			g2d.setColor(fontColor);
			g2d.drawRect(r.x, r.y, r.width, r.height);
			g2d.drawString(r.id + "", (int)r.getCenterX(), (int)r.getCenterY());
		}
		return canvas;
	}

	// draws bounding boxes onto an image
	public static BufferedImage drawBoundingPolygon(BufferedImage canvas,
			Polygon p, Color fgColour) {
		Graphics2D g2d = canvas.createGraphics();
		g2d.setColor(fgColour);
		g2d.drawPolygon(p);
		return canvas;
	}

	// draws bounding boxes onto an image
	public static BufferedImage drawBoundingBoxes(BufferedImage canvas,
			List<Rectangle> boxes, Color fgColour) {
		return drawBoundingBoxes(canvas, boxes, fgColour, Color.WHITE);
	}

	// highlight regions with a given id
	public static synchronized BufferedImage highlightRegions(Image img, int[][] regions,
			int regionId, Color fgColour) {
		BufferedImage canvas = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = canvas.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.setColor(fgColour);
		for (int y = 0; y < regions.length; y++) {
			for (int x = 0; x < regions[y].length; x++) {
				if (regions[y][x] == regionId) {
					g2d.drawRect(x, y, 1, 1);
				}
			}
		}

		return canvas;
	}

	public static BufferedImage resizeImage(BufferedImage image, int height,
			int width) {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleX = (double) width / imageWidth;
		double scaleY = (double) height / imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(
				scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(
				scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(image, new BufferedImage(width, height,
				image.getType()));
	}
	
	
	public 	static int[][] computeMetaInformation(BufferedImage screenshot) {
		// image size
		final int nHeight = screenshot.getHeight();
		final int nWidth = screenshot.getWidth();

		// meta debugging information
		int[][] meta = new int[nHeight][nWidth];
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				final int colour = screenshot.getRGB(x, y);
				meta[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
			}
		}

		return meta;
	}

	
	 public static BufferedImage analyseScreenShot(BufferedImage screenshot) {


		// get game state
		GameStateExtractor game = new GameStateExtractor();
		GameStateExtractor.GameState state = game.getGameState(screenshot);
	//	System.out.println(state.toString());

		if (state != GameStateExtractor.GameState.PLAYING) {
			//System.out.println("End game score : " + game.getScoreEndGame(screenshot));
			screenshot = VisionUtils.convert2grey(screenshot);
			return screenshot;
		}

		//System.out.println("In game score : " + game.getScoreInGame(screenshot));
		// process image
		VisionMBR vision = new VisionMBR(screenshot);
		List<Rectangle> pigs = vision.findPigsMBR();
		List<Rectangle> redBirds = vision.findRedBirdsMBRs();
		List<Rectangle> blueBirds = vision.findBlueBirdsMBRs();
		List<Rectangle> yellowBirds = vision.findYellowBirdsMBRs();
		List<Rectangle> woodBlocks = vision.findWoodMBR();
		List<Rectangle> stoneBlocks = vision.findStonesMBR();
		List<Rectangle> iceBlocks = vision.findIceMBR();
		List<Rectangle> whiteBirds = vision.findWhiteBirdsMBRs();
		List<Rectangle> blackBirds = vision.findBlackBirdsMBRs();
		List<Rectangle> TNTs = vision.findTNTsMBR();
		List<Point> trajPoints = vision.findTrajPoints();

		Rectangle sling = vision.findSlingshotMBR();

		
		// draw objects
		screenshot = VisionUtils.convert2grey(screenshot);
		VisionUtils.drawBoundingBoxes(screenshot, pigs, Color.GREEN);
		VisionUtils.drawBoundingBoxes(screenshot, redBirds, Color.RED);
		VisionUtils.drawBoundingBoxes(screenshot, blueBirds, Color.BLUE);
		VisionUtils.drawBoundingBoxes(screenshot, yellowBirds, Color.YELLOW);
		VisionUtils.drawBoundingBoxes(screenshot, woodBlocks, Color.WHITE,
				Color.ORANGE);
		VisionUtils.drawBoundingBoxes(screenshot, stoneBlocks, Color.WHITE,
				Color.GRAY);
		VisionUtils.drawBoundingBoxes(screenshot, iceBlocks, Color.WHITE,
				Color.CYAN);
		VisionUtils.drawBoundingBoxes(screenshot, whiteBirds, Color.WHITE,
				Color.lightGray);
		VisionUtils.drawBoundingBoxes(screenshot, TNTs, Color.WHITE,
				Color.PINK);
		VisionUtils.drawBoundingBoxes(screenshot, blackBirds,
				Color.BLACK);
		if (sling != null) {
			VisionUtils.drawBoundingBox(screenshot, sling, Color.ORANGE,
					Color.BLACK);

			// generate traj points using estimated parameters
			Matrix W = vision.fitParabola(trajPoints);
			int p[][] = new int[2][100];
			int startx = (int) sling.getCenterX();
			for (int i = 0; i < 100; i++) {
				p[0][i] = startx;
				double tem = W.get(0, 0) * Math.pow(p[0][i], 2) + W.get(1, 0)
						* p[0][i] + W.get(2, 0);
				p[1][i] = (int) tem;
				startx += 10;
			}
			if (W.get(0, 0) > 0)
				VisionUtils.drawtrajectory(screenshot, p, Color.RED);

		}

		return screenshot;
	}

}
