/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

package ab.vision;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/* GameStateExtractor ----------------------------------------------------- */

public class GameStateExtractor {

	public enum GameState {
		 UNKNOWN, MAIN_MENU, EPISODE_MENU, LEVEL_SELECTION, LOADING, PLAYING, WON, LOST
	}

    static int repeatCount = 0;
    static int prevScore = 0;
    static boolean saved = false;
    
	// images for determining game state
	private static BufferedImage _mainmenu = null;
	private static BufferedImage _episodemenu = null;
	private static BufferedImage _levelselection = null;
	private static BufferedImage _loading = null;
	private static BufferedImage _loading2 = null;
	private static BufferedImage _gamewon1 = null;
	private static BufferedImage _gamewon2 = null;
	private static BufferedImage _gamelost = null;

	// images for classifying end game score
	private static BufferedImage _endGame0 = null;
	private static BufferedImage _endGame1 = null;
	private static BufferedImage _endGame2 = null;
	private static BufferedImage _endGame3 = null;
	private static BufferedImage _endGame4 = null;
	private static BufferedImage _endGame5 = null;
	private static BufferedImage _endGame6 = null;
	private static BufferedImage _endGame7 = null;
	private static BufferedImage _endGame8 = null;
	private static BufferedImage _endGame9 = null;

	

	private static class RectLeftOf implements java.util.Comparator<Rectangle> {
		public int compare(Rectangle rA, Rectangle rB) {
			return (rA.x - rB.x);
		}
	}

	// create a game state extractor and load subimages
	public GameStateExtractor() {
		try {
			_mainmenu = ImageIO.read(getClass().getResource(
					"resources/mainmenu.png"));
			_episodemenu = ImageIO.read(getClass().getResource(
					"resources/episodemenu.png"));
			_levelselection = ImageIO.read(getClass().getResource(
					"resources/levelselection.png"));
			_loading = ImageIO.read(getClass().getResource(
					"resources/loading.png"));
			_loading2 = ImageIO.read(getClass().getResource(
					"resources/loading2.png"));
			_gamewon1 = ImageIO.read(getClass().getResource(
					"resources/gamewon1.png"));
			_gamewon2 = ImageIO.read(getClass().getResource(
					"resources/gamewon2.png"));
			_gamelost = ImageIO.read(getClass().getResource(
					"resources/gamelost.png"));
			_endGame0 = ImageIO.read(getClass().getResource(
					"resources/0endScreen.png"));
			_endGame1 = ImageIO.read(getClass().getResource(
					"resources/1endScreen.png"));
			_endGame2 = ImageIO.read(getClass().getResource(
					"resources/2endScreen.png"));
			_endGame3 = ImageIO.read(getClass().getResource(
					"resources/3endScreen.png"));
			_endGame4 = ImageIO.read(getClass().getResource(
					"resources/4endScreen.png"));
			_endGame5 = ImageIO.read(getClass().getResource(
					"resources/5endScreen.png"));
			_endGame6 = ImageIO.read(getClass().getResource(
					"resources/6endScreen.png"));
			_endGame7 = ImageIO.read(getClass().getResource(
					"resources/7endScreen.png"));
			_endGame8 = ImageIO.read(getClass().getResource(
					"resources/8endScreen.png"));
			_endGame9 = ImageIO.read(getClass().getResource(
					"resources/9endScreen.png"));

		} catch (IOException e) {
			System.err.println("failed to load resources");
			e.printStackTrace();
		}
	}

	public GameState getGameState(BufferedImage screenshot) {

		// pixel colour deviation threshold for valid detection
		final int avgColourThreshold = 5;

		// check for main menu or episode menu or level selection
		BufferedImage wnd = screenshot.getSubimage(636, 24, 192, 26);
		

		int numBytes = 3 * wnd.getWidth() * wnd.getHeight();
		if (VisionUtils.imageDifference(wnd, _mainmenu) < numBytes
				* avgColourThreshold) {
			return GameState.MAIN_MENU;
		} else if (VisionUtils.imageDifference(wnd, _episodemenu) < numBytes
				* avgColourThreshold) {
			return GameState.EPISODE_MENU;
		} else if (VisionUtils.imageDifference(wnd, _levelselection) < numBytes
				* avgColourThreshold) {
			return GameState.LEVEL_SELECTION;
		} else if ((VisionUtils.imageDifference(wnd, _loading) < numBytes
				* avgColourThreshold)
				|| (VisionUtils.imageDifference(wnd, _loading2) < numBytes
						* avgColourThreshold)) {
			return GameState.LOADING;
		}
		// otherwise check for end game or playing
		wnd = screenshot.getSubimage(467, 350, 61, 60);
		numBytes = 3 * wnd.getWidth() * wnd.getHeight();
		if (VisionUtils.imageDifference(wnd, _gamewon1) < numBytes
				* avgColourThreshold || VisionUtils.imageDifference(wnd, _gamewon2) < numBytes
				* avgColourThreshold) {
			return GameState.WON;
		}

		wnd = screenshot.getSubimage(320, 112, 192, 26);
		numBytes = 3 * wnd.getWidth() * wnd.getHeight();
		if (VisionUtils.imageDifference(wnd, _gamelost) < numBytes
				* avgColourThreshold) {
			return GameState.LOST;
		}

		return GameState.PLAYING;
	}

	public int getScoreInGame(BufferedImage screenshot) {
		// crop score image
		BufferedImage scoreImage = screenshot.getSubimage(632, 21, 200, 32);

		// extract characters
		int mask[][] = new int[scoreImage.getHeight()][scoreImage.getWidth()];
		for (int y = 0; y < scoreImage.getHeight(); y++) {
			for (int x = 0; x < scoreImage.getWidth(); x++) {
				final int colour = scoreImage.getRGB(x, y);
				mask[y][x] = ((colour & 0x00ffffff) == 0x00ffffff) ? 1 : -1;
			}
		}
		scoreImage = VisionUtils.int2image(mask);
		mask = VisionUtils.findConnectedComponents(mask);
		Rectangle[] letters = VisionUtils.findBoundingBoxes(mask);
		Arrays.sort(letters, new RectLeftOf());

		// decode letters
		int score = 0;
		for (int i = 0; i < letters.length; i++) {
			if (letters[i].width < 2)
				continue;

			BufferedImage letterImage = scoreImage.getSubimage(letters[i].x,
					letters[i].y, letters[i].width, letters[i].height);
			final String letterHash = VisionUtils.imageDigest(letterImage);

			int value = 0;
			if (letterHash.equals("62d05c5ce368be507a096aa6b5c68aeb")) {
				value = 1;
			} else if (letterHash.equals("518b4a3878a75aad32e23da4781e4c14")) {
				value = 2;
			} else if (letterHash.equals("be2b93e09c0f94a7c93b1b9cc675b26d")) {
				value = 3;
			} else if (letterHash.equals("3171f145ff67389b22d50ade7a13b5f7")) {
				value = 4;
			} else if (letterHash.equals("96c7dc988a5ad5aa50c3958a0f7869f4")) {
				value = 5;
			} else if (letterHash.equals("049b9aa34adf05ff2cca8cd4057a4d6b")) {
				value = 6;
			} else if (letterHash.equals("897aca1b39d4e2f6bc58b658e8819191")) {
				value = 7;
			} else if (letterHash.equals("e66e8aca895a06c1c9200b1b6b781567")) {
				value = 8;
			} else if (letterHash.equals("41c3010757c2e707146aa5d136e72c7a")) {
				value = 9;
			}

			score = 10 * score + value;
			// System.out.println(i + " : " + letters[i] + " : " + letterHash +
			// " : " + value);
		}            
        
		/*
		 * VisionUtils.drawBoundingBoxes(scoreImage, letters, Color.BLUE); if
		 * (_debug == null) { _debug = new ShowDebuggingImage("score",
		 * scoreImage); } else { _debug.refresh(scoreImage); }
		 */

		return score;
	}

	//transform image into black-white format
	private static BufferedImage extractNumber(BufferedImage image) {

		int mask[][] = new int[image.getHeight()][image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				final int colour = image.getRGB(x, y);
				mask[y][x] = (((colour & 0x00ff0000) >> 16) > 192) ? 1 : -1;
			}
		}
		
		BufferedImage numberImage = VisionUtils.int2image(mask);
		mask = VisionUtils.findConnectedComponents(mask);
        Rectangle number[] = VisionUtils.findBoundingBoxes(mask);
        
		return numberImage.getSubimage(number[0].x, number[0].y, number[0].width, number[0].height);
	}

	public int getScoreEndGame(BufferedImage screenshot) {
		// crop score image
		BufferedImage scoreImage = screenshot.getSubimage(370, 265, 100, 32);
	
		

        // transform template images into black-white format
		BufferedImage[] endGameNumberTemplates = { extractNumber(_endGame0),
				extractNumber(_endGame1), extractNumber(_endGame2),
				extractNumber(_endGame3), extractNumber(_endGame4),
				extractNumber(_endGame5), extractNumber(_endGame6),
				extractNumber(_endGame7), extractNumber(_endGame8),
				extractNumber(_endGame9) };
		
        
		// extract characters
		int mask[][] = new int[scoreImage.getHeight()][scoreImage.getWidth()];
		for (int y = 0; y < scoreImage.getHeight(); y++) {
			for (int x = 0; x < scoreImage.getWidth(); x++) {
				final int colour = scoreImage.getRGB(x, y);
				mask[y][x] = (((colour & 0x00ff0000) >> 16) > 192) ? 1 : -1;
			}
		}
		scoreImage = VisionUtils.int2image(mask);
		mask = VisionUtils.findConnectedComponents(mask);
		Rectangle[] letters = VisionUtils.findBoundingBoxes(mask);
		Arrays.sort(letters, new RectLeftOf());
        
		// decode letters
		int score = 0;
		for (int i = 0; i < letters.length; i++) {
			if (letters[i].width < 2)
				continue;

			BufferedImage letterImage = scoreImage.getSubimage(letters[i].x,
					letters[i].y, letters[i].width, letters[i].height);

			int value = 0;
			
			//init min different between target number and template
			int minDiff = Integer.MAX_VALUE;
			
			//loop to find a template with minimum difference
			for (int j = 0; j < 10; j++) {
		        int diff = getPixelDifference(letterImage, endGameNumberTemplates[j]);
				if(diff < minDiff){
					minDiff = diff;
					value = j;
				}
			}
			score = 10 * score + value;
		}

        /*
        if (score != prevScore)
        {
            saved = false;
            repeatCount = 0;
            prevScore = score;
        }
        else if (score != 0 && !saved)
        {
            repeatCount++;
            if (repeatCount > 0)
            {
                saved = true;
                try {
                    File outputfile = new File("scoreImage/" + score + ".png");
                    ImageIO.write(saveImage, "png", outputfile);
                } catch (IOException e) {
                
                }
            }
        }*/
        
		/*
		 * VisionUtils.drawBoundingBoxes(scoreImage, letters, Color.BLUE); if
		 * (_debug == null) { _debug = new ShowDebuggingImage("score",
		 * scoreImage); } else { _debug.refresh(scoreImage); }
		 */

		return score;
	}
	
	public int getPixelDifference(BufferedImage letter, BufferedImage template)
	{
	    // resize the template image
	    int height = letter.getHeight();
	    int width = (int) ((double) height / template.getHeight() * template.getWidth());
	    if (height == 0 || width == 0)
	        return 0;
	    template = VisionUtils.resizeImage(template, height, width);
	    	    
	    // fill blank pixels
	    if (width < letter.getWidth())
	    {
	        int minX = (letter.getWidth() - width) / 2;
	        width = letter.getWidth();
	        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g = temp.createGraphics();
	        g.drawImage(template, null, minX, 0);
	        template = temp;
	    }
	    else
	    {
	        int minX = (width - letter.getWidth()) / 2;
	        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g = temp.createGraphics();
	        g.drawImage(letter, null, minX, 0);
	        letter = temp;
	    }
	    
	    // return image difference
	    return VisionUtils.imageDifference(letter, template);
	}
}
