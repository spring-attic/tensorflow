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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;

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
 * By default the inference result is returned in the outbound Message payload. If the saveResultInHeader property is
 * set to true then the inference result would be stored in the outbound Message header by name as set by
 * the getResultHeader property. In this case the message payload is the same like the inbound message payload.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TensorflowCommonProcessorProperties.class)
@Import(TensorflowCommonProcessorConfiguration.class)
public class TensorflowProcessorConfiguration {

	public static final String ORIGINAL_INPUT_DATA = "original.input.data";

	private static final Log logger = LogFactory.getLog(TensorflowProcessorConfiguration.class);

	@Autowired
	private TensorflowCommonProcessorProperties properties;

	/**
	 * @return a default output message builder
	 */
	@Bean
	@ConditionalOnMissingBean
	public OutputMessageBuilder tensorflowOutputMessageBuilder() {
		return new DefaultOutputMessageBuilder(properties);
	}

	@Bean
	public TensorflowOutputConverter tensorflowOutputConverter() {
		// Default implementations serializes the Tensor into Json
		return (TensorflowOutputConverter<String>) (tensorMap, processorContext) -> {
			Tensor<?> tensor = tensorMap.entrySet().iterator().next().getValue();
			return TensorJsonConverter.toJson(tensor);
		};
	}

	@Bean
	@SuppressWarnings("unchecked")
	public TensorflowInputConverter tensorflowInputConverter() {
		return (input, processorContext) -> {

			if (input instanceof Map) {
				return (Map<String, Object>) input;
			}

			throw new MessageConversionException("Unsupported input format: " + input);
		};
	}
}
