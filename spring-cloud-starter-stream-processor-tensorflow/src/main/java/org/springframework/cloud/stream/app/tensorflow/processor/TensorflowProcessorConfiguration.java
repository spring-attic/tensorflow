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

package org.springframework.cloud.stream.app.tensorflow.processor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.tuple.Tuple;

/**
 * A processor that evaluates a machine learning model stored in TensorFlow's ProtoBuf format.
 *
 * Processor uses a {@link TensorflowInputConverter} to convert the input data into data format compliant with the
 * TensorFlow Model used. The input converter converts the input {@link Message} into key/value {@link Map}, where
 * the Key corresponds to a model input placeholder and the content is {@link org.tensorflow.DataType} compliant value.
 * The default converter implementation expects either Map payload or flat json message that can be converted int a Map.
 *
 * The {@link TensorflowInputConverter} can be extended and customized.
 *
 * Processor's output uses {@link TensorflowOutputConverter} to convert the computed {@link Tensor} result into a serializable
 * message. The default implementation uses {@link Tuple} triple (see: {@link TensorflowOutputConverter}).
 *
 * Custom {@link TensorflowOutputConverter} can provide more convenient data representations.
 * (see TwitterSentimentTensorflowOutputConverter.java
 *
 * @author Christian Tzolov
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TensorflowProcessorProperties.class)
public class TensorflowProcessorConfiguration implements AutoCloseable {

	private static final Log logger = LogFactory.getLog(TensorflowProcessorConfiguration.class);

	/**
	 * Header name where the output is stored if the isSaveOutputInHeader is set
	 */
	public static final String TF_OUTPUT_HEADER = "TF_OUTPUT";

	/**
	 * Note: The Kafka binder requires you to withe list the custom headers. Therefore if you set the
	 * saveOutputInHeader to true the you have to start the SCDF server with this property:
	 * <code>
	 *  --spring.cloud.dataflow.applicationProperties.stream.spring.cloud.stream.kafka.binder.headers=TF_OUTPUT,TF_INPUT
	 * </code>
	 */

	/**
	 * Header name where the input is stored.
	 * The default TensorflowInputConverter implementation will use TF_INPUT header if provided it over
	 * the message payload.
	 */
	public static final String TF_INPUT_HEADER = "TF_INPUT";


	@Autowired
	private TensorflowProcessorProperties properties;

	@Autowired
	@Qualifier("tensorflowInputConverter")
	private TensorflowInputConverter tensorflowInputConverter;

	@Autowired
	@Qualifier("tensorflowOutputConverter")
	private TensorflowOutputConverter tensorflowOutputConverter;

	@Autowired
	private TensorFlowService tensorFlowService;

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Message<?> evaluate(Message<?> input) {

		Map<String, Object> processorContext = new ConcurrentHashMap<>();

		Map<String, Object> inputData = tensorflowInputConverter.convert(input, processorContext);

		Tensor outputTensor = tensorFlowService.evaluate(
				inputData, properties.getOutputName(), properties.getOutputIndex());

		Object outputData = tensorflowOutputConverter.convert(outputTensor, processorContext);

		if (properties.isSaveOutputInHeader()) {
			// Add the result to the message header
			return MessageBuilder
					.withPayload(input.getPayload())
					.copyHeadersIfAbsent(input.getHeaders())
					.setHeaderIfAbsent(TF_OUTPUT_HEADER, outputData)
					.build();
		}

		// Add the outputData as part of the message payload
		Message<?> outputMessage = MessageBuilder
				.withPayload(outputData)
				.copyHeadersIfAbsent(input.getHeaders())
				.build();

		return outputMessage;
	}

	@Bean
	@RefreshScope
	public TensorFlowService tensorFlowService() throws IOException {
		return new TensorFlowService(properties.getModelLocation());
	}

	@Bean
	@ConditionalOnMissingBean(name = "tensorflowOutputConverter")
	public TensorflowOutputConverter tensorflowOutputConverter() {
		// Default implementations serializes the Tensor into Tuple
		return new TensorflowOutputConverter<Tuple>() {
			@Override
			public Tuple convert(Tensor tensor, Map<String, Object> processorContext) {
				return TensorTupleConverter.toTuple(tensor);
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(name = "tensorflowInputConverter")
	public TensorflowInputConverter tensorflowInputConverter() {
		return new TensorflowInputConverter() {

			@Override
			public Map<String, Object> convert(Message<?> input, Map<String, Object> processorContext) {

				if (input.getHeaders().containsKey(TF_INPUT_HEADER)) {
					return (Map<String, Object>) input.getHeaders().get(TF_INPUT_HEADER, Map.class);
				}
				else if (input.getPayload() instanceof Map) {
					return (Map<String, Object>) input.getPayload();
				}
				else if (input.getPayload() instanceof Tuple) {

				}
				throw new RuntimeException("Unsupported input format: " + input);

			}
		};
	}

	@Override
	public void close() throws Exception {
		logger.info("Close TensorflowProcessorConfiguration");
		tensorFlowService.close();
	}
}
