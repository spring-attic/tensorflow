/*
 * Copyright 2015-2017 the original author or authors.
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
	 * Controls if the inference result is stored in the {@link org.springframework.messaging.Message} payload
	 * or header.
	 * By default (false) the result is stored in the payload. When set to true, the result will be
	 * stored in the header using the resultHeaderName value.
	 */
	private boolean saveResultInHeader = false;

	/**
	 * Applicable only for `saveResultInHeader=true`, this property holds the header name used to store
	 * the inference result. The default name is `tf_result`.
	 * If multiple tensorflow processors are connected in a single pipeline then make sure they have
	 * distinct resultHeaderName values to avoid overriding.
	 */
	private String resultHeaderName = "tf_result";

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

	public boolean isSaveResultInHeader() {
		return saveResultInHeader;
	}

	public void setSaveResultInHeader(boolean saveResultInHeader) {
		this.saveResultInHeader = saveResultInHeader;
	}

	public String getResultHeaderName() {
		return resultHeaderName;
	}

	public void setResultHeaderName(String resultHeaderName) {
		this.resultHeaderName = resultHeaderName;
	}
}
