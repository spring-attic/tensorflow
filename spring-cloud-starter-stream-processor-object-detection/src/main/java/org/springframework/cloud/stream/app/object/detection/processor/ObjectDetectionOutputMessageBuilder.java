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

package org.springframework.cloud.stream.app.object.detection.processor;

import java.awt.Color;
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

import org.springframework.cloud.stream.app.object.detection.mocel.ObjectDetection;
import org.springframework.cloud.stream.app.tensorflow.processor.DefaultOutputMessageBuilder;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowCommonProcessorProperties;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

/**
 * Extends the {@link DefaultOutputMessageBuilder} with ability to augment the input image with detected object
 * bounding boxes.
 * If the {@link #drawBoundingBox} is set the input image is augmented with bounding boxers around each detected object.
 *
 * @author Christian Tzolov
 */
public class ObjectDetectionOutputMessageBuilder extends DefaultOutputMessageBuilder {

	private static final Log logger = LogFactory.getLog(ObjectDetectionOutputMessageBuilder.class);

	public static final String IMAGE_FORMAT = "jpg";

	private final boolean drawBoundingBox;

	private boolean drawMask;

	private final boolean agnosticColors;

	public ObjectDetectionOutputMessageBuilder(boolean drawBoundingBox, boolean drawMask, boolean agnosticColors,
			TensorflowCommonProcessorProperties properties) {
		super(properties);
		this.drawBoundingBox = drawBoundingBox;
		this.drawMask = drawMask;
		this.agnosticColors = agnosticColors;
	}

	@Override
	public MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore) {
		Message<?> annotatedInput = inputMessage;

		List<ObjectDetection> objectDetections = (List<ObjectDetection>) computedScore;
		if (this.drawBoundingBox) {
			byte[] annotatedImage = drawBoundingBox((byte[]) inputMessage.getPayload(), objectDetections);
			annotatedInput = MessageBuilder.withPayload(annotatedImage)
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
					.build();
		}

		return super.createOutputMessageBuilder(annotatedInput, toJson(objectDetections));
	}

	private byte[] drawBoundingBox(byte[] imageBytes, List<ObjectDetection> objectDetections) {
		if (objectDetections != null) {
			try {
				BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

				for (ObjectDetection od : objectDetections) {
					int y1 = (int) (od.getY1() * (float) originalImage.getHeight());
					int x1 = (int) (od.getX1() * (float) originalImage.getWidth());
					int y2 = (int) (od.getY2() * (float) originalImage.getHeight());
					int x2 = (int) (od.getX2() * (float) originalImage.getWidth());

					int cid = od.getCid();

					String labelName = od.getName();
					int probability = (int) (100 * od.getConfidence());
					String title = labelName + ": " + probability + "%";

					GraphicsUtils.drawBoundingBox(originalImage, cid, title, x1, y1, x2, y2, agnosticColors);

					if (this.drawMask && od.getMask() != null) {
						float[][] mask = od.getMask();
						if (mask != null) {
							Color maskColor = agnosticColors ? null : GraphicsUtils.getClassColor(cid);
							BufferedImage maskImage = GraphicsUtils.createMaskImage(
									mask, x2 - x1, y2 - y1, maskColor);
							GraphicsUtils.overlayImages(originalImage, maskImage, x1, y1);
						}
					}
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(originalImage, IMAGE_FORMAT, baos);
				baos.flush();
				imageBytes = baos.toByteArray();
				baos.close();
			}
			catch (IOException e) {
				logger.error(e);
			}
		}

		// Null mend that QR image is found and not output message will be send.
		return imageBytes;
	}

	private String toJson(List<ObjectDetection> objectDetections) {
		try {
			return new ObjectMapper().writeValueAsString(objectDetections);
		}
		catch (JsonProcessingException e) {
			logger.error("Failed to encode the object detections into JSON message", e);
		}
		return "ERROR";
	}

}
