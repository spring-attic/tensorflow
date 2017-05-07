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
	 * The model graph output name
	 */
	private String outputName;

	/**
	 * The model graph output index
	 */
	private int outputIndex = 0;

	/**
	 * Specifies where to obtain the input data from. By default it looks
	 * at the {@link org.springframework.messaging.Message}'s payload.
	 * Instead one can obtain the input value from a payload {@link org.springframework.tuple.Tuple} like this:
	 * 'tensorflow.inputExpression=payload.myInTupleName', where myInTupleName is a Tuple key.
	 * To obtain input date from the message headers use expression like this:
	 * 'tensorflow.inputExpression=headers[myHeaderName]', where is the name of the header that contains the input data.
	 */
	private String inputExpression = "payload";

	@NotNull
	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	@NotNull
	public Resource getModelLocation() {
		return modelLocation;
	}

	public void setModelLocation(Resource modelLocation) {
		this.modelLocation = modelLocation;
	}

	public int getOutputIndex() {
		return outputIndex;
	}

	public void setOutputIndex(int outputIndex) {
		this.outputIndex = outputIndex;
	}

	public String getInputExpression() {
		return inputExpression;
	}

	public void setInputExpression(String inputExpression) {
		this.inputExpression = inputExpression;
	}
}
