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

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Holds configuration properties for the Tensorflow Processor module.
 *
 * @author Christian Tzolov
 */
@ConfigurationProperties("tensorflow")
@Validated
public class TensorflowProcessorProperties {

	/**
	 * The location of the Tensorflow model file.
	 */
	private Resource modelLocation;

	/**
	 * The graph model output. Name of TensorFlow operation to fetch the output Tensors from.
	 */
	private String modelFetchName;

	/**
	 * Fetched operation (modelFetchName) returns a list of Tensors. The modelFetchIndex specifies which
	 * Tensor from that list to use as an output.
	 */
	private int modelFetchIndex = 0;

	/**
	 * Specifies where to obtain the input data from. When empty defaults to {@link org.springframework.messaging.Message}'s payload.
	 * To obtain an input from a {@link org.springframework.tuple.Tuple} set:
	 * 'tensorflow.expression=payload.myInTupleName', where myInTupleName is a Tuple key.
	 * To obtain input date from a message header use:
	 * 'tensorflow.expression=headers[myHeaderName]', where is the name of the header that contains the input data.
	 */
	private Expression expression;

	/**
	 * Specifies how the outbound data is carried and whether the input message is mirrored in the output or discarded.
	 *
	 * The {@link OutputMode#payload} mode (default) stores the output data in the outbound message payload. The
	 * input message payload is discarded.
	 *
	 * The {@link OutputMode#header} mode stores the output data in message's header under the
	 * {@link TensorflowProcessorProperties#outputName} name. The the output message payload mirrors the input payload.
	 *
	 * The {@link OutputMode#tuple} mode stores the output data in the payload using a
	 * {@link org.springframework.tuple.Tuple} structure. The output data is stored under the
	 * {@link TensorflowProcessorProperties#outputName} key, while the input payload is passed through under the
	 * {@link TensorflowProcessorConfiguration#ORIGINAL_INPUT_DATA} key.
	 */
	private OutputMode mode = OutputMode.payload;

	/**
	 * Applicable only for the {@link OutputMode#header} and {@link OutputMode#tuple} modes. Sets the output data key
	 * either in the outbound Header or Tuple. If empty it defaults to the modelFetchName property.
	 */
	private String outputName;

	@NotNull
	public String getModelFetchName() {
		return modelFetchName;
	}

	public void setModelFetchName(String modelFetchName) {
		this.modelFetchName = modelFetchName;
	}

	@NotNull
	public Resource getModelLocation() {
		return modelLocation;
	}

	public void setModelLocation(Resource modelLocation) {
		this.modelLocation = modelLocation;
	}

	public int getModelFetchIndex() {
		return modelFetchIndex;
	}

	public void setModelFetchIndex(int modelFetchIndex) {
		this.modelFetchIndex = modelFetchIndex;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public OutputMode getMode() {
		return mode;
	}

	public void setMode(OutputMode mode) {
		this.mode = mode;
	}

	public String getOutputName() {
		return StringUtils.isEmpty(outputName) ? getModelFetchName() : outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
}
