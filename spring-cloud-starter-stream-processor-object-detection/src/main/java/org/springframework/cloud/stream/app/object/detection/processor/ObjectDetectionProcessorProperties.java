/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.cloud.stream.app.object.detection.processor;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * @author Christian Tzolov
 */
@ConfigurationProperties("tensorflow.object.detection")
@Validated
public class ObjectDetectionProcessorProperties {

	/**
	 * The text file containing the category names (e.g. labels) of all categories
	 * that this model is trained to recognize. Every category is on a separate line.
	 */
	private Resource labels;

	/**
	 * Probability threshold. Only objects detected with probability higher then
	 * the confidence threshold are accepted. Value is between 0 and 1.
	 */
	private float confidence = 0.4f;

	/**
	 * When set to true, the output image will be annotated with the detected object boxes
	 */
	private boolean drawBoundingBox = true;


	/**
	 * For models with mask support enable drawing the mask of the detected objects
	 */
	private boolean drawMask = true;

	/**
	 * If disabled (default) the bounding box colors are selected as a function of the object class id.
	 * If enabled all bounding boxes are visualized with a single color.
	 */
	private boolean colorAgnostic = false;

	@NotNull
	public Resource getLabels() {
		return labels;
	}

	public void setLabels(Resource labels) {
		this.labels = labels;
	}

	public float getConfidence() {
		return confidence;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	public boolean isDrawBoundingBox() {
		return drawBoundingBox;
	}

	public void setDrawBoundingBox(boolean drawBoundingBox) {
		this.drawBoundingBox = drawBoundingBox;
	}

	public boolean isDrawMask() {
		return drawMask;
	}

	public void setDrawMask(boolean drawMask) {
		this.drawMask = drawMask;
	}

	public boolean isColorAgnostic() {
		return colorAgnostic;
	}

	public void setColorAgnostic(boolean colorAgnostic) {
		this.colorAgnostic = colorAgnostic;
	}
}
