/*
 * Copyright 2017-2018 the original author or authors.
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

package org.springframework.cloud.stream.app.tensorflow.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Session.Runner;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.tensorflow.util.ModelExtractor;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * @author Christian Tzolov
 */
public class TensorFlowService implements AutoCloseable {

	private static final Log logger = LogFactory.getLog(TensorflowCommonProcessorConfiguration.class);

	private Graph graph;

	public TensorFlowService(Resource modelLocation) {
		if (logger.isInfoEnabled()) {
			logger.info("Loading TensorFlow graph model: " + modelLocation);
		}
		graph = new Graph();
		byte[] model = new ModelExtractor().getModel(modelLocation);
		graph.importGraphDef(model);
	}

	/**
	 * Evaluates a pre-trained tensorflow model (encoded as {@link Graph}). Use the feeds parameter to feed in the
	 * model input data and fetch-names to specify the output tensors.
	 *
	 * @param feeds Named map of input tensors. Tensors can be encoded as {@link Tensor} or JSON string objects.
	 * @param fetchedNames Names of the output tensors computed by the model.
	 * @return Returns the computed output tensors. The names of the output tensors is defined by the fetchedNames
	 * argument
	 */
	public Map<String, Tensor<?>> evaluate(Map<String, Object> feeds, List<String> fetchedNames) {

		try (Session session = new Session(graph)) {

			Runner runner = session.runner();

			// Keep tensor references to release them in the finally block
			Tensor[] feedTensors = new Tensor[feeds.size()];
			try {
				// Feed in the input named tensors
				int inputIndex = 0;
				for (Entry<String, Object> e : feeds.entrySet()) {
					String feedName = e.getKey();
					feedTensors[inputIndex] = toFeedTensor(e.getValue());
					runner = runner.feed(feedName, feedTensors[inputIndex]);
					inputIndex++;
				}

				// Set the tensor name to be fetched after the evaluation
				for (String fetchName : fetchedNames) {
					runner.fetch(fetchName);
				}

				// Evaluate the input
				List<Tensor<?>> outputTensors = runner.run();

				// Extract the output tensors
				Map<String, Tensor<?>> outTensorMap = new HashMap<>();
				for (int outputIndex = 0; outputIndex < fetchedNames.size(); outputIndex++) {
					outTensorMap.put(fetchedNames.get(outputIndex), outputTensors.get(outputIndex));
				}
				return outTensorMap;
			}
			finally {
				// Release all feed tensors
				for (Tensor tensor : feedTensors) {
					if (tensor != null) {
						tensor.close();
					}
				}
			}
		}
	}

	/**
	 * Convert an object into {@link Tensor} instance. Supports java primitive types, JSON string encoded
	 * tensors or {@link Tensor} instances.
	 * @param value Can be a java primitive type, JSON string encoded tensors or {@link Tensor} instances.
	 * @return Tensor instance representing the input object value.
	 */
	private Tensor toFeedTensor(Object value) {
		if (value instanceof Tensor) {
			return (Tensor) value;
		}
		else if (value instanceof String) {
			return TensorJsonConverter.toTensor((String) value);
		}
		else if (value instanceof byte[]) {
			return TensorJsonConverter.toTensor(new String((byte[]) value));
		}

		return Tensor.create(value);
	}

	@Override
	public void close() {
		logger.info("Close TensorFlow Graph!");
		if (graph != null) {
			graph.close();
		}
	}

}
