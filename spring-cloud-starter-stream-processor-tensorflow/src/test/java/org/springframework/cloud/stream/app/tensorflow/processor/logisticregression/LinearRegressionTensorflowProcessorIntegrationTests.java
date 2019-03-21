/*
 * Copyright 2017-2018 the original author or authors.
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

package org.springframework.cloud.stream.app.tensorflow.processor.logisticregression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.Tensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorJsonConverter;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowOutputConverter;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowProcessorConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Integration Tests for TensorflowProcessor.
 *
 * Uses the example model from the TensorFlow Core tutorial (https://www.tensorflow.org/get_started/get_started#complete_program)
 * The model is exported as protobuf file (linear_regression_graph.proto) using this snipped
 * <code>
 *   RUN_DIR = os.path.abspath(os.path.curdir)
 *   minimal_graph = convert_variables_to_constants(sess, sess.graph_def, ['add'])
 *   tf.train.write_graph(minimal_graph, RUN_DIR, 'linear_regression_graph.proto', as_text=False)
 *   tf.train.write_graph(minimal_graph, RUN_DIR, 'linear_regression.txt', as_text=True)
 * </code>
 *
 * The linear_regression.txt provides detail graph model description.
 *
 * The computational graph ('add' = W * 'Placeholder' + b) takes an input (called 'Placeholder' of type FLOAT)
 * and computes output (called 'add' of type FLOAT). The 'W' and 'b' are the trained variables.
 *
 * @author Christian Tzolov
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {
				"tensorflow.model=classpath:tensorflow/model/linear_regression_graph.proto",
				"tensorflow.modelFetch=add"
		}
)
@DirtiesContext
public abstract class LinearRegressionTensorflowProcessorIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Test
	public void testEvaluationFloatInput() throws InterruptedException {
		testEvaluation(0.7f);
	}

	@Test
	public void testEvaluationWithTensorInput() throws InterruptedException {
		testEvaluation(Tensor.create(0.7f));
	}

	@Test
	public void testEvaluationWithJsonInput() throws InterruptedException {
		testEvaluation(TensorJsonConverter.toJson(Tensor.create(0.7f)));
	}

	@Test(expected = org.springframework.messaging.MessagingException.class)
	public void testEvaluationIncorrectJsonInput() throws InterruptedException {
		String incompleteJson = String.format("{ \"shape\": %s, \"value\": \"%s\" }", Arrays.toString(new long[0]), "value");
		testEvaluation(incompleteJson);
	}

	protected abstract void testEvaluation(Object input) throws InterruptedException;

	public static class DefaultInputDefaultOutput extends LinearRegressionTensorflowProcessorIntegrationTests {

		@Override
		protected void testEvaluation(Object input) throws InterruptedException {

			Map<String, Object> inMap = new HashMap<>();
			inMap.put("Placeholder", input);

			Message<?> inputMessage = MessageBuilder
					.withPayload(inMap)
					.setHeader("passthrough", "passthrough")
					.build();

			channels.input().send(inputMessage);

			Message<?> outputMessage = messageCollector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);

			assertThat(outputMessage.getHeaders().get("passthrough"), equalTo("passthrough"));

			assertThat(outputMessage.getPayload(), equalTo("0.29999298"));
		}
	}

	@TestPropertySource(properties = {
			"tensorflow.expression=headers[myInputData]",
			"tensorflow.mode=payload"
	})
	public static class HeaderInputPayloadOutput extends LinearRegressionTensorflowProcessorIntegrationTests {
		@Override
		protected void testEvaluation(Object input) throws InterruptedException {

			Map<String, Object> inMap = new HashMap<>();
			inMap.put("Placeholder", input);

			Message<?> inputMessage = MessageBuilder
					.withPayload("Dummy Payload")
					.setHeader("myInputData", inMap)
					.setHeader("passthrough", "passthrough")
					.build();

			channels.input().send(inputMessage);

			Message<?> outputMessage = messageCollector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);

			assertThat(outputMessage.getHeaders().get("passthrough"), equalTo("passthrough"));

			assertThat(outputMessage.getPayload(), equalTo("0.29999298"));
		}
	}

	@TestPropertySource(properties = {
			"tensorflow.mode=header"
	})
	public static class DefaultInputHeaderOutput extends LinearRegressionTensorflowProcessorIntegrationTests {

		@Override
		protected void testEvaluation(Object input) throws InterruptedException {

			if (input instanceof Tensor) {
				return; // Tensor input for Header mode is not valid.
			}

			Map<String, Object> inMap = new HashMap<>();
			inMap.put("Placeholder", input);
			Message<?> inputMessage = MessageBuilder
					.withPayload(inMap)
					.setHeader("passthrough", "passthrough")
					.build();

			channels.input().send(inputMessage);

			Message<?> outputMessage = messageCollector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);

			assertThat("Inference result must be stored in the header[myheader]",
					outputMessage.getHeaders().get("result"), equalTo(0.29999298f));

			assertThat(outputMessage.getHeaders().get("passthrough"), equalTo("passthrough"));
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(TensorflowProcessorConfiguration.class)
	public static class LogisticRegressionTensorflowProcessorApplication {

		@Bean
		@Primary
		public TensorflowOutputConverter tensorflowOutputConverterX() {
			return (TensorflowOutputConverter<Object>) (tensorMap, processorContext) -> {
				Tensor tensor = tensorMap.entrySet().iterator().next().getValue();
				float[] outputValue = new float[1];
				tensor.copyTo(outputValue);
				return outputValue[0];
			};
		}

	}

}
