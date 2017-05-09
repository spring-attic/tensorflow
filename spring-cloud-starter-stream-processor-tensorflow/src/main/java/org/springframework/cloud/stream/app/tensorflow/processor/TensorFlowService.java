/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.tensorflow.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Session.Runner;
import org.tensorflow.Tensor;

import org.springframework.core.io.Resource;
import org.springframework.tuple.Tuple;
import org.springframework.util.StreamUtils;

/**
 * @author Christian Tzolov
 */
public class TensorFlowService implements AutoCloseable {

	private static final Log logger = LogFactory.getLog(TensorflowProcessorConfiguration.class);

	private Graph graph;

	public TensorFlowService(Resource modelLocation) throws IOException {
		try (InputStream is = modelLocation.getInputStream()) {
			if (logger.isInfoEnabled()) {
				logger.info("Loading TensorFlow graph model: " + modelLocation);
			}
			graph = new Graph();
			graph.importGraphDef(StreamUtils.copyToByteArray(is));
		}
	}

	public Tensor evaluate(Map<String, Object> feeds, String fetchedOperationName, int outputTensorIndex) {

		try (Session session = new Session(graph)) {

			Runner runner = session.runner();

			// Keep tensor references to release them in the finally block
			Tensor[] feedTensors = new Tensor[feeds.size()];
			try {
				int i = 0;
				for (Entry<String, Object> e : feeds.entrySet()) {
					String feedName = e.getKey();
					feedTensors[i] = toFeedTensor(e.getValue());
					runner = runner.feed(feedName, feedTensors[i]);
					i++;
				}
				return runner.fetch(fetchedOperationName).run().get(outputTensorIndex);
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

	private Tensor toFeedTensor(Object value) {
		if (value instanceof Tensor) {
			return (Tensor) value;
		}
		else if (value instanceof Tuple) {
			return TensorTupleConverter.toTensor((Tuple) value);
		}

		return Tensor.create(value);
	}

	@Override
	public void close() throws Exception {
		logger.info("Close TensorFlow Graph!");
		if (graph != null) {
			graph.close();
		}
	}
}
