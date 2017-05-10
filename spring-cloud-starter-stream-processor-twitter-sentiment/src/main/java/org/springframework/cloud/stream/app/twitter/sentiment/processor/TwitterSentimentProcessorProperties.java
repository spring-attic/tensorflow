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

package org.springframework.cloud.stream.app.twitter.sentiment.processor;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Twitter Sentiment Analysis Processor module.
 *
 * @author Christian Tzolov
 */
@ConfigurationProperties("tensorflow.twitter")
@Validated
public class TwitterSentimentProcessorProperties {

	/**
	 * The location of the word vocabulary file, used for training the model
	 */
	private Resource vocabulary;

	@NotNull
	public Resource getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(Resource vocabulary) {
		this.vocabulary = vocabulary;
	}
}
