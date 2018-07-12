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

package org.springframework.cloud.stream.app.pose.estimation.processor;

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
				"tensorflow.modelFetch=Openpose/concat_stage7",
				"tensorflow.model=http://dl.bintray.com/big-data/generic/2018-05-14-cmu-graph_opt.pb"
		})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PoseEstimationTensorflowProcessorIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@TestPropertySource(properties = {
			"tensorflow.mode=payload",
			"tensorflow.pose.estimation.minBodyPartCount=5",
			"tensorflow.pose.estimation.totalPafScoreThreshold=4.4",
			//"tensorflow.model=http://dl.bintray.com/big-data/generic/2018-30-05-mobilenet_thin_graph_opt.pb"
			//"tensorflow.model=file:/Users/ctzolov/Dev/projects/tf-pose-estimation/models/graph/cmu/graph_opt.pb"

	})
	public static class OutputInPayloadTests extends PoseEstimationTensorflowProcessorIntegrationTests {

		@Test
		public void testEvaluationPositive() throws IOException, JSONException {
			try (InputStream is = new ClassPathResource("/images/tourists.jpg").getInputStream()) {

				byte[] image = StreamUtils.copyToByteArray(is);


				channels.input().send(MessageBuilder.withPayload(image).build());

				Message<String> received = (Message<String>) messageCollector.forChannel(channels.output()).poll();

				JSONArray expected = new JSONArray(JsonUtils.resourceToString("classpath:/pose-tourists.json"));
				JSONAssert.assertEquals(expected, new JSONArray(received.getPayload()), false);
			}
		}

		@TestPropertySource(properties = {
				"tensorflow.mode=header",
				//"tensorflow.model=file:/Users/ctzolov/Dev/projects/tf-pose-estimation/models/graph/mobilenet_thin/graph_opt.pb",
				"tensorflow.model=file:/Users/ctzolov/Dev/projects/tf-pose-estimation/models/graph/cmu/graph_opt.pb",

				"tensorflow.pose.estimation.debugVisualisationEnabled=true",
				"tensorflow.pose.estimation.minBodyPartCount=5",
				"tensorflow.pose.estimation.totalPafScoreThreshold=5.4",
				"tensorflow.pose.estimation.drawPartRadius=6",
				"tensorflow.pose.estimation.drawLineWidth=6",
				"tensorflow.pose.estimation.bodyDrawingColorSchema=limbType",
		})
		public static class OutputInHeaderTests extends PoseEstimationTensorflowProcessorIntegrationTests {

			@Test
			public void testEvaluationPositive() throws IOException {
				try (InputStream is = new ClassPathResource("/images/s1p2017_panel.jpg").getInputStream()) {

					channels.input().send(
							MessageBuilder.withPayload(StreamUtils.copyToByteArray(is)).build());

					Message<byte[]> received = (Message<byte[]>) messageCollector.forChannel(channels.output()).poll();

					IOUtils.write(received.getPayload(), new FileOutputStream("./target/out2.jpg"));
				}
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(PoseEstimationProcessorConfiguration.class)
	public static class TensorflowProcessorApplication {

	}

}
