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

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Christian Tzolov
 */
public class TwitterSentimentProcessorPropertiesTests {

	@Test
	public void vocabularyCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("tensorflow.twitter.vocabulary=/remote").applyTo(context);
		context.register(Conf.class);
		context.refresh();
		TwitterSentimentProcessorProperties properties = context.getBean(TwitterSentimentProcessorProperties.class);
		assertThat(properties.getVocabulary(), equalTo(context.getResource("/remote")));
		context.close();
	}

	@Configuration
	@EnableConfigurationProperties(TwitterSentimentProcessorProperties.class)
	static class Conf {

	}

}
