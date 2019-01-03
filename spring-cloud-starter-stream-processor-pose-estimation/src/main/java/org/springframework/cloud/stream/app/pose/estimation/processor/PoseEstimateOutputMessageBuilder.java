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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.app.pose.estimation.model.Body;
import org.springframework.cloud.stream.app.pose.estimation.model.Limb;
import org.springframework.cloud.stream.app.pose.estimation.model.Model;
import org.springframework.cloud.stream.app.pose.estimation.model.Part;
import org.springframework.cloud.stream.app.tensorflow.processor.DefaultOutputMessageBuilder;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowCommonProcessorProperties;
import org.springframework.cloud.stream.app.tensorflow.util.GraphicsUtils;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

/**
 * Extends the {@link DefaultOutputMessageBuilder} with ability to to augment the input image with the
 * recognized poses.
 *
 * @author Christian Tzolov
 */
public class PoseEstimateOutputMessageBuilder extends DefaultOutputMessageBuilder {

	private static final Log logger = LogFactory.getLog(PoseEstimateOutputMessageBuilder.class);

	public static final String IMAGE_FORMAT = "jpg";

	public static final Color DEFAULT_COLOR = new Color(167, 252, 0);

	private PoseEstimationProcessorProperties poseProperties;

	public PoseEstimateOutputMessageBuilder(PoseEstimationProcessorProperties poseProperties,
			TensorflowCommonProcessorProperties properties) {
		super(properties);
		this.poseProperties = poseProperties;
	}

	@Override
	public MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore) {

		Message<?> annotatedInput = inputMessage;

		List<Body> bodies = (List<Body>) computedScore;

		if (this.poseProperties.isDrawPoses()) {
			try {
				byte[] annotatedImage = drawPoses((byte[]) inputMessage.getPayload(), bodies);
				annotatedInput = MessageBuilder.withPayload(annotatedImage)
						.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
						.build();
			}
			catch (IOException e) {
				logger.error("Failed to draw the poses", e);
			}
		}

		return super.createOutputMessageBuilder(annotatedInput, toJson(bodies));
	}

	private byte[] drawPoses(byte[] imageBytes, List<Body> bodies) throws IOException {

		if (bodies != null) {

			BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

			Graphics2D g = originalImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Stroke stroke = g.getStroke();
			g.setStroke(new BasicStroke(this.poseProperties.getDrawLineWidth()));

			for (Body body : bodies) {
				for (Limb limb : body.getLimbs()) {

					Color limbColor = findLimbColor(body, limb);

					Part from = limb.getFromPart();
					Part to = limb.getToPart();

					if (limb.getLimbType() != Model.LimbType.limb17 && limb.getLimbType() != Model.LimbType.limb18) {
						g.setColor(limbColor);
						g.draw(new Line2D.Double(from.getNormalizedX(), from.getNormalizedY(),
								to.getNormalizedX(), to.getNormalizedY()));
					}

					g.setStroke(new BasicStroke(1));
					drawPartOval(from, this.poseProperties.getDrawPartRadius(), g);
					drawPartOval(to, this.poseProperties.getDrawPartRadius(), g);
					g.setStroke(new BasicStroke(this.poseProperties.getDrawLineWidth()));
				}
			}

			g.setStroke(stroke);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(originalImage, IMAGE_FORMAT, baos);
			baos.flush();
			imageBytes = baos.toByteArray();
			baos.close();
			g.dispose();
		}

		return imageBytes;
	}

	private Color findLimbColor(Body body, Limb limb) {
		Color limbColor = DEFAULT_COLOR; ;
		switch (this.poseProperties.getBodyDrawingColorSchema()) {
		case bodyInstance:
			limbColor = GraphicsUtils.getClassColor(body.getBodyId() * 3);
			break;
		case limbType:
			limbColor = GraphicsUtils.LIMBS_COLORS[limb.getLimbType().getId()];
			break;
		case monochrome:
			limbColor = DEFAULT_COLOR;
			break;
		}

		return limbColor;
	}

	private void drawPartOval(Part part, int radius, Graphics2D g) {
		int partX = part.getNormalizedX();
		int partY = part.getNormalizedY();

		g.setColor(GraphicsUtils.LIMBS_COLORS[part.getPartType().getId()]);
		g.fillOval(partX - radius, partY - radius, 2 * radius, 2 * radius);

		if (this.poseProperties.isDrawPartLabels()) {
			String label = part.getPartType().getId() + ":" + part.getPartType().name();
			FontMetrics fm = g.getFontMetrics();
			int labelX = partX + 5;
			int labelY = partY - 5;
			AffineTransform t = g.getTransform();
			g.setTransform(AffineTransform.getRotateInstance(Math.toRadians(-35), labelX, labelY));

			g.drawString(label, labelX, labelY);
			g.setTransform(t);
		}

	}

	private String toJson(List<Body> bodies) {
		try {
			return new ObjectMapper().writeValueAsString(bodies);
		}
		catch (JsonProcessingException e) {
			logger.error("Failed to encode the bodies into JSON message", e);
		}
		return "ERROR";
	}
}
