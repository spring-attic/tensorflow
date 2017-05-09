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
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;

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
 * serializable message. The default implementation converts the Tensor result into {@link Tuple} triple (see:
 * {@link TensorflowOutputConverter}).
 *
 * The {@link TensorflowOutputConverter} can be extended and customized to provide a convenient data representations,
 * accustomed for a particular model (see TwitterSentimentTensorflowOutputConverter.java)
 *
 * By default the inference result is returned in the outbound Message payload. If the saveResultInHeader property is
 * set to true then the inference result would be stored in the outbound Message header by name as set by
 * the getResultHeader property. In this case the message payload is the same like the inbound message payload.
 *
 * @author Christian Tzolov
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TensorflowProcessorProperties.class)
public class TensorflowProcessorConfiguration implements AutoCloseable {

	public static final String ORIGINAL_INPUT_DATA = "original.input.data";

	private static final Log logger = LogFactory.getLog(TensorflowProcessorConfiguration.class);

	@Autowired
	@Qualifier(IntegrationContextUtils.INTEGRATION_EVALUATION_CONTEXT_BEAN_NAME)
	private EvaluationContext evaluationContext;

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
	public Object evaluate(Message<?> input) {

		Object inputData = properties.getExpression() == null?
				input.getPayload() : properties.getExpression().getValue(evaluationContext, input, Object.class);

		// The processorContext allows to convey metadata from the Input to Output converter.
		Map<String, Object> processorContext = new ConcurrentHashMap<>();

		Map<String, Object> inputDataMap = tensorflowInputConverter.convert(inputData, processorContext);

		Tensor outputTensor = tensorFlowService.evaluate(
				inputDataMap, properties.getModelFetchName(), properties.getModelFetchIndex());

		Object outputData = tensorflowOutputConverter.convert(outputTensor, processorContext);

		if (properties.getMode() == OutputMode.payload) {
			//Payload
			return outputData;
		} else if (properties.getMode() == OutputMode.tuple) {

			//Tuple
			TupleBuilder outTupleBuilder = TupleBuilder.tuple().put(properties.getOutputName(), outputData);

			Object payload = input.getPayload();

			if (payload instanceof Tuple && ((Tuple) payload).hasFieldName(ORIGINAL_INPUT_DATA)) {
				// If the payload is already a tuple that contains ORIGINAL_INPUT_DATA entry then copy the
				// content of the existing tuple in the new tuple to be returned.
				outTupleBuilder.putAll((Tuple) payload);
			}
			else {
				// This is a new tuple so preserve the input data.
				outTupleBuilder.put(ORIGINAL_INPUT_DATA, payload);
			}

			return outTupleBuilder.build();
		}

		//Headers
		return MessageBuilder
				.withPayload(input.getPayload())
				.setHeader(properties.getOutputName(), outputData);
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
			public Map<String, Object> convert(Object input, Map<String, Object> processorContext) {

				if (input instanceof Map) {
					return (Map<String, Object>) input;
				}

				throw new MessageConversionException("Unsupported input format: " + input);
			}
		};
	}

	@Override
	public void close() throws Exception {
		logger.info("Close TensorflowProcessorConfiguration");
		tensorFlowService.close();
	}
}
