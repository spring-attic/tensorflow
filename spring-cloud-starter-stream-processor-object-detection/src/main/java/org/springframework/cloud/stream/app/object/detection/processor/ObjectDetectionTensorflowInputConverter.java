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
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowInputConverter;

/**
 * @author Christian Tzolov
 */
public class ObjectDetectionTensorflowInputConverter implements TensorflowInputConverter {

	private static final Log logger = LogFactory.getLog(ObjectDetectionTensorflowInputConverter.class);

	@Override
	public Map<String, Object> convert(Object input, Map<String, Object> processorContext) {

		if (input instanceof byte[]) {
			try {
				Tensor inputImageTensor = makeImageTensor((byte[]) input);
				Map<String, Object> inputMap = new HashMap<>();
				inputMap.put("image_tensor", inputImageTensor);

				return inputMap;
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Incorrect image format", e);
			}
		}

		throw new IllegalArgumentException("Unsupported payload type: " + input);

	}

	private static void bgr2rgb(byte[] data) {
		for (int i = 0; i < data.length; i += 3) {
			byte tmp = data[i];
			data[i] = data[i + 2];
			data[i + 2] = tmp;
		}
	}

	private static Tensor<UInt8> makeImageTensor(byte[] imageBytes) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
		BufferedImage img = ImageIO.read(is);
		//img.getGraphics().drawRect(10, 10, 70, 70);

		if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			throw new IOException(
					String.format("Expected 3-byte BGR encoding in BufferedImage, found %d", img.getType()));
		}
		byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
		// ImageIO.read seems to produce BGR-encoded images, but the model expects RGB.
		bgr2rgb(data);
		final long BATCH_SIZE = 1;
		final long CHANNELS = 3;
		long[] shape = new long[] { BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS };

		return Tensor.create(UInt8.class, shape, ByteBuffer.wrap(data));
	}

}
