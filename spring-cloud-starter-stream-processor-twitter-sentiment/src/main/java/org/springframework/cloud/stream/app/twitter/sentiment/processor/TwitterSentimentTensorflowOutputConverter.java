/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.twitter.sentiment.processor;

import java.util.HashMap;
import java.util.Map;

import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowOutputConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Decodes the evaluated result into POSITIVE, NEGATIVE and NEUTRAL values.
 * Then creates and returns a simple JSON message with this structure:
 * <code>
 *     {
 *      "sentiment" : "... computed sentiment type ...",
 *      "text" : "...TEXT tag form the input json tweet...",
 *      "id" : "...ID tag form the input json tweet...",
 *      "lang" : "...LANG tag form the input json tweet..."
 *      }
 * </code>
 * @author Christian Tzolov
 */
public class TwitterSentimentTensorflowOutputConverter implements TensorflowOutputConverter<String> {

	public static final String SENTIMENT_TAG = "sentiment";

	public static final String TEXT_TAG = "text";

	public static final String ID_TAG = "id";

	public static final String LANG_TAG = "lang";

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convert(Map<String, Tensor<?>> tensorMap, Map<String, Object> processorContext) {
		Tensor tensor = tensorMap.entrySet().iterator().next().getValue();
		// Read Tensor's value into float[][] matrix
		float[][] resultMatrix = new float[12][2];
		tensor.copyTo(resultMatrix);
		String sentimentString = Sentiment.get(resultMatrix[0][1]).toString();

		// Prepare teh output map
		Map inputJsonMap = (Map) processorContext.get(
				TwitterSentimentProcessorConfiguration.PROCESSOR_CONTEXT_TWEET_JSON_MAP);

		Map<String, Object> outputJsonMap = new HashMap<>();
		outputJsonMap.put(SENTIMENT_TAG, sentimentString);
		outputJsonMap.put(TEXT_TAG, inputJsonMap.get(TEXT_TAG));
		outputJsonMap.put(ID_TAG, inputJsonMap.get(ID_TAG));
		outputJsonMap.put(LANG_TAG, inputJsonMap.get(LANG_TAG));

		try {
			return objectMapper.writeValueAsString(outputJsonMap);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to generate JSON output", e);
		}
	}

}
