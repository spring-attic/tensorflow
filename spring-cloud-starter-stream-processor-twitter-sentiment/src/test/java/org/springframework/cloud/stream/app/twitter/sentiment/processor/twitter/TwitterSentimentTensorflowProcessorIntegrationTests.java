/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.twitter.sentiment.processor.twitter;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.cloud.stream.app.tensorflow.processor.TensorflowProcessorConfiguration.TF_OUTPUT_HEADER;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.twitter.sentiment.processor.TwitterSentimentProcessorConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for TwitterSentimentTensorflowProcessor
 *
 * @author Christian Tzolov
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"tensorflow.modelLocation=http://dl.bintray.com/big-data/generic/minimal_graph.proto",
				"tensorflow.outputName=output/Softmax",
				"twitter.vocabularyLocation=http://dl.bintray.com/big-data/generic/vocab.csv"
		})
@DirtiesContext
public abstract class TwitterSentimentTensorflowProcessorIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@TestPropertySource(properties = {"tensorflow.saveOutputInHeader=true"})
	public static class OutputInHeaderTests extends TwitterSentimentTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() {
			testEvaluationWithOutputInHeader(
					"{\"text\": \"RT @PostGradProblem: In preparation for the NFL lockout ...\", \"id\":666, \"lang\":\"en\" }",
					"{\"sentiment\":\"POSITIVE\",\"text\":\"RT @PostGradProblem: In preparation for the NFL lockout ...\",\"id\":666,\"lang\":\"en\"}");
		}

		@Test
		public void testEvaluationNegative() {
			testEvaluationWithOutputInHeader(
					"{\"text\": \"This is really bad\", \"id\":666, \"lang\":\"en\" }",
					"{\"sentiment\":\"NEGATIVE\",\"text\":\"This is really bad\",\"id\":666,\"lang\":\"en\"}");
		}

		private void testEvaluationWithOutputInHeader(String tweetJson, String resultJson) {
			channels.input().send(MessageBuilder.withPayload(tweetJson).build());

			Message<String> received = (Message<String>) messageCollector.forChannel(channels.output()).poll();

			Assert.assertThat(received.getPayload(), equalTo(tweetJson));
			Assert.assertThat(received.getHeaders().get(TF_OUTPUT_HEADER).toString(), equalTo(resultJson));
		}
	}

	@TestPropertySource(properties = {"tensorflow.saveOutputInHeader=false"})
	public static class OutputInPayloadTests extends TwitterSentimentTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() {
			String value = "{\"text\": \"RT @PostGradProblem: In preparation for the NFL lockout ...\", \"id\":666, \"lang\":\"en\" }";

			channels.input().send(MessageBuilder.withPayload(value).build());

			Message<String> received = (Message<String>) messageCollector.forChannel(channels.output()).poll();

			Assert.assertTrue(received.getPayload().getClass().isAssignableFrom(String.class));
			Assert.assertThat(received.getPayload().toString(),
					equalTo("{\"sentiment\":\"POSITIVE\",\"text\":\"RT @PostGradProblem: In preparation for the NFL lockout ...\",\"id\":666,\"lang\":\"en\"}"));
		}
	}

	@SpringBootApplication
	@Import(TwitterSentimentProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}
}
