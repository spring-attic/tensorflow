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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.protobuf.TextFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.object.detection.protos.StringIntLabelMapOuterClass;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowOutputConverter;
import org.springframework.core.io.Resource;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

/**
 * @author Christian Tzolov
 */
public class ObjectDetectionTensorflowOutputConverter implements TensorflowOutputConverter<Tuple> {

	private static final Log logger = LogFactory.getLog(ObjectDetectionTensorflowOutputConverter.class);

	private final String[] labels;
	private float confidence;

	public ObjectDetectionTensorflowOutputConverter(Resource labelsResource, float confidence) {
		this.confidence = confidence;
		try {
			this.labels = loadLabels(labelsResource);
			Assert.notNull(this.labels, "Failed to initialize the labels list");
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize the Vocabulary", e);
		}

		logger.info("Word Vocabulary Initialized");
	}

	private static String[] loadLabels(Resource labelsResource) throws Exception {
		try (InputStream is = labelsResource.getInputStream()) {
			String text = StreamUtils.copyToString(is, Charset.forName("UTF-8"));
			StringIntLabelMapOuterClass.StringIntLabelMap.Builder builder =
					StringIntLabelMapOuterClass.StringIntLabelMap.newBuilder();
			TextFormat.merge(text, builder);
			StringIntLabelMapOuterClass.StringIntLabelMap proto = builder.build();
			int maxId = 0;
			for (StringIntLabelMapOuterClass.StringIntLabelMapItem item : proto.getItemList()) {
				if (item.getId() > maxId) {
					maxId = item.getId();
				}
			}
			String[] ret = new String[maxId + 1];
			for (StringIntLabelMapOuterClass.StringIntLabelMapItem item : proto.getItemList()) {
				ret[item.getId()] = item.getDisplayName();
			}
			return ret;
		}
	}

	@Override
	public Tuple convert(Map<String, Tensor<?>> tensorMap, Map<String, Object> processorContext) {

		try (Tensor<Float> scoresT = tensorMap.get("detection_scores").expect(Float.class);
			 Tensor<Float> classesT = tensorMap.get("detection_classes").expect(Float.class);
			 Tensor<Float> boxesT = tensorMap.get("detection_boxes").expect(Float.class)) {
			// All these tensors have:
			// - 1 as the first dimension
			// - maxObjects as the second dimension
			// While boxesT will have 4 as the third dimension (2 sets of (x, y) coordinates).
			// This can be verified by looking at scoresT.shape() etc.
			int maxObjects = (int) scoresT.shape()[1];
			float[] scores = scoresT.copyTo(new float[1][maxObjects])[0];
			float[] classes = classesT.copyTo(new float[1][maxObjects])[0];
			float[][] boxes = boxesT.copyTo(new float[1][maxObjects][4])[0];

			List<Tuple> tuples = new ArrayList<>();

			// Print all objects whose score is at least 0.5.
			for (int i = 0; i < scores.length; ++i) {
				if (scores[i] >= confidence) {
					String category = labels[(int) classes[i]];
					float score = scores[i];

					tuples.add(TupleBuilder.tuple()
							.put(category, score)
							.put("x1", boxes[i][0])
							.put("y1", boxes[i][1])
							.put("x2", boxes[i][2])
							.put("y2", boxes[i][3]).build());
				}
			}

			return TupleBuilder.tuple().of("labels", tuples);
		}
	}
}
