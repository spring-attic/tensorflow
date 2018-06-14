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
				"tensorflow.model=http://dl.bintray.com/big-data/generic/faster_rcnn_resnet101_coco_2018_01_28_frozen_inference_graph.pb",
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

	@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=header"
	})
	public static class OutputInHeaderTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException {
			//try (InputStream is = new ClassPathResource("/images/tourists.jpg").getInputStream()) {
			try (InputStream is = new ClassPathResource("/images/object-detection.jpg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				testEvaluationWithOutputInHeader(
						image, "{\"labels\":[" +
								"{\"person\":0.99666023,\"x1\":0.7767177,\"y1\":0.15963306,\"x2\":0.9540965,\"y2\":0.20158374,\"cid\":1}," +
								"{\"kite\":0.99346715,\"x1\":0.08797318,\"y1\":0.44025636,\"x2\":0.16985515,\"y2\":0.49672586,\"cid\":38}," +
								"{\"person\":0.99229527,\"x1\":0.6840088,\"y1\":0.08519748,\"x2\":0.843071,\"y2\":0.12301033,\"cid\":1}," +
								"{\"person\":0.9789544,\"x1\":0.56879956,\"y1\":0.06220094,\"x2\":0.629103,\"y2\":0.07995274,\"cid\":1}," +
								"{\"kite\":0.96934885,\"x1\":0.26183245,\"y1\":0.2070512,\"x2\":0.3146273,\"y2\":0.22642946,\"cid\":38}," +
								"{\"kite\":0.9462258,\"x1\":0.43824652,\"y1\":0.8026538,\"x2\":0.4702088,\"y2\":0.8175442,\"cid\":38}," +
								"{\"kite\":0.92378986,\"x1\":0.37467834,\"y1\":0.3474477,\"x2\":0.3987156,\"y2\":0.36179984,\"cid\":38}," +
								"{\"kite\":0.9130423,\"x1\":0.38156357,\"y1\":0.42770827,\"x2\":0.41003278,\"y2\":0.4449141,\"cid\":38}," +
								"{\"kite\":0.89646775,\"x1\":0.41558802,\"y1\":0.22641928,\"x2\":0.45246547,\"y2\":0.24253754,\"cid\":38}," +
								"{\"person\":0.83506095,\"x1\":0.601887,\"y1\":0.13212581,\"x2\":0.63641346,\"y2\":0.14473228,\"cid\":1}," +
								"{\"person\":0.7343776,\"x1\":0.5767689,\"y1\":0.023850055,\"x2\":0.62417924,\"y2\":0.038195316,\"cid\":1}," +
								"{\"person\":0.58631957,\"x1\":0.5590419,\"y1\":0.38939288,\"x2\":0.5883101,\"y2\":0.3997791,\"cid\":1}," +
								"{\"person\":0.57765806,\"x1\":0.5586649,\"y1\":0.38527238,\"x2\":0.58879423,\"y2\":0.3959817,\"cid\":1}," +
								"{\"person\":0.572748,\"x1\":0.5611651,\"y1\":0.3912078,\"x2\":0.59040546,\"y2\":0.4047312,\"cid\":1}," +
								"{\"person\":0.55604917,\"x1\":0.54020363,\"y1\":0.25858936,\"x2\":0.5598617,\"y2\":0.2650398,\"cid\":1}," +
								"{\"kite\":0.46935454,\"x1\":0.4220001,\"y1\":0.5656192,\"x2\":0.43472755,\"y2\":0.57504195,\"cid\":38}," +
								"{\"person\":0.43634608,\"x1\":0.5645142,\"y1\":0.3955184,\"x2\":0.5911028,\"y2\":0.4101856,\"cid\":1}]}"
				);
			}
		}

		private void testEvaluationWithOutputInHeader(byte[] image, String resultJson) {
			channels.input().send(MessageBuilder.withPayload(image).build());
			Message<?> received = messageCollector.forChannel(channels.output()).poll();
			Assert.assertThat(received.getHeaders().get("result").toString(), equalTo(resultJson));
		}
	}

	@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
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
								"\"x1\":0.072180204,\"y1\":0.2741249,\"x2\":0.893106,\"y2\":0.77897394,\"cid\":23}]}"));
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ObjectDetectionProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
