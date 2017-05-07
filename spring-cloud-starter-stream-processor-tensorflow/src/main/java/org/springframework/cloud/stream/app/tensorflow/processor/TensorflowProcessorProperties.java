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
	 * Controls if the inference result would be carried in the {@link org.springframework.messaging.Message} payload
	 * or in header.
	 * If the resultHeader is empty (default) the result is carried in the payload. If not empty then the result is
	 * carried in the header using the resultHeader name.
	 *
	 * When multiple tensorflow processors in a single pipeline carry their results in the header make sure they
	 * are configured with distinct resultHeader names.
	 */
	private String resultHeader;

	/**
	 * If not empty, the processor will use the inputHeader content as data input instead of message's payload.
	 */
	private String inputHeader;

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

	public String getResultHeader() {
		return resultHeader;
	}

	public void setResultHeader(String resultHeader) {
		this.resultHeader = resultHeader;
	}

	public String getInputHeader() {
		return inputHeader;
	}

	public void setInputHeader(String inputHeader) {
		this.inputHeader = inputHeader;
	}
}
