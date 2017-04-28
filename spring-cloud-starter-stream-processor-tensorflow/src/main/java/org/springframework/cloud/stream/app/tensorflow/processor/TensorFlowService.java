/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.tensorflow.processor;

import static org.apache.commons.io.IOUtils.buffer;
import static org.apache.commons.io.IOUtils.toByteArray;

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

/**
 * @author Christian Tzolov
 */
public class TensorFlowService implements AutoCloseable {

	private static final Log logger = LogFactory.getLog(TensorflowProcessorConfiguration.class);

	private Graph graph;

	public TensorFlowService(Resource modelLocation) throws IOException {
		try (InputStream is = modelLocation.getInputStream()) {
			graph = new Graph();
			logger.info("Loading TensorFlow graph model: " + modelLocation );
			graph.importGraphDef(toByteArray(buffer(is)));
			logger.info("TensorFlow Graph Model Ready To Serve!");
		}
	}

	public Tensor evaluate(Map<String, Object> feeds, String outputName, int outputIndex) {

		try (Session session = new Session(graph)) {
			Runner runner = session.runner();
			Tensor[] feedTensors = new Tensor[feeds.size()];
			try {
				int i = 0;
				for (Entry<String, Object> e : feeds.entrySet()) {
					Tensor tensor = Tensor.create(e.getValue());
					runner = runner.feed(e.getKey(), tensor);
					feedTensors[i++] = tensor;
				}
				return runner.fetch(outputName).run().get(outputIndex);
			}
			finally {
				if (feedTensors != null) {
					for (Tensor tensor : feedTensors) {
						if (tensor != null) {
							tensor.close();
						}
					}
				}
			}
		}
	}

	@Override
	public void close() throws Exception {
		logger.info("Close TensorFlow Graph!");
		if (graph != null) {
			graph.close();
		}
	}
}
