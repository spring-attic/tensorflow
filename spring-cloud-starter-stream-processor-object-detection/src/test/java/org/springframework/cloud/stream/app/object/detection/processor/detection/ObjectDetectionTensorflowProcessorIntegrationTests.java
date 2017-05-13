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

package org.springframework.cloud.stream.app.object.detection.processor.detection;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.object.detection.processor.ObjectDetectionProcessorConfiguration;
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
				"tensorflow.modelFetch=detection_scores,detection_classes,detection_boxes",
				"tensorflow.model=http://dl.bintray.com/big-data/generic/faster_rcnn_resnet101_coco_2018_01_28_frozen_inference_graph.pb",
				"tensorflow.object.detection.labels=http://dl.bintray.com/big-data/generic/mscoco_label_map.pbtxt"
				//"tensorflow.model=file:/Users/ctzolov/Downloads/ssd_inception_v2_coco_2017_11_17/frozen_inference_graph.pb",
				//"tensorflow.model=file:/Users/ctzolov/Downloads/faster_rcnn_inception_resnet_v2_atrous_lowproposals_oid_2018_01_28/frozen_inference_graph.pb",
				//"tensorflow.object.detection.labels=file:/Users/ctzolov/Downloads/oid_bbox_trainable_label_map.pbtxt"
		})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class ObjectDetectionTensorflowProcessorIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;


	@TestPropertySource(properties = {
			"tensorflow.mode=header"
	})
	public static class OutputInHeaderTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/tourists.jpg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				testEvaluationWithOutputInHeader(
						image, "{\"labels\":[" +
								"{\"person\":0.9996774,\"x1\":0.0,\"y1\":0.3940161,\"x2\":0.9465165,\"y2\":0.5592592}," +
								"{\"person\":0.9996604,\"x1\":0.047891676,\"y1\":0.03169123,\"x2\":0.941098,\"y2\":0.2085562}," +
								"{\"person\":0.9994741,\"x1\":0.038780525,\"y1\":0.20172822,\"x2\":0.9464298,\"y2\":0.39004815}," +
								"{\"person\":0.99946576,\"x1\":0.0632496,\"y1\":0.79281414,\"x2\":0.96308815,\"y2\":0.99012446}," +
								"{\"person\":0.99867463,\"x1\":0.051441744,\"y1\":0.57940257,\"x2\":0.9477767,\"y2\":0.8259268}," +
								"{\"backpack\":0.96534747,\"x1\":0.15588468,\"y1\":0.85957795,\"x2\":0.5091308,\"y2\":0.9908878}," +
								"{\"backpack\":0.963343,\"x1\":0.1273736,\"y1\":0.57658505,\"x2\":0.47765,\"y2\":0.6986431}," +
								"{\"backpack\":0.9294457,\"x1\":0.14560387,\"y1\":0.022079457,\"x2\":0.53702116,\"y2\":0.113561034}," +
								"{\"backpack\":0.6643621,\"x1\":0.53017575,\"y1\":0.31962925,\"x2\":0.77074707,\"y2\":0.4030401}," +
								"{\"backpack\":0.57413465,\"x1\":0.1278874,\"y1\":0.457999,\"x2\":0.3998378,\"y2\":0.5622766}]}");
			}
		}

		private void testEvaluationWithOutputInHeader(byte[] image, String resultJson) {
			channels.input().send(MessageBuilder.withPayload(image).build());
			Message<?> received = messageCollector.forChannel(channels.output()).poll();
			Assert.assertThat(received.getHeaders().get("result").toString(), equalTo(resultJson));
		}
	}

	@TestPropertySource(properties = {
			"tensorflow.mode=payload"
	})
	public static class OutputInPayloadTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

				Assert.assertThat(new String(received.getPayload()),
						equalTo("{\"labels\":[{\"bear\":0.9999691," +
								"\"x1\":0.072180204,\"y1\":0.2741249,\"x2\":0.893106,\"y2\":0.77897394}]}"));
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ObjectDetectionProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
