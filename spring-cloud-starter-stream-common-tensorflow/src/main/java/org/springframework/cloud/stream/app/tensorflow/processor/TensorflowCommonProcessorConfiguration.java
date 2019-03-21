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

package org.springframework.cloud.stream.app.tensorflow.processor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * A processor that evaluates a machine learning model stored in TensorFlow's ProtoBuf format.
 *
 * Processor uses a {@link TensorflowInputConverter} to convert the input data into TensorFlow model input format (called
 * feeds). The input converter converts the input {@link Message} into key/value {@link Map},
 * where the Key corresponds to a model input placeholder (feed) and the content is {@link org.tensorflow.DataType}
 * compliant value. The default converter implementation expects either Map payload.
 *
 * The {@link TensorflowInputConverter} can be extended and customized.
 *
 * Processor's output uses the {@link TensorflowOutputConverter} to convert the computed {@link Tensor} result into a
 * serializable message. The default implementation converts the Tensor result into JSON (see:
 * {@link TensorflowOutputConverter}).
 *
 * The {@link TensorflowOutputConverter} can be extended and customized to provide a convenient data representations,
 * accustomed for a particular model (see TwitterSentimentTensorflowOutputConverter.java)
 *
 * The {@link OutputMessageBuilder} defines how the computed inference score is arranged withing the output message.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@EnableConfigurationProperties(TensorflowCommonProcessorProperties.class)
public class TensorflowCommonProcessorConfiguration {

	private static final Log logger = LogFactory.getLog(TensorflowCommonProcessorConfiguration.class);

	@Autowired
	@Qualifier(IntegrationContextUtils.INTEGRATION_EVALUATION_CONTEXT_BEAN_NAME)
	private EvaluationContext evaluationContext;

	@Autowired
	private TensorflowCommonProcessorProperties properties;

	@Autowired
	private TensorflowInputConverter tensorflowInputConverter;

	@Autowired
	private TensorflowOutputConverter tensorflowOutputConverter;

	@Autowired
	private OutputMessageBuilder tensorflowOutputMessageBuilder;

	@Autowired
	private TensorFlowService tensorFlowService;

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Object evaluate(Message<byte[]> input) {

		Object inputData =
				this.properties.getExpression() == null
						? input.getPayload()
						: this.properties.getExpression().getValue(this.evaluationContext, input, Object.class);

		// The processorContext allows to convey metadata from the Input to Output converter.
		Map<String, Object> processorContext = new ConcurrentHashMap<>();

		Map<String, Object> inputDataMap = this.tensorflowInputConverter.convert(inputData, processorContext);

		Map<String, Tensor<?>> outputTensorMap = this.tensorFlowService.evaluate(inputDataMap, this.properties.getModelFetch());

		Object outputData = tensorflowOutputConverter.convert(outputTensorMap, processorContext);

		return tensorflowOutputMessageBuilder.createOutputMessageBuilder(input, outputData);

	}

	@Bean
	public TensorFlowService tensorFlowService() {
		return new TensorFlowService(this.properties.getModel());
	}
}
