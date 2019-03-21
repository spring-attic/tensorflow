/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.cloud.stream.app.object.detection.processor.detection;

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
				//"tensorflow.modelFetch=detection_scores,detection_classes,detection_boxes,detection_masks,num_detections",
				"tensorflow.modelFetch=detection_scores,detection_classes,detection_boxes,num_detections",
				//"tensorflow.model=http://dl.bintray.com/big-data/generic/faster_rcnn_resnet101_coco_2018_01_28_frozen_inference_graph.pb",
				"tensorflow.model=http://dl.bintray.com/big-data/generic/ssdlite_mobilenet_v2_coco_2018_05_09_frozen_inference_graph.pb",
				//"tensorflow.model=file:/Users/ctzolov/Downloads/ssd_mobilenet_v2_coco_2018_03_29/frozen_inference_graph.pb",
				"tensorflow.object.detection.labels=http://dl.bintray.com/big-data/generic/mscoco_label_map.pbtxt",
				//"tensorflow.model=file:/Users/ctzolov/Downloads/mask_rcnn_resnet101_atrous_coco_2018_01_28/frozen_inference_graph.pb"
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

	//@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=header"
	})
	public static class OutputInHeaderTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		@Ignore("Asserting values need to be updated. Will fix after the release")
		public void testEvaluationPositive() throws IOException {
			//try (InputStream is = new ClassPathResource("/images/tourists.jpg").getInputStream()) {
			try (InputStream is = new ClassPathResource("/images/object-detection.jpg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				testEvaluationWithOutputInHeader(
						image, "{\"labels\":[" +
								"{\"kite\":0.86736834,\"x1\":0.081495196,\"y1\":0.44308645,\"x2\":0.169772,\"y2\":0.5014239,\"cid\":38}," +
								"{\"kite\":0.80015683,\"x1\":0.37845963,\"y1\":0.34496674,\"x2\":0.4024166,\"y2\":0.3610665,\"cid\":38}," +
								"{\"person\":0.78764266,\"x1\":0.5630007,\"y1\":0.39138338,\"x2\":0.59502745,\"y2\":0.40834948,\"cid\":1}," +
								"{\"person\":0.72429323,\"x1\":0.6802319,\"y1\":0.08181208,\"x2\":0.8319267,\"y2\":0.12482223,\"cid\":1}," +
								"{\"person\":0.6290598,\"x1\":0.57875264,\"y1\":0.059143394,\"x2\":0.61880136,\"y2\":0.075514555,\"cid\":1}," +
								"{\"person\":0.6122512,\"x1\":0.5782344,\"y1\":0.025721392,\"x2\":0.6188131,\"y2\":0.04140707,\"cid\":1}," +
								"{\"kite\":0.60772866,\"x1\":0.27496636,\"y1\":0.20563951,\"x2\":0.31009442,\"y2\":0.22761866,\"cid\":38}," +
								"{\"person\":0.5325224,\"x1\":0.76527464,\"y1\":0.15765251,\"x2\":0.9485351,\"y2\":0.20344453,\"cid\":1}]}"
				);
			}
		}

		private void testEvaluationWithOutputInHeader(byte[] image, String resultJson) {
			channels.input().send(MessageBuilder.withPayload(image).build());
			Message<?> received = messageCollector.forChannel(channels.output()).poll();
			Assert.assertThat(received.getHeaders().get("result").toString(), equalTo(resultJson));
		}
	}

	//@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=payload"
	})
	public static class OutputInPayloadTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		@Ignore("Asserting values need to be updated. Will fix after the release")
		public void testEvaluationPositive() throws IOException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

				Assert.assertThat(new String(received.getPayload()),
						equalTo("{\"labels\":[{\"bear\":0.9912719," +
								"\"x1\":0.08205441,\"y1\":0.31035388,\"x2\":0.87202954,\"y2\":0.7736771,\"cid\":23}]}"));
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ObjectDetectionProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
