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

import static org.springframework.cloud.stream.app.twitter.sentiment.processor.TwitterSentimentProcessorConfiguration.PROCESSOR_CONTEXT_TWEET_JSON_MAP;
import static org.springframework.util.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowInputConverter;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts the input Tweet JSON message into key/value map that corresponds to the Twitter Sentiment CNN model:
 * <code>
 *     data_in : vectorized TEXT tag
 *     dropout_keep_prob: 1.0f
 * </code>
 * It also preservers the original Tweet (encoded as Java Map) in the processor context. Later is used by the
 * output converter to compose the output json message.
 *
 * @author Christian Tzolov
 */
public class TwitterSentimentTensorflowInputConverter implements TensorflowInputConverter, AutoCloseable {

	public static final Float DROPOUT_KEEP_PROB_VALUE = new Float(1.0);

	public static final String DATA_IN = "data_in";

	public static final String DROPOUT_KEEP_PROB = "dropout_keep_prob";

	public static final String TWEET_TEXT_TAG = "text";

	public static final String TWEET_ID_TAG = "id";

	private static final Log logger = LogFactory.getLog(TwitterSentimentTensorflowInputConverter.class);

	private final WordVocabulary wordVocabulary;

	private final ObjectMapper objectMapper;

	public TwitterSentimentTensorflowInputConverter(Resource vocabularLocation) {
		try (InputStream is = vocabularLocation.getInputStream()) {
			wordVocabulary = new WordVocabulary(is);
			objectMapper = new ObjectMapper();
			Assert.notNull(wordVocabulary, "Failed to initialize the word vocabulary");
			Assert.notNull(objectMapper, "Failed to initialize the objectMapper");
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to initialize the Vocabulary", e);
		}

		logger.info("Word Vocabulary Initialized");
	}

	@Override
	public Map<String, Object> convert(Object input, Map<String, Object> processorContext) {

		try {
			Map tweetJsonMap = null;

			if (input instanceof byte[]) {
				tweetJsonMap = objectMapper.readValue((byte[]) input, Map.class);
			}
			else if (input instanceof String) {
				tweetJsonMap = objectMapper.readValue((String) input, Map.class);
			}
			else if (input instanceof Map) {
				tweetJsonMap = (Map) input;
			}

			if (tweetJsonMap != null) {
				processorContext.put(PROCESSOR_CONTEXT_TWEET_JSON_MAP, tweetJsonMap);
				return getStringObjectMap(tweetJsonMap);
			}

			throw new IllegalArgumentException("Unsupported payload type:" + input);
		}
		catch (IOException e) {
			throw new RuntimeException("Can't parse input tweet json: " + input);
		}

	}

	private Map<String, Object> getStringObjectMap(Map jsonMap) {
		Assert.notNull(jsonMap, "Failed to parse the Tweet json!");

		String tweetText = (String) jsonMap.get(TWEET_TEXT_TAG);

		if (isEmpty(tweetText)) {
			logger.warn("Tweet with out text: " + jsonMap.get(TWEET_ID_TAG));
			tweetText = "";
		}

		int[][] tweetVector = wordVocabulary.vectorizeSentence(tweetText);

		Assert.notEmpty(tweetVector, "Failed to vectorize the tweet text: " + tweetText);

		Map<String, Object> response = new HashMap<>();
		response.put(DATA_IN, tweetVector);
		response.put(DROPOUT_KEEP_PROB, DROPOUT_KEEP_PROB_VALUE);

		return response;
	}

	@Override
	public void close() throws Exception {
		logger.info("Word Vocabulary Destroyed");
		if (wordVocabulary != null) {
			wordVocabulary.close();
		}
	}

}
