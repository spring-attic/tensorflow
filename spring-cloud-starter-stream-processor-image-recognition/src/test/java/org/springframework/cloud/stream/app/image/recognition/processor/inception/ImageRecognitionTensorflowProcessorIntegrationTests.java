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

package org.springframework.cloud.stream.app.image.recognition.processor.inception;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.image.recognition.processor.ImageRecognitionProcessorConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import static org.hamcrest.Matchers.equalTo;

/**
 * @author Christian Tzolov
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"tensorflow.model=http://dl.bintray.com/big-data/generic/tensorflow_inception_graph.pb",
				"tensorflow.modelFetch=output",
				"tensorflow.image.recognition.labels=http://dl.bintray.com/big-data/generic/imagenet_comp_graph_label_strings.txt"
		})
@DirtiesContext
public abstract class ImageRecognitionTensorflowProcessorIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=header"
	})
	public static class OutputInHeaderTests extends ImageRecognitionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				testEvaluationWithOutputInHeader(
						image, "{\"labels\":[{\"giant panda\":0.98649305}]}");
			}
		}

		private void testEvaluationWithOutputInHeader(byte[] image, String resultJson) {
			channels.input().send(MessageBuilder.withPayload(image).build());

			Message<?> received = messageCollector.forChannel(channels.output()).poll();
			System.out.println(received.getHeaders().get("output"));
			Assert.assertThat(received.getHeaders().get("result").toString(), equalTo(resultJson));
		}
	}

	@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=payload"
	})
	public static class OutputInPayloadTests extends ImageRecognitionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

				Assert.assertThat(new String(received.getPayload()),
						equalTo("{\"labels\":[{\"giant panda\":0.98649305}]}"));
			}
		}
	}

	@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.image.recognition.responseSize=3"
	})
	public static class OutputWithAlternativesTests extends ImageRecognitionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

				Assert.assertThat(new String(received.getPayload()),
						equalTo("{\"labels\":[" +
								"{\"giant panda\":0.98649305}," +
								"{\"badger\":0.010562794}," +
								"{\"ice bear\":0.001130851}]}"));
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ImageRecognitionProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
