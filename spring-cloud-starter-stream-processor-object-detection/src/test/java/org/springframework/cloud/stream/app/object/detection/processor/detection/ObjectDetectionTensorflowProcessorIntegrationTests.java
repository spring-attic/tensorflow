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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.object.detection.processor.ObjectDetectionProcessorConfiguration;
import org.springframework.cloud.stream.app.test.tensorflow.JsonUtils;
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
				//"tensorflow.model=https://storage.googleapis.com/scdf-tensorflow-models/object-detection/faster_rcnn_resnet101_coco_2018_01_28_frozen_inference_graph.pb",
				"tensorflow.model=https://download.tensorflow.org/models/object_detection/ssdlite_mobilenet_v2_coco_2018_05_09.tar.gz#frozen_inference_graph.pb",
				//"tensorflow.model=https://download.tensorflow.org/models/object_detection/ssdlite_mobiledet_cpu_320x320_coco_2020_05_19.tar.gz",
				"tensorflow.object.detection.labels=https://storage.googleapis.com/scdf-tensorflow-models/object-detection/mscoco_label_map.pbtxt",
				//"tensorflow.model=file:/Users/ctzolov/Downloads/mask_rcnn_resnet101_atrous_coco_2018_01_28/frozen_inference_graph.pb"
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
		public void testHeaderMode() throws IOException, JSONException {
			try (InputStream is = new ClassPathResource("/images/object-detection.jpg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());
				Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

				JSONArray expected = new JSONArray(JsonUtils.resourceToString("classpath:/test-object-detection.json"));
				JSONAssert.assertEquals(expected, new JSONArray(received.getHeaders().get("result").toString()), false);

				IOUtils.write(received.getPayload(), new FileOutputStream("./target/out2.jpg"));
			}
		}
	}

	//@Ignore("Exclude the Processor Integration Test until a proper Mock TF Model is provided!")
	@TestPropertySource(properties = {
			"tensorflow.mode=payload"
	})
	public static class OutputInPayloadTests extends ObjectDetectionTensorflowProcessorIntegrationTests {

		@Test
		public void testPayloadMode() throws IOException, JSONException {
			try (InputStream is = new ClassPathResource("/images/panda.jpeg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);

				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<String> received = (Message<String>) messageCollector.forChannel(channels.output()).poll();

				JSONArray expected = new JSONArray(JsonUtils.resourceToString("classpath:/test-panda.json"));
				JSONAssert.assertEquals(expected, new JSONArray(received.getPayload()), false);
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(ObjectDetectionProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
