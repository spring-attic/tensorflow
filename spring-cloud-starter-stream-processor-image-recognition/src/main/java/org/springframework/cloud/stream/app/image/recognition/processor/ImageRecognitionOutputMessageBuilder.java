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

package org.springframework.cloud.stream.app.image.recognition.processor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.app.tensorflow.processor.DefaultOutputMessageBuilder;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowCommonProcessorProperties;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * Extends the {@link DefaultOutputMessageBuilder} with ability to to augment the input image with the
 * recognized labels.
 *
 * @author Christian Tzolov
 */
public class ImageRecognitionOutputMessageBuilder extends DefaultOutputMessageBuilder {

	private static final Log logger = LogFactory.getLog(ImageRecognitionOutputMessageBuilder.class);

	public static final String IMAGE_FORMAT = "jpg";

	private final Color textColor = Color.BLACK;
	private final Color bgColor = new Color(167, 252, 0);
	private final boolean drawLabels;

	public ImageRecognitionOutputMessageBuilder(boolean drawLabels,
			TensorflowCommonProcessorProperties properties) {
		super(properties);
		this.drawLabels = drawLabels;
	}

	@Override
	public MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore) {
		Message<?> annotatedInput = inputMessage;

		if (this.drawLabels) {
			byte[] annotatedImage = drawLabels((byte[]) inputMessage.getPayload(), computedScore);
			annotatedInput = MessageBuilder.withPayload(annotatedImage).build();
		}

		return super.createOutputMessageBuilder(annotatedInput, computedScore);
	}

	/**
	 * Augment the input image by adding the recognized classes.
	 *
	 * @param imageBytes input image as byte array
	 * @param result computed recognition labels
	 * @return the image augmented with recognized labels.
	 */
	private byte[] drawLabels(byte[] imageBytes, Object result) {
		try {
			if (result != null) {
				BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

				Graphics2D g = originalImage.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				FontMetrics fm = g.getFontMetrics();

				Map<String, Object> resultMap = new ObjectMapper().readValue(result.toString(), Map.class);
				List<Map<String, Double>> labelsList = (List<Map<String, Double>>) resultMap.get("labels");

				int x = 1;
				int y = 1;
				for (Map<String, Double> m : labelsList) {

					Map.Entry<String, Double> l = m.entrySet().iterator().next();

					String labelName = l.getKey();
					int probability = (int) (100 * l.getValue());
					String title = labelName + ": " + probability + "%";

					Rectangle2D rect = fm.getStringBounds(title, g);

					g.setColor(bgColor);
					g.fillRect(x, y, (int) rect.getWidth() + 6, (int) rect.getHeight());

					g.setColor(textColor);
					g.drawString(title, x + 3, (int) (y + rect.getHeight() - 3));
					y = (int) (y + rect.getHeight() + 1);
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, IMAGE_FORMAT, baos);
				baos.flush();
				imageBytes = baos.toByteArray();
				baos.close();
			}
		}
		catch (IOException e) {
			logger.error("Failed to draw labels in the input image", e);
		}

		return imageBytes;
	}

}
