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

package org.springframework.cloud.stream.app.image.recognition.processor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowOutputConverter;
import org.springframework.core.io.Resource;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Floats;

/**
 * @author Christian Tzolov
 */
public class ImageRecognitionTensorflowOutputConverter implements TensorflowOutputConverter<Tuple> {

	private static final Log logger = LogFactory.getLog(ImageRecognitionTensorflowOutputConverter.class);

	private final List<String> labels;

	private final int responseSize;

	public ImageRecognitionTensorflowOutputConverter(Resource labels, int responseSize) {
		this.responseSize = responseSize;
		try (InputStream is = labels.getInputStream()) {
			this.labels = Arrays.asList(StreamUtils.copyToString(is, Charset.forName("UTF-8")).split("\n"));
			Assert.notNull(this.labels, "Failed to initialize the labels list");
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to initialize the Vocabulary", e);
		}

		logger.info("Word Vocabulary Initialized");
	}

	@Override
	public Tuple convert(Tensor tensor, Map<String, Object> processorContext) {
		final long[] rshape = tensor.shape();
		if (tensor.numDimensions() != 2 || rshape[0] != 1) {
			throw new RuntimeException(
					String.format(
							"Expected model to produce a [1 N] shaped tensor where N is the number of labels, " +
									"instead it produced one with shape %s",
							Arrays.toString(rshape)));
		}
		int labelsCount = (int) rshape[1];

		float[] labelProbabilities = tensor.copyTo(new float[1][labelsCount])[0];

		List<Tuple> tuples =new ArrayList<>(responseSize);
		if (responseSize == 1) {
			int maxProbabilityIndex = maxProbabilityIndex(labelProbabilities);
			tuples.add(TupleBuilder.tuple().of(labels.get(maxProbabilityIndex), labelProbabilities[maxProbabilityIndex]));
		}
		else {
			List<Integer> top10Probabilities = topKProbabilities(labelProbabilities, responseSize);

			for (int i = 0; i < top10Probabilities.size(); i++) {
				int probabilityIndex = top10Probabilities.get(i);
				tuples.add(TupleBuilder.tuple().of(labels.get(probabilityIndex), labelProbabilities[probabilityIndex]));
			}
		}

		return TupleBuilder.tuple().of("labels", tuples);

	}

	private List<Integer> topKProbabilities(final float[] labelProbabilities, int k) {

		List<Integer> list = new ArrayList<>(labelProbabilities.length);
		for (int i = 0; i < labelProbabilities.length; i++) {
			list.add(i);
		}

		List<Integer> topK = new Ordering<Integer>() {
			@Override
			public int compare(Integer left, Integer right) {
				return Floats.compare(labelProbabilities[left], labelProbabilities[right]);
			}
		}.greatestOf(list, k);

		return topK;
	}


	private int maxProbabilityIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

}
