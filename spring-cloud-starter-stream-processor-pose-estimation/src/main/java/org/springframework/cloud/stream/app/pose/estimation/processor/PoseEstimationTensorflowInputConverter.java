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

package org.springframework.cloud.stream.app.pose.estimation.processor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowInputConverter;
import org.springframework.cloud.stream.app.tensorflow.util.GraphicsUtils;

/**
 * Converts byte array image into a input Tensor for the Pose Estimation API. The computed image tensors uses the
 * 'image' model placeholder.
 *
 * @author Christian Tzolov
 */
public class PoseEstimationTensorflowInputConverter implements TensorflowInputConverter {

	private static final Log logger = LogFactory.getLog(PoseEstimationTensorflowInputConverter.class);

	private static final long BATCH_SIZE = 1;
	private static final long CHANNELS = 3;
	public static final String IMAGE_TENSOR_FEED_NAME = "image";
	private final static int[] COLOR_CHANNELS = new int[] { 0, 1, 2 };

	private PoseEstimationProcessorProperties properties;

	public PoseEstimationTensorflowInputConverter(PoseEstimationProcessorProperties properties) {
		this.properties = properties;
	}

	@Override
	public Map<String, Object> convert(Object input, Map<String, Object> processorContext) {

		if (input instanceof byte[]) {
			try {
				Tensor inputImageTensor = makeImageTensor((byte[]) input);
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put(IMAGE_TENSOR_FEED_NAME, inputImageTensor);

				if (properties.isDebugVisualisationEnabled()) {
					processorContext.put("inputImage", input);
				}

				return inputMap;
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Incorrect image format", e);
			}
		}

		throw new IllegalArgumentException(String.format("Expected byte[] payload type, found: %s", input));
	}

	private Tensor<Float> makeImageTensor(byte[] imageBytes) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
		BufferedImage img = ImageIO.read(is);

		if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			throw new IllegalArgumentException(
					String.format("Expected 3-byte BGR encoding in BufferedImage, found %d", img.getType()));
		}

		// ImageIO.read produces BGR-encoded images, while the model expects RGB.
		int[] data = toIntArray(img);

		//Expand dimensions since the model expects images to have shape: [1, None, None, 3]
		long[] shape = new long[] { BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS };

		return Tensor.create(shape, FloatBuffer.wrap(toRgbFloat(data)));
	}

	private int[] toIntArray(BufferedImage image) {
		BufferedImage imgToRecognition = GraphicsUtils.toBufferedImage(image);
		return ((DataBufferInt) imgToRecognition.getRaster().getDataBuffer()).getData();
	}

	private float[] toRgbFloat(int[] data) {
		float[] float_image = new float[data.length * 3];
		for (int i = 0; i < data.length; ++i) {
			final int val = data[i];
			float_image[i * 3 + COLOR_CHANNELS[0]] = ((val >> 16) & 0xFF); //R
			float_image[i * 3 + COLOR_CHANNELS[1]] = ((val >> 8) & 0xFF);  //G
			float_image[i * 3 + COLOR_CHANNELS[2]] = (val & 0xFF);         //B
		}
		return float_image;
	}

}
