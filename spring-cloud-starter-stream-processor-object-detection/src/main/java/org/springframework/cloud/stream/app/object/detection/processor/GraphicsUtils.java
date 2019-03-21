/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.object.detection.processor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Utility class used to provide some handy image manipulation functions. Among others it can provide contrast colors
 * for image annotation labels and bounding boxes as well as functionality to draw later.
 *
 * @author Christian Tzolov
 */
public class GraphicsUtils {

	/**
	 * Default DEFAULT_FONT used in image label annotation.
	 */
	private static final Font DEFAULT_FONT = new Font("arial", Font.PLAIN, 12);

	/**
	 * Bounding box default line thickness
	 */
	private static final float LINE_THICKNESS = 2;

	/**
	 * Color used when no multi-color is used
	 */
	private static final Color AGNOSTIC_COLOR = new Color(167, 252, 0);

	/**
	 * in labels text offset.
	 */
	public static final int TITLE_OFFSET = 3;

	/**
	 * Predefined contrasting colors used when drawing multiple objects in the same image.
	 */
	public static final Color aliceblue = new Color(240, 248, 255);
	public static final Color antiquewhite = new Color(250, 235, 215);
	public static final Color aqua = new Color(0, 255, 255);
	public static final Color aquamarine = new Color(127, 255, 212);
	public static final Color azure = new Color(240, 255, 255);
	public static final Color beige = new Color(245, 245, 220);
	public static final Color bisque = new Color(255, 228, 196);
	public static final Color black = new Color(0, 0, 0);
	public static final Color blanchedalmond = new Color(255, 255, 205);
	public static final Color blue = new Color(0, 0, 255);
	public static final Color blueviolet = new Color(138, 43, 226);
	public static final Color brown = new Color(165, 42, 42);
	public static final Color burlywood = new Color(222, 184, 135);
	public static final Color cadetblue = new Color(95, 158, 160);
	public static final Color chartreuse = new Color(127, 255, 0);
	public static final Color chocolate = new Color(210, 105, 30);
	public static final Color coral = new Color(255, 127, 80);
	public static final Color cornflowerblue = new Color(100, 149, 237);
	public static final Color cornsilk = new Color(255, 248, 220);
	public static final Color crimson = new Color(220, 20, 60);
	public static final Color cyan = new Color(0, 255, 255);
	public static final Color darkblue = new Color(0, 0, 139);
	public static final Color darkcyan = new Color(0, 139, 139);
	public static final Color darkgoldenrod = new Color(184, 134, 11);
	public static final Color darkgray = new Color(169, 169, 169);
	public static final Color darkgreen = new Color(0, 100, 0);
	public static final Color darkkhaki = new Color(189, 183, 107);
	public static final Color darkmagenta = new Color(139, 0, 139);
	public static final Color darkolivegreen = new Color(85, 107, 47);
	public static final Color darkorange = new Color(255, 140, 0);
	public static final Color darkorchid = new Color(153, 50, 204);
	public static final Color darkred = new Color(139, 0, 0);
	public static final Color darksalmon = new Color(233, 150, 122);
	public static final Color darkseagreen = new Color(143, 188, 143);
	public static final Color darkslateblue = new Color(72, 61, 139);
	public static final Color darkslategray = new Color(47, 79, 79);
	public static final Color darkturquoise = new Color(0, 206, 209);
	public static final Color darkviolet = new Color(148, 0, 211);
	public static final Color deeppink = new Color(255, 20, 147);
	public static final Color deepskyblue = new Color(0, 191, 255);
	public static final Color dimgray = new Color(105, 105, 105);
	public static final Color dodgerblue = new Color(30, 144, 255);
	public static final Color firebrick = new Color(178, 34, 34);
	public static final Color floralwhite = new Color(255, 250, 240);
	public static final Color forestgreen = new Color(34, 139, 34);
	public static final Color fuchsia = new Color(255, 0, 255);
	public static final Color gainsboro = new Color(220, 220, 220);
	public static final Color ghostwhite = new Color(248, 248, 255);
	public static final Color gold = new Color(255, 215, 0);
	public static final Color goldenrod = new Color(218, 165, 32);
	public static final Color gray = new Color(128, 128, 128);
	public static final Color green = new Color(0, 128, 0);
	public static final Color greenyellow = new Color(173, 255, 47);
	public static final Color honeydew = new Color(240, 255, 240);
	public static final Color hotpink = new Color(255, 105, 180);
	public static final Color indianred = new Color(205, 92, 92);
	public static final Color indigo = new Color(75, 0, 130);
	public static final Color ivory = new Color(255, 240, 240);
	public static final Color khaki = new Color(240, 230, 140);
	public static final Color lavender = new Color(230, 230, 250);
	public static final Color lavenderblush = new Color(255, 240, 245);
	public static final Color lawngreen = new Color(124, 252, 0);
	public static final Color lemonchiffon = new Color(255, 250, 205);
	public static final Color lightblue = new Color(173, 216, 230);
	public static final Color lightcoral = new Color(240, 128, 128);
	public static final Color lightcyan = new Color(224, 255, 255);
	public static final Color lightgoldenrodyellow = new Color(250, 250, 210);
	public static final Color lightgreen = new Color(144, 238, 144);
	public static final Color lightgrey = new Color(211, 211, 211);
	public static final Color lightpink = new Color(255, 182, 193);
	public static final Color lightsalmon = new Color(255, 160, 122);
	public static final Color lightseagreen = new Color(32, 178, 170);
	public static final Color lightskyblue = new Color(135, 206, 250);
	public static final Color lightslategray = new Color(119, 136, 153);
	public static final Color lightsteelblue = new Color(176, 196, 222);
	public static final Color lightyellow = new Color(255, 255, 224);
	public static final Color lime = new Color(0, 255, 0);
	public static final Color limegreen = new Color(50, 205, 50);
	public static final Color linen = new Color(250, 240, 230);
	public static final Color magenta = new Color(255, 0, 255);
	public static final Color maroon = new Color(128, 0, 0);
	public static final Color mediumaquamarine = new Color(102, 205, 170);
	public static final Color mediumblue = new Color(0, 0, 205);
	public static final Color mediumorchid = new Color(186, 85, 211);
	public static final Color mediumpurple = new Color(147, 112, 219);
	public static final Color mediumseagreen = new Color(60, 179, 113);
	public static final Color mediumslateblue = new Color(123, 104, 238);
	public static final Color mediumspringgreen = new Color(0, 250, 154);
	public static final Color mediumturquoise = new Color(72, 209, 204);
	public static final Color mediumvioletred = new Color(199, 21, 133);
	public static final Color midnightblue = new Color(25, 25, 112);
	public static final Color mintcream = new Color(245, 255, 250);
	public static final Color mistyrose = new Color(255, 228, 225);
	public static final Color mocassin = new Color(255, 228, 181);
	public static final Color navajowhite = new Color(255, 222, 173);
	public static final Color navy = new Color(0, 0, 128);
	public static final Color oldlace = new Color(253, 245, 230);
	public static final Color olive = new Color(128, 128, 0);
	public static final Color olivedrab = new Color(107, 142, 35);
	public static final Color orange = new Color(255, 165, 0);
	public static final Color orangered = new Color(255, 69, 0);
	public static final Color orchid = new Color(218, 112, 214);
	public static final Color palegoldenrod = new Color(238, 232, 170);
	public static final Color palegreen = new Color(152, 251, 152);
	public static final Color paleturquoise = new Color(175, 238, 238);
	public static final Color palevioletred = new Color(219, 112, 147);
	public static final Color papayawhip = new Color(255, 239, 213);
	public static final Color peachpuff = new Color(255, 218, 185);
	public static final Color peru = new Color(205, 133, 63);
	public static final Color pink = new Color(255, 192, 203);
	public static final Color plum = new Color(221, 160, 221);
	public static final Color powderblue = new Color(176, 224, 230);
	public static final Color purple = new Color(128, 0, 128);
	public static final Color red = new Color(255, 0, 0);
	public static final Color rosybrown = new Color(188, 143, 143);
	public static final Color royalblue = new Color(65, 105, 225);
	public static final Color saddlebrown = new Color(139, 69, 19);
	public static final Color salmon = new Color(250, 128, 114);
	public static final Color sandybrown = new Color(244, 164, 96);
	public static final Color seagreen = new Color(46, 139, 87);
	public static final Color seashell = new Color(255, 245, 238);
	public static final Color sienna = new Color(160, 82, 45);
	public static final Color silver = new Color(192, 192, 192);
	public static final Color skyblue = new Color(135, 206, 235);
	public static final Color slateblue = new Color(106, 90, 205);
	public static final Color slategray = new Color(112, 128, 144);
	public static final Color snow = new Color(255, 250, 250);
	public static final Color springgreen = new Color(0, 255, 127);
	public static final Color steelblue = new Color(70, 138, 180);
	public static final Color tan = new Color(210, 180, 140);
	public static final Color teal = new Color(0, 128, 128);
	public static final Color thistle = new Color(216, 191, 216);
	public static final Color tomato = new Color(253, 99, 71);
	public static final Color turquoise = new Color(64, 224, 208);
	public static final Color violet = new Color(238, 130, 238);
	public static final Color wheat = new Color(245, 222, 179);
	public static final Color white = new Color(255, 255, 255);
	public static final Color whitesmoke = new Color(245, 245, 245);
	public static final Color yellow = new Color(255, 255, 0);
	public static final Color yellowgreen = new Color(154, 205, 50);


	private static final Color[] CLASS_COLOR = new Color[] {
			aliceblue, chartreuse, aqua, aquamarine, azure, beige, bisque,
			blanchedalmond, blueviolet, burlywood, cadetblue, antiquewhite,
			chocolate, coral, cornflowerblue, cornsilk, crimson, cyan,
			darkcyan, darkgoldenrod, darkgray, darkkhaki, darkorange,
			darkorchid, darksalmon, darkseagreen, darkturquoise, darkviolet,
			deeppink, deepskyblue, dodgerblue, firebrick, floralwhite,
			forestgreen, fuchsia, gainsboro, ghostwhite, gold, goldenrod,
			salmon, tan, honeydew, hotpink, indianred, ivory, khaki,
			lavender, lavenderblush, lawngreen, lemonchiffon, lightblue,
			lightcoral, lightcyan, lightgoldenrodyellow, lightgreen, lightgrey,
			lightgreen, lightpink, lightsalmon, lightseagreen, lightskyblue,
			lightslategray, lightslategray, lightsteelblue, lightyellow, lime,
			limegreen, linen, magenta, mediumaquamarine, mediumorchid,
			mediumpurple, mediumseagreen, mediumslateblue, mediumspringgreen,
			mediumturquoise, mediumvioletred, mintcream, mistyrose, mocassin,
			navajowhite, oldlace, olive, olivedrab, orange, orangered,
			orchid, palegoldenrod, palegreen, paleturquoise, palevioletred,
			papayawhip, peachpuff, peru, pink, plum, powderblue, purple,
			red, rosybrown, royalblue, saddlebrown, green, sandybrown,
			seagreen, seashell, sienna, silver, skyblue, slateblue,
			slategray, slategray, snow, springgreen, steelblue, greenyellow,
			teal, thistle, tomato, turquoise, violet, wheat, white,
			whitesmoke, yellow, yellowgreen
	};

	/**
	 * Return different color for each Id. It rotates when the ID exceeds the number of predefined colors.
	 * @param id the unique id to pick color for.
	 * @return a distinct color computed from the input #id
	 */
	public static Color getClassColor(int id) {
		return CLASS_COLOR[id % CLASS_COLOR.length];
	}

	/**
	 * Augments the input image with a labeled rectangle (e.g. bounding box) with coordinates: (x1, y1, x2, y2).
	 *
	 * @param image Input image to be augmented with labeled rectangle.
	 * @param cid Unique id used to select the color of the rectangle. Used only if the colorAgnostic is set to false.
	 * @param title rectangle title
	 * @param x1 top left corner for the bounding box
	 * @param y1 top left corner for the bounding box
	 * @param x2 bottom right corner for the bounding box
	 * @param y2 bottom right corner for the bounding box
	 * @param colorAgnostic If set to false the cid is used to select the bounding box color. Uses the
	 *                      AGNOSTIC_COLOR otherwise.
	 */
	public static void drawBoundingBox(BufferedImage image, int cid, String title, int x1, int y1, int x2, int y2,
			boolean colorAgnostic) {

		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Color labelColor = colorAgnostic ? AGNOSTIC_COLOR : GraphicsUtils.getClassColor(cid);
		g.setColor(labelColor);

		g.setFont(DEFAULT_FONT);
		FontMetrics fontMetrics = g.getFontMetrics();

		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(LINE_THICKNESS));
		g.drawRect(x1, y1, (x2 - x1), (y2 - y1));
		g.setStroke(oldStroke);

		Rectangle2D rect = fontMetrics.getStringBounds(title, g);

		g.setColor(labelColor);
		g.fillRect(x1, y1 - fontMetrics.getAscent(),
				(int) rect.getWidth() + 2 * TITLE_OFFSET, (int) rect.getHeight());

		g.setColor(getTextColor(labelColor));
		g.drawString(title, x1 + TITLE_OFFSET, y1);
	}

	/**
	 * Depends on the darkness of the background, pick a dark or light DEFAULT_FONT color
	 * @param backGroundColor background color within which the text is drawn
	 * @return a text color, that contrast to the given background color.
	 */
	private static Color getTextColor(Color backGroundColor) {
		double y = (299 * backGroundColor.getRed() + 587 * backGroundColor.getGreen() +
				114 * backGroundColor.getBlue()) / 1000;
		return y >= 128 ? Color.black : Color.white;
	}

	public static BufferedImage createMaskImage(float[][] maskPixels,
			int scaledWidth, int scaledHeight, Color maskColor) {

		int maskWidth = maskPixels.length;
		int maskHeight = maskPixels[0].length;
		int[] maskArray = new int[maskWidth * maskHeight];
		int k = 0;
		for (int i = 0; i < maskHeight; i++) {
			for (int j = 0; j < maskWidth; j++) {
				maskArray[k++] = grayScaleToARGB(maskPixels[i][j], maskColor);
			}
		}

		// Turn the pixel array into image;
		BufferedImage maskImage = new BufferedImage(maskWidth, maskHeight, BufferedImage.TYPE_INT_ARGB);
		maskImage.setRGB(0, 0, maskWidth, maskHeight, maskArray, 0, maskWidth);

		// Stretch the image to fit the target box width and height!
		return toBufferedImage(maskImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT));
	}

	/**
	 * Converts an gray scale (e.g. value between 0 to 1) into ARGB.
	 *
	 * @param grayScale - value between 0 and 1
	 * @param maskColor - desired mask color
	 * @return Returns a ARGB color based on the grayscale and the mask colors
	 */
	private static int grayScaleToARGB(float grayScale, Color maskColor) {
		if (maskColor != null) {
			float r = col(maskColor.getRed(), grayScale);
			float g = col(maskColor.getGreen(), grayScale);
			float b = col(maskColor.getBlue(), grayScale);
			float t = grayScale * 0.7f;
			return new Color(r, g, b, t).getRGB();
		}

		return new Color(grayScale, grayScale, grayScale, grayScale).getRGB();
	}

	private static float col(int channelColor, float grayScale) {
		//return ((float) channelColor / 255) * grayScale;
		return ((float) channelColor / 255);
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static BufferedImage overlayImages(BufferedImage bgImage, BufferedImage fgImage, int fgX, int fgY) {
		// Foreground image width and height cannot be greater than background image width and height.
		if (fgImage.getHeight() > bgImage.getHeight()
				|| fgImage.getWidth() > fgImage.getWidth()) {
			throw new IllegalArgumentException(
					"Foreground Image Is Bigger In One or Both Dimensions"
							+ "nCannot proceed with overlay."
							+ "nn Please use smaller Image for foreground");
		}

		// Create a Graphics  from the background image
		Graphics2D g = bgImage.createGraphics();

		//Set Antialias Rendering
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//Draw background image at location (0,0)
		g.drawImage(bgImage, 0, 0, null);

		// Draw foreground image at location (fgX,fgy)
		g.drawImage(fgImage, fgX, fgY, null);

		g.dispose();
		return bgImage;
	}
}
