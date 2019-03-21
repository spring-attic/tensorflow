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

package org.springframework.cloud.stream.app.pose.estimation.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Christian Tzolov
 */
@ConfigurationProperties("tensorflow.pose.estimation")
@Validated
public class PoseEstimationProcessorProperties {

	public enum BodyDrawingColorSchema {monochrome, bodyInstance, limbType}

	/**
	 * Non-maximum suppression (NMS) distance for Part instances. Two parts suppress each other if they are less than `nmsWindowSize` pixels away.
	 */
	private int nmsWindowSize = 4;

	/**
	 * Only return instance detections that have part score greater or equal to this value.
	 */
	private float nmsThreshold = 0.15f;

	/**
	 * Minimal paf score between two parts to consider them being connected and part of the same limb
	 */
//	private float totalPafScoreThreshold = 0.15f;
	private float totalPafScoreThreshold = 4.4f;

	/**
	 * Minimal paf score between two Parts at individual integration step, to consider the parts connected
	 */
	private float stepPafScoreThreshold = 0.1f;

	/**
	 * Minimum number of integration intervals with paf score above the stepPafScoreThreshold, to consider the parts connected.
	 */
	private int pafCountThreshold = 2;

	/**
	 * Minimum number of parts a body should contain. Body instances with less parts are discarded.
	 */
	private int minBodyPartCount = 5;
	//private int minBodyPartCount = 2;

	/**
	 * When set to true, the output image will be augmented with the computed person skeletons
	 */
	private boolean drawPoses = true;

	/**
	 * When drawPoses is enabled, defines the radius of the oval drawn for each part instance
	 */
	private int drawPartRadius = 4;

	/**
	 * When drawPoses is enabled, defines the line width for drawing the limbs
	 */
	private int drawLineWidth = 2;

	/**
	 * if drawPoses is enabled, drawPartLabels will show the party type ids and description.
	 */
	private boolean drawPartLabels = false;

	/**
	 * When drawPoses is enabled, one can decide to draw all body poses in one color (monochrome), have every
	 * body pose drawn in an unique color (bodyInstance) or use common color schema drawing different limbs.
	 */
	private BodyDrawingColorSchema bodyDrawingColorSchema = BodyDrawingColorSchema.limbType;

	/**
	 * If enabled the inference operation will produce 4 additional debug visualization of the intermediate processing
	 * stages:
	 *  - PartHeatMap - Part heat map as computed by DL
	 *  - PafField - PAF limb field as computed by DL
	 *  - PartCandidates - Part final candidates as computed by the post-processor
	 *  - LimbCandidates - Limb final candidates as computed by the post-processor
	 *
	 *  Note: Do NOT enable this feature in production or in streaming mode!
	 */
	private boolean debugVisualisationEnabled = false;

	/**
	 * Parent directory to save the  debug images produced for the intermediate processing stages
	 */
	private String debugVisualizationOutputPath = "./target";

	public int getNmsWindowSize() {
		return nmsWindowSize;
	}

	public void setNmsWindowSize(int nmsWindowSize) {
		this.nmsWindowSize = nmsWindowSize;
	}

	public float getNmsThreshold() {
		return nmsThreshold;
	}

	public void setNmsThreshold(float nmsThreshold) {
		this.nmsThreshold = nmsThreshold;
	}

	public float getTotalPafScoreThreshold() {
		return totalPafScoreThreshold;
	}

	public void setTotalPafScoreThreshold(float totalPafScoreThreshold) {
		this.totalPafScoreThreshold = totalPafScoreThreshold;
	}

	public int getPafCountThreshold() {
		return pafCountThreshold;
	}

	public void setPafCountThreshold(int pafCountThreshold) {
		this.pafCountThreshold = pafCountThreshold;
	}

	public float getStepPafScoreThreshold() {
		return stepPafScoreThreshold;
	}

	public void setStepPafScoreThreshold(float stepPafScoreThreshold) {
		this.stepPafScoreThreshold = stepPafScoreThreshold;
	}

	public int getMinBodyPartCount() {
		return minBodyPartCount;
	}

	public void setMinBodyPartCount(int minBodyPartCount) {
		this.minBodyPartCount = minBodyPartCount;
	}

	public boolean isDrawPoses() {
		return drawPoses;
	}

	public void setDrawPoses(boolean drawPoses) {
		this.drawPoses = drawPoses;
	}

	public boolean isDebugVisualisationEnabled() {
		return debugVisualisationEnabled;
	}

	public void setDebugVisualisationEnabled(boolean debugVisualisationEnabled) {
		this.debugVisualisationEnabled = debugVisualisationEnabled;
	}

	public String getDebugVisualizationOutputPath() {
		return debugVisualizationOutputPath;
	}

	public void setDebugVisualizationOutputPath(String debugVisualizationOutputPath) {
		this.debugVisualizationOutputPath = debugVisualizationOutputPath;
	}

	public int getDrawPartRadius() {
		return drawPartRadius;
	}

	public void setDrawPartRadius(int drawPartRadius) {
		this.drawPartRadius = drawPartRadius;
	}

	public int getDrawLineWidth() {
		return drawLineWidth;
	}

	public void setDrawLineWidth(int drawLineWidth) {
		this.drawLineWidth = drawLineWidth;
	}

	public BodyDrawingColorSchema getBodyDrawingColorSchema() {
		return bodyDrawingColorSchema;
	}

	public void setBodyDrawingColorSchema(BodyDrawingColorSchema bodyDrawingColorSchema) {
		this.bodyDrawingColorSchema = bodyDrawingColorSchema;
	}

	public boolean isDrawPartLabels() {
		return drawPartLabels;
	}

	public void setDrawPartLabels(boolean drawPartLabels) {
		this.drawPartLabels = drawPartLabels;
	}

	@Override
	public String toString() {
		return "PoseEstimationProcessorProperties{" +
				"nmsWindowSize=" + nmsWindowSize +
				", nmsThreshold=" + nmsThreshold +
				", totalPafScoreThreshold=" + totalPafScoreThreshold +
				", stepPafScoreThreshold=" + stepPafScoreThreshold +
				", pafCountThreshold=" + pafCountThreshold +
				", minBodyPartCount=" + minBodyPartCount +
				", drawPoses=" + drawPoses +
				", drawPartRadius=" + drawPartRadius +
				", drawLineWidth=" + drawLineWidth +
				", drawPartLabels=" + drawPartLabels +
				", bodyDrawingColorSchema=" + bodyDrawingColorSchema +
				", debugVisualisationEnabled=" + debugVisualisationEnabled +
				", debugVisualizationOutputPath='" + debugVisualizationOutputPath + '\'' +
				'}';
	}
}
