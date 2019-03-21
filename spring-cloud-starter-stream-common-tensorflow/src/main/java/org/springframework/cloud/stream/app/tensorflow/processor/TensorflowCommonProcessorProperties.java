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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Holds configuration properties for the TensorFlow Processor module.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@ConfigurationProperties("tensorflow")
@Validated
public class TensorflowCommonProcessorProperties {

	/**
	 * The location of the TensorFlow model file.
	 */
	private Resource model;

	/**
	 * The TensorFlow graph model output. Name of TensorFlow operation to fetch the output Tensors from.
	 */
	private String modelFetch;

	/**
	 * The modelFetch returns a list of Tensors. The modelFetchIndex specifies the index in the list to use as an output.
	 */
	private int modelFetchIndex = 0;

	/**
	 * How to obtain the input data from the input message. If empty it defaults to the input message payload.
	 * The payload.myInTupleName expression treats the input payload as a Tuple, and myInTupleName stands for
	 * a Tuple key. The headers[myHeaderName] expression to get input data from message's header using
	 * myHeaderName as a key.
	 */
	private Expression expression;

	/**
	 * Defines how to store the output data and if the input payload is passed through or discarded.
	 * Payload (Default) stores the output data in the outbound message payload. The input payload is discarded.
	 * Header stores the output data in outputName message's header. The the input payload is passed through.
	 * Tuple stores the output data in an Tuple payload, using the outputName key. The input payload is passed through
	 * in the same Tuple using the 'original.input.data'. If the input payload is already a Tuple that contains
	 * a 'original.input.data' key, then copy the input Tuple into the new Tuple to be returned.
	 */
	private OutputMode mode = OutputMode.payload;

	/**
	 * The output data key used in the Header or Tuple modes. Empty name defaults to the modelFetch property value.
	 */
	private String outputName;

	@NotNull
	public String getModelFetch() {
		return this.modelFetch;
	}

	public void setModelFetch(String modelFetch) {
		this.modelFetch = modelFetch;
	}

	@NotNull
	public Resource getModel() {
		return this.model;
	}

	public void setModel(Resource model) {
		this.model = model;
	}

	public int getModelFetchIndex() {
		return this.modelFetchIndex;
	}

	public void setModelFetchIndex(int modelFetchIndex) {
		this.modelFetchIndex = modelFetchIndex;
	}

	public Expression getExpression() {
		return this.expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	@NotNull
	public OutputMode getMode() {
		return this.mode;
	}

	public void setMode(OutputMode mode) {
		this.mode = mode;
	}

	public String getOutputName() {
		return StringUtils.isEmpty(this.outputName) ? getModelFetch() : this.outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

}
