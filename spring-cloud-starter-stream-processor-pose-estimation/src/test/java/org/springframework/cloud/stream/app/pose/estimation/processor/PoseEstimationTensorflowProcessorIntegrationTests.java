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

				JSONArray expected = new JSONArray("[{\"limbs\":[{\"score\":7.7624025,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":232},\"to\":{\"type\":\"lShoulder\",\"y\":40,\"x\":248}},{\"score\":9.036457,\"from\":{\"type\":\"rElbow\",\"y\":88,\"x\":216},\"to\":{\"type\":\"rWist\",\"y\":120,\"x\":200}},{\"score\":9.494497,\"from\":{\"type\":\"rShoulder\",\"y\":48,\"x\":224},\"to\":{\"type\":\"rElbow\",\"y\":88,\"x\":216}},{\"score\":7.8425403,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":232},\"to\":{\"type\":\"lHip\",\"y\":120,\"x\":240}},{\"score\":7.7026844,\"from\":{\"type\":\"rHip\",\"y\":120,\"x\":224},\"to\":{\"type\":\"rKnee\",\"y\":176,\"x\":240}},{\"score\":7.144474,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":232},\"to\":{\"type\":\"rHip\",\"y\":120,\"x\":224}},{\"score\":9.494364,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":232},\"to\":{\"type\":\"nose\",\"y\":16,\"x\":208}},{\"score\":9.4346695,\"from\":{\"type\":\"lKnee\",\"y\":176,\"x\":232},\"to\":{\"type\":\"lAnkle\",\"y\":232,\"x\":248}},{\"score\":9.273419,\"from\":{\"type\":\"lElbow\",\"y\":80,\"x\":264},\"to\":{\"type\":\"lWrist\",\"y\":120,\"x\":256}},{\"score\":9.831003,\"from\":{\"type\":\"lShoulder\",\"y\":40,\"x\":248},\"to\":{\"type\":\"lElbow\",\"y\":80,\"x\":264}},{\"score\":7.4351315,\"from\":{\"type\":\"rKnee\",\"y\":176,\"x\":240},\"to\":{\"type\":\"rAnkle\",\"y\":232,\"x\":240}},{\"score\":9.134686,\"from\":{\"type\":\"lShoulder\",\"y\":40,\"x\":248},\"to\":{\"type\":\"lEar\",\"y\":16,\"x\":224}},{\"score\":8.6209545,\"from\":{\"type\":\"lEye\",\"y\":16,\"x\":216},\"to\":{\"type\":\"lEar\",\"y\":16,\"x\":224}},{\"score\":8.222714,\"from\":{\"type\":\"lHip\",\"y\":120,\"x\":240},\"to\":{\"type\":\"lKnee\",\"y\":176,\"x\":232}},{\"score\":9.501558,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":232},\"to\":{\"type\":\"rShoulder\",\"y\":48,\"x\":224}}]}," +
						"{\"limbs\":[{\"score\":7.729192,\"from\":{\"type\":\"rShoulder\",\"y\":56,\"x\":40},\"to\":{\"type\":\"rEar\",\"y\":24,\"x\":56}},{\"score\":9.524682,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":56},\"to\":{\"type\":\"lShoulder\",\"y\":56,\"x\":64}},{\"score\":9.536926,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":56},\"to\":{\"type\":\"rHip\",\"y\":120,\"x\":56}},{\"score\":9.0363655,\"from\":{\"type\":\"rEye\",\"y\":24,\"x\":64},\"to\":{\"type\":\"rEar\",\"y\":24,\"x\":56}},{\"score\":6.972417,\"from\":{\"type\":\"nose\",\"y\":24,\"x\":72},\"to\":{\"type\":\"rEye\",\"y\":24,\"x\":64}},{\"score\":9.020044,\"from\":{\"type\":\"rShoulder\",\"y\":56,\"x\":40},\"to\":{\"type\":\"rElbow\",\"y\":96,\"x\":40}},{\"score\":9.381481,\"from\":{\"type\":\"lKnee\",\"y\":176,\"x\":48},\"to\":{\"type\":\"lAnkle\",\"y\":232,\"x\":40}},{\"score\":8.968785,\"from\":{\"type\":\"rHip\",\"y\":120,\"x\":56},\"to\":{\"type\":\"rKnee\",\"y\":184,\"x\":64}},{\"score\":8.532742,\"from\":{\"type\":\"lShoulder\",\"y\":56,\"x\":64},\"to\":{\"type\":\"lElbow\",\"y\":88,\"x\":64}},{\"score\":9.869107,\"from\":{\"type\":\"rKnee\",\"y\":184,\"x\":64},\"to\":{\"type\":\"rAnkle\",\"y\":240,\"x\":72}},{\"score\":8.945595,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":56},\"to\":{\"type\":\"nose\",\"y\":24,\"x\":72}},{\"score\":9.050734,\"from\":{\"type\":\"rElbow\",\"y\":96,\"x\":40},\"to\":{\"type\":\"rWist\",\"y\":120,\"x\":56}},{\"score\":7.1464586,\"from\":{\"type\":\"lHip\",\"y\":120,\"x\":64},\"to\":{\"type\":\"lKnee\",\"y\":176,\"x\":48}},{\"score\":8.99887,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":56},\"to\":{\"type\":\"lHip\",\"y\":120,\"x\":64}},{\"score\":9.227391,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":56},\"to\":{\"type\":\"rShoulder\",\"y\":56,\"x\":40}},{\"score\":9.684619,\"from\":{\"type\":\"lElbow\",\"y\":88,\"x\":64},\"to\":{\"type\":\"lWrist\",\"y\":112,\"x\":88}}]}," +
						"{\"limbs\":[{\"score\":9.970467,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":144},\"to\":{\"type\":\"lShoulder\",\"y\":56,\"x\":160}},{\"score\":8.0971155,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":144},\"to\":{\"type\":\"rHip\",\"y\":128,\"x\":128}},{\"score\":9.048936,\"from\":{\"type\":\"rHip\",\"y\":128,\"x\":128},\"to\":{\"type\":\"rKnee\",\"y\":184,\"x\":136}},{\"score\":7.9185815,\"from\":{\"type\":\"rShoulder\",\"y\":56,\"x\":128},\"to\":{\"type\":\"rElbow\",\"y\":96,\"x\":120}},{\"score\":6.4126267,\"from\":{\"type\":\"rElbow\",\"y\":96,\"x\":120},\"to\":{\"type\":\"rWist\",\"y\":128,\"x\":104}},{\"score\":9.0444565,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":144},\"to\":{\"type\":\"lHip\",\"y\":128,\"x\":152}},{\"score\":8.031553,\"from\":{\"type\":\"lKnee\",\"y\":184,\"x\":152},\"to\":{\"type\":\"lAnkle\",\"y\":240,\"x\":144}},{\"score\":8.4396105,\"from\":{\"type\":\"lShoulder\",\"y\":56,\"x\":160},\"to\":{\"type\":\"lEar\",\"y\":24,\"x\":152}},{\"score\":10.328954,\"from\":{\"type\":\"rKnee\",\"y\":184,\"x\":136},\"to\":{\"type\":\"rAnkle\",\"y\":224,\"x\":176}},{\"score\":5.3274198,\"from\":{\"type\":\"nose\",\"y\":24,\"x\":128},\"to\":{\"type\":\"lEye\",\"y\":24,\"x\":136}},{\"score\":8.678178,\"from\":{\"type\":\"lShoulder\",\"y\":56,\"x\":160},\"to\":{\"type\":\"lElbow\",\"y\":96,\"x\":160}},{\"score\":8.60636,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":144},\"to\":{\"type\":\"nose\",\"y\":24,\"x\":128}},{\"score\":9.745945,\"from\":{\"type\":\"lHip\",\"y\":128,\"x\":152},\"to\":{\"type\":\"lKnee\",\"y\":184,\"x\":152}},{\"score\":10.145516,\"from\":{\"type\":\"neck\",\"y\":56,\"x\":144},\"to\":{\"type\":\"rShoulder\",\"y\":56,\"x\":128}},{\"score\":7.646615,\"from\":{\"type\":\"lEye\",\"y\":24,\"x\":136},\"to\":{\"type\":\"lEar\",\"y\":24,\"x\":152}},{\"score\":8.218666,\"from\":{\"type\":\"lElbow\",\"y\":96,\"x\":160},\"to\":{\"type\":\"lWrist\",\"y\":128,\"x\":168}}]}," +
						"{\"limbs\":[{\"score\":6.4001913,\"from\":{\"type\":\"nose\",\"y\":24,\"x\":416},\"to\":{\"type\":\"rEye\",\"y\":16,\"x\":408}},{\"score\":8.033572,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":432},\"to\":{\"type\":\"rHip\",\"y\":120,\"x\":424}},{\"score\":8.621042,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":432},\"to\":{\"type\":\"lHip\",\"y\":120,\"x\":432}},{\"score\":5.836733,\"from\":{\"type\":\"rShoulder\",\"y\":48,\"x\":424},\"to\":{\"type\":\"rElbow\",\"y\":88,\"x\":424}},{\"score\":9.344168,\"from\":{\"type\":\"rKnee\",\"y\":176,\"x\":432},\"to\":{\"type\":\"rAnkle\",\"y\":232,\"x\":432}},{\"score\":8.949261,\"from\":{\"type\":\"rHip\",\"y\":120,\"x\":424},\"to\":{\"type\":\"rKnee\",\"y\":176,\"x\":432}},{\"score\":8.429034,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":432},\"to\":{\"type\":\"nose\",\"y\":24,\"x\":416}},{\"score\":8.87405,\"from\":{\"type\":\"lHip\",\"y\":120,\"x\":432},\"to\":{\"type\":\"lKnee\",\"y\":176,\"x\":440}},{\"score\":6.4418483,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":432},\"to\":{\"type\":\"rShoulder\",\"y\":48,\"x\":424}},{\"score\":9.649043,\"from\":{\"type\":\"lKnee\",\"y\":176,\"x\":440},\"to\":{\"type\":\"lAnkle\",\"y\":224,\"x\":472}}]}," +
						"{\"limbs\":[{\"score\":8.600379,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":328},\"to\":{\"type\":\"lShoulder\",\"y\":48,\"x\":320}},{\"score\":5.776266,\"from\":{\"type\":\"rShoulder\",\"y\":56,\"x\":344},\"to\":{\"type\":\"rEar\",\"y\":32,\"x\":360}},{\"score\":6.8949876,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":328},\"to\":{\"type\":\"lHip\",\"y\":128,\"x\":304}},{\"score\":9.813445,\"from\":{\"type\":\"rKnee\",\"y\":192,\"x\":336},\"to\":{\"type\":\"rAnkle\",\"y\":248,\"x\":336}},{\"score\":7.878962,\"from\":{\"type\":\"rHip\",\"y\":128,\"x\":328},\"to\":{\"type\":\"rKnee\",\"y\":192,\"x\":336}},{\"score\":7.7970695,\"from\":{\"type\":\"rShoulder\",\"y\":56,\"x\":344},\"to\":{\"type\":\"rElbow\",\"y\":96,\"x\":360}},{\"score\":9.607412,\"from\":{\"type\":\"lHip\",\"y\":128,\"x\":304},\"to\":{\"type\":\"lKnee\",\"y\":184,\"x\":304}},{\"score\":7.85779,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":328},\"to\":{\"type\":\"rHip\",\"y\":128,\"x\":328}},{\"score\":9.062571,\"from\":{\"type\":\"lKnee\",\"y\":184,\"x\":304},\"to\":{\"type\":\"lAnkle\",\"y\":240,\"x\":296}},{\"score\":7.347301,\"from\":{\"type\":\"rElbow\",\"y\":96,\"x\":360},\"to\":{\"type\":\"rWist\",\"y\":104,\"x\":400}},{\"score\":8.256059,\"from\":{\"type\":\"neck\",\"y\":48,\"x\":328},\"to\":{\"type\":\"rShoulder\",\"y\":56,\"x\":344}}]}]\n");

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
