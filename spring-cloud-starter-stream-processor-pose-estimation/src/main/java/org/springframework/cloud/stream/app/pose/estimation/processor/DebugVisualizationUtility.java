/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.pose.estimation.processor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.app.pose.estimation.model.Limb;
import org.springframework.cloud.stream.app.pose.estimation.model.Model;
import org.springframework.cloud.stream.app.pose.estimation.model.Part;
import org.springframework.cloud.stream.app.tensorflow.util.GraphicsUtils;

/**
 * Utility used to debug and visualize some the intermediate processing stages.
 * It can be used to visualize the Heatmaps, the PAF fields produced by the Tensorflow model as well as
 * the part and limb candidates computed by the post-processors.
 *
 * @author Christian Tzolov
 */
public class DebugVisualizationUtility {

	private static final Log logger = LogFactory.getLog(DebugVisualizationUtility.class);

	public static final String IMAGE_FORMAT = "jpg";
	private static final int STROKE_WIDTH = 1;
	private static final int OVAL_WIDTH = 6;
	private static final int OVAL_HEIGHT = 6;

	public static void visualizeAllPafHeatMapChannels(byte[] inputImage, float[][][] tensorData, String debugImageFilePath) {
		byte[] partHeatmap = inputImage;
		int heatmapColor = 0;
		for (Model.PartType partType : Model.PartType.values()) {
			partHeatmap = DebugVisualizationUtility.drawPartHeatmap(partHeatmap, partType, tensorData, GraphicsUtils.CLASS_COLOR2[heatmapColor++]);
		}
		DebugVisualizationUtility.writeImageToFile(partHeatmap, debugImageFilePath);

	}

	private static byte[] drawPartHeatmap(byte[] imageBytes, Model.PartType partType,
			float[][][] outputTensor, Color color) {

		return new ImageGraphicsTemplate() {
			@Override
			public void drawWithingImage(Graphics2D g) {
				g.setColor(color);
				for (int x = 0; x < outputTensor.length; x++) {
					for (int y = 0; y < outputTensor[0].length; y++) {
						float partScore = outputTensor[x][y][partType.getId()];
						g.fillOval(y * 8, x * 8, (int) (15 * partScore), (int) (15 * partScore));
					}
				}
			}
		}.draw(imageBytes);
	}

	public static void visualizeAllPafChannels(byte[] inputImage, float[][][] tensorData, String debugImageFilePath) {
		byte[] pafFieldImage = inputImage;
		int pafColor = 0;
		for (Model.LimbType pafLimbType : Model.LimbType.values()) {
			pafFieldImage = DebugVisualizationUtility.drawPafField(pafFieldImage, pafLimbType, tensorData,
					GraphicsUtils.CLASS_COLOR2[pafColor++]);
		}
		DebugVisualizationUtility.writeImageToFile(pafFieldImage, debugImageFilePath);

	}

	private static byte[] drawPafField(byte[] imageBytes, Model.LimbType limbType, float[][][] outputTensor, Color color) {

		return new ImageGraphicsTemplate() {
			@Override
			public void drawWithingImage(Graphics2D g) {
				g.setColor(color);
				for (int x = 0; x < outputTensor.length; x++) {
					for (int y = 0; y < outputTensor[0].length; y++) {
						float pafX = outputTensor[x][y][limbType.getPafIndexX()];
						float pafY = outputTensor[x][y][limbType.getPafIndexY()];

						if (pafX > 0.1f || pafY > 0.1f) {
							g.draw(new Line2D.Double(y * 8, x * 8, (y + 2 * pafY) * 8, (x + 3 * pafX) * 8));
						}

					}
				}
			}
		}.draw(imageBytes);
	}

	public static void visualizePartCandidates(byte[] inputImage, Map<Model.PartType, List<Part>> partCandidates,
			String debugImageFilePath) {

		byte[] partCandidatesImage = inputImage;
		for (List<Part> parts : partCandidates.values()) {
			for (Part part : parts) {
				partCandidatesImage = drawPartCandidate(partCandidatesImage, part, GraphicsUtils.CLASS_COLOR2[part.getPartType().getId()]);
			}
		}
		DebugVisualizationUtility.writeImageToFile(partCandidatesImage, debugImageFilePath);

	}

	private static byte[] drawPartCandidate(byte[] imageBytes, Part partCandidate, Color color) {

		return new ImageGraphicsTemplate() {
			@Override
			public void drawWithingImage(Graphics2D g) {
				g.setColor(color);
				int partScore = (int) (20 * partCandidate.getConfidence());
				g.fillOval(partCandidate.getNormalizedX() - partScore / 2, partCandidate.getNormalizedY() - partScore / 2, partScore, partScore);

			}
		}.draw(imageBytes);
	}

	public static void visualizeLimbCandidates(byte[] inputImage,
			Map<Model.LimbType, List<Limb>> limbCandidates, String debugImageFilePath) {

		byte[] limbCandidatesImage = inputImage;

		for (List<Limb> limbs : limbCandidates.values()) {
			for (Limb limb : limbs) {
				limbCandidatesImage = drawLimbCandidate(limbCandidatesImage, limb);
			}
		}
		DebugVisualizationUtility.writeImageToFile(limbCandidatesImage, debugImageFilePath);
	}

	private static byte[] drawLimbCandidate(byte[] imageBytes, Limb limb) {

		return new ImageGraphicsTemplate() {
			@Override
			public void drawWithingImage(Graphics2D g) {
				Part from = limb.getFromPart();
				Part to = limb.getToPart();

				int x1 = from.getNormalizedX();
				int x2 = to.getNormalizedX();

				int y1 = from.getNormalizedY();
				int y2 = to.getNormalizedY();

				int xl = (x2 - x1) / 2;
				int yl = (y2 - y1) / 2;

				g.setColor(GraphicsUtils.yellow);
				g.drawString(String.format("%.2f", limb.getPafScore()), x1 + xl + 5, y1 + yl + 5);

				g.setColor(new Color(167, 252, 0));
				g.draw(new Line2D.Double(from.getNormalizedX(), from.getNormalizedY(),
						to.getNormalizedX(), to.getNormalizedY()));

				g.setColor(GraphicsUtils.CLASS_COLOR2[from.getPartType().getId()]);
				g.fillOval(from.getNormalizedX() - OVAL_WIDTH / 2, from.getNormalizedY() - OVAL_WIDTH / 2, OVAL_WIDTH, OVAL_HEIGHT);
				g.setColor(GraphicsUtils.CLASS_COLOR2[to.getPartType().getId()]);
				g.fillOval(to.getNormalizedX() - OVAL_WIDTH / 2, to.getNormalizedY() - OVAL_WIDTH / 2, OVAL_WIDTH, OVAL_HEIGHT);
			}
		}.draw(imageBytes);
	}

	public static void writeImageToFile(byte[] image, String filePath) {
		try {
			IOUtils.write(image, new FileOutputStream(filePath));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public abstract static class ImageGraphicsTemplate {

		public byte[] draw(byte[] imageBytes) {
			try {
				BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

				Graphics2D g = originalImage.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Stroke stroke = g.getStroke();
				g.setStroke(new BasicStroke(STROKE_WIDTH));
				g.setFont(new Font("arial", Font.PLAIN, 11));

				drawWithingImage(g);

				g.setStroke(stroke);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, IMAGE_FORMAT, baos);
				baos.flush();
				imageBytes = baos.toByteArray();
				baos.close();
				g.dispose();

				return imageBytes;
			}
			catch (IOException ex) {
				throw new RuntimeException((ex));
			}
		}

		public abstract void drawWithingImage(Graphics2D g);
	}
}
