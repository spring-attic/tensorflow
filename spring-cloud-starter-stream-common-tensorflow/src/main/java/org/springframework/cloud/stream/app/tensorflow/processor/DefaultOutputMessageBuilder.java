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
package org.springframework.cloud.stream.app.tensorflow.processor;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;
import org.springframework.util.MimeTypeUtils;

/**
 * The {@link #outputMode} determines how the computed inference score is stored in the output Message.
 *
 * By default ({@link OutputMode#payload}) the inference score is stored directly within the output Message payload.
 *
 * The {@link OutputMode#header} stores the inference score inside a Message header with name {@link #outputName}.
 * In this case the output message payload copies the inbound message payload.
 *
 * The {@link OutputMode#tuple} uses a {@link Tuple} to store the inference score along with input message payload.
 * If the payload is already a tuple that contains an ORIGINAL_INPUT_DATA entry then copy the content of the input tuple
 * in the new tuple to be returned.
 *
 * @author Christian Tzolov
 */
public class DefaultOutputMessageBuilder implements OutputMessageBuilder {

	public static final String ORIGINAL_INPUT_DATA = "original.input.data";
	public static final String APPLICATION_X_SPRING_TUPLE = "application/x-spring-tuple";

	private OutputMode outputMode;

	private final String outputName;

	public DefaultOutputMessageBuilder(TensorflowCommonProcessorProperties properties) {
		this.outputMode = properties.getMode();
		this.outputName = properties.getOutputName();
	}

	@Override
	public MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore) {
		switch (this.outputMode) {

		case tuple:
			TupleBuilder outTupleBuilder = TupleBuilder.tuple().put(this.outputName, computedScore);

			Object inputPayload = inputMessage.getPayload();

			if (inputPayload instanceof Tuple && ((Tuple) inputPayload).hasFieldName(ORIGINAL_INPUT_DATA)) {
				// If the payload is already a tuple that contains ORIGINAL_INPUT_DATA entry then copy the
				// content of the input tuple in the new tuple to be returned.
				outTupleBuilder.putAll((Tuple) inputPayload);
			}
			else {
				// This is a new tuple so preserve the input data.
				outTupleBuilder.put(ORIGINAL_INPUT_DATA, inputPayload);
			}

			return MessageBuilder
					.withPayload(outTupleBuilder.build())
					.setHeader(MessageHeaders.CONTENT_TYPE, APPLICATION_X_SPRING_TUPLE);

		case header:
			MessageBuilder<?> builder = MessageBuilder
					.withPayload(inputMessage.getPayload())
					.setHeader(this.outputName, computedScore);
			if (inputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE) != null) {
				builder.setHeader(MessageHeaders.CONTENT_TYPE, inputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE));
			}
			return builder;

		default: // payload mode
			return MessageBuilder
					.withPayload(computedScore)
					.setHeader(MessageHeaders.CONTENT_TYPE, findContentType(computedScore));
		}

	}

	private String findContentType(Object computedScore) {
		if (computedScore instanceof Tuple) {
			return APPLICATION_X_SPRING_TUPLE;
		}
		else if (computedScore instanceof String) {
			return MimeTypeUtils.APPLICATION_JSON_VALUE;
		}

		// default
		return MimeTypeUtils.APPLICATION_JSON_VALUE;
	}
}
