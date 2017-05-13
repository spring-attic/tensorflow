/*
 * Copyright 2017-2018 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowInputConverter;

/**
 * @author Christian Tzolov
 */
public class ImageRecognitionTensorflowInputConverter implements TensorflowInputConverter, AutoCloseable {

	private static final Log logger = LogFactory.getLog(ImageRecognitionTensorflowInputConverter.class);

	private final Graph graph;

	private final Output graphOutput;

	public ImageRecognitionTensorflowInputConverter() {
		graph = new Graph();
		GraphBuilder b = new GraphBuilder(graph);
		// Some constants specific to the pre-trained model at:
		// https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
		// - The model was trained with images scaled to 224x224 pixels.
		// - The colors, represented as R, G, B in 1-byte each were converted to
		//   float using (value - Mean)/Scale.
		final int H = 224;
		final int W = 224;
		final float mean = 117f;
		final float scale = 1f;

		final Output input = b.placeholder("input", DataType.STRING);
		graphOutput =
				b.div(
						b.sub(
								b.resizeBilinear(
										b.expandDims(
												b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
												b.constant("make_batch", 0)),
										b.constant("size", new int[] { H, W })),
								b.constant("mean", mean)),
						b.constant("scale", scale));

	}

	private Tensor constructAndExecuteGraphToNormalizeImage3(byte[] imageBytes) {
		try (Session s = new Session(graph)) {
			try (Tensor inputTensor = Tensor.create(imageBytes)) {
				return s.runner().feed("input", inputTensor).fetch(graphOutput.op().name()).run().get(0);
			}
		}
	}

	@Override
	public void close() throws Exception {
		logger.info("Input Graph Destroyed");
		if (graph != null) {
			graph.close();
		}
	}

	@Override
	public Map<String, Object> convert(Object input, Map<String, Object> processorContext) {

		if (input instanceof byte[]) {
			Tensor inputImageTensor = constructAndExecuteGraphToNormalizeImage3((byte[]) input);
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("input", inputImageTensor);

			return inputMap;
		}

		throw new IllegalArgumentException("Unsupported payload type: " + input);

	}

	// In the fullness of time, equivalents of the methods of this class should be auto-generated from
	// the OpDefs linked into libtensorflow_jni.so. That would match what is done in other languages
	// like Python, C++ and Go.
	static class GraphBuilder {
		private Graph g;

		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output div(Output x, Output y) {
			return binaryOp("Div", x, y);
		}

		Output sub(Output x, Output y) {
			return binaryOp("Sub", x, y);
		}

		Output resizeBilinear(Output images, Output size) {
			return binaryOp("ResizeBilinear", images, size);
		}

		Output expandDims(Output input, Output dim) {
			return binaryOp("ExpandDims", input, dim);
		}

		Output cast(Output value, DataType dtype) {
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
		}

		Output decodeJpeg(Output contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg")
					.addInput(contents)
					.setAttr("channels", channels)
					.build()
					.output(0);
		}

		Output constant(String name, Object value) {
			try (Tensor t = Tensor.create(value)) {
				return g.opBuilder("Const", name)
						.setAttr("dtype", t.dataType())
						.setAttr("value", t)
						.build()
						.output(0);
			}
		}

		Output placeholder(String name, DataType dtype) {
			return g.opBuilder("Placeholder", name)
					.setAttr("dtype", dtype)
					.build()
					.output(0);
		}

		private Output binaryOp(String type, Output in1, Output in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
		}
	}

}
