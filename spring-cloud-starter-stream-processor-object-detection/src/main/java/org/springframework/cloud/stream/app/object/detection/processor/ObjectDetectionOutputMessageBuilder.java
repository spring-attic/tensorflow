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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.app.tensorflow.processor.DefaultOutputMessageBuilder;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowCommonProcessorProperties;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.tuple.JsonStringToTupleConverter;
import org.springframework.tuple.Tuple;

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

	private final boolean agnosticColors;

	public ObjectDetectionOutputMessageBuilder(boolean drawBoundingBox, boolean agnosticColors,
			TensorflowCommonProcessorProperties properties) {
		super(properties);
		this.drawBoundingBox = drawBoundingBox;
		this.agnosticColors = agnosticColors;
	}

	@Override
	public MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore) {
		Message<?> annotatedInput = inputMessage;

		if (this.drawBoundingBox) {
			byte[] annotatedImage = drawBoundingBox((byte[]) inputMessage.getPayload(), computedScore);
			annotatedInput = MessageBuilder.withPayload(annotatedImage).build();
		}

		return super.createOutputMessageBuilder(annotatedInput, computedScore);
	}

	private byte[] drawBoundingBox(byte[] imageBytes, Object result) {
		if (result != null) {
			try {
				BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

				Tuple resultTuple = new JsonStringToTupleConverter().convert(result.toString());
				ArrayList<Tuple> labels = (ArrayList) resultTuple.getValues().get(0);

				for (Tuple l : labels) {
					int y1 = (int) (l.getFloat(1) * (float) originalImage.getHeight());
					int x1 = (int) (l.getFloat(2) * (float) originalImage.getWidth());
					int y2 = (int) (l.getFloat(3) * (float) originalImage.getHeight());
					int x2 = (int) (l.getFloat(4) * (float) originalImage.getWidth());

					int cid = l.getInt("cid");

					String labelName = l.getFieldNames().get(0);
					int probability = (int) (100 * l.getFloat(0));
					String title = labelName + ": " + probability + "%";

					GraphicsUtils.drawBoundingBox(originalImage, cid, title, x1, y1, x2, y2, agnosticColors);
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

}
