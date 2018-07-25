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

package org.springframework.cloud.stream.app.pose.estimation.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tensorflow.Tensor;

import org.springframework.cloud.stream.app.pose.estimation.model.Body;
import org.springframework.cloud.stream.app.pose.estimation.model.Limb;
import org.springframework.cloud.stream.app.pose.estimation.model.Model;
import org.springframework.cloud.stream.app.pose.estimation.model.Part;
import org.springframework.cloud.stream.app.tensorflow.processor.TensorflowOutputConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Credits:
 *  - https://arvrjourney.com/human-pose-estimation-using-openpose-with-tensorflow-part-2-e78ab9104fc8
 *
 * @author Christian Tzolov
 */
public class PoseEstimationTensorflowOutputConverter implements TensorflowOutputConverter<List<Body>> {

	private static final Log logger = LogFactory.getLog(PoseEstimationTensorflowOutputConverter.class);
	private final String modelFetchOutput;
	private PoseEstimationProcessorProperties poseProperties;

	public PoseEstimationTensorflowOutputConverter(PoseEstimationProcessorProperties poseEstimationProcessorProperties,
			List<String> modelFetch) {
		Assert.isTrue(modelFetch.size() == 1, "A single model output is supported");
		this.modelFetchOutput = modelFetch.get(0);
		logger.info("Pose Estimation model fetch output: " + this.modelFetchOutput);
		this.poseProperties = poseEstimationProcessorProperties;
		logger.info("Pose Estimation properties: " + this.poseProperties);
	}

	@Override
	public List<Body> convert(Map<String, Tensor<?>> tensorMap, Map<String, Object> processorContext) {

		try (Tensor<Float> openPoseOutputTensor = tensorMap.get(this.modelFetchOutput).expect(Float.class)) {

			int height = (int) openPoseOutputTensor.shape()[1]; // = input image's height / 8;
			int width = (int) openPoseOutputTensor.shape()[2]; //  = input image's width / 8;
			int heatmapPafmapCount = (int) openPoseOutputTensor.shape()[3]; // HeatMapCount + PafMapCount = 57 layers

			Assert.isTrue(heatmapPafmapCount == 57, "Incorrect number of output tensor layer");

			// [H] [W] [Heat + PAF]
			float[][][] tensorData = openPoseOutputTensor.copyTo(new float[1][height][width][heatmapPafmapCount])[0];

			if (poseProperties.isDebugVisualisationEnabled()) {
				byte[] inputImage = (byte[]) processorContext.get("inputImage");
				DebugVisualizationUtility.visualizeAllPafHeatMapChannels(inputImage, tensorData,
						poseProperties.getDebugVisualizationOutputPath() + "/PosePartHeatMap.jpg");
				DebugVisualizationUtility.visualizeAllPafChannels(inputImage, tensorData,
						poseProperties.getDebugVisualizationOutputPath() + "/PosePafField.jpg");
			}

			// -------------------------------------------------------------------------------------------------------
			// 1. Select the Part instances with higher confidence
			// -------------------------------------------------------------------------------------------------------

			// Perform non-maximum suppression on the detection confidence (e.g. heatmap) maps to obtain a discrete
			// set of part candidate locations. For each part, several candidates could appear, due to multiple
			// people in the image or false positives.

			Map<Model.PartType, List<Part>> parts = new HashMap<>();

			for (Model.PartType partType : Model.Body.getPartTypes()) {
				List<Part> partsPerType = findHighConfidenceParts(partType, height, width, tensorData);
				parts.put(partType, partsPerType);
			}
			if (poseProperties.isDebugVisualisationEnabled()) {
				DebugVisualizationUtility.visualizePartCandidates((byte[]) processorContext.get("inputImage"),
						parts, poseProperties.getDebugVisualizationOutputPath() + "/PosePartCandidates.jpg");
			}
			// -------------------------------------------------------------------------------------------------------
			// 2. Connect the selected Parts into Limbs
			// -------------------------------------------------------------------------------------------------------

			// Part candidates define a large set of possible limbs. Score each candidate Limb using the line integral
			// computation on the PAF.

			Map<Model.LimbType, List<Limb>> limbs = new HashMap<>();

			// For every Limb Type, retrieve the "from" and "to" Part Types. Retrieve all part Part instances for
			// those types and find the relationships (e.g. limbs) between them.
			for (Model.LimbType limbType : Model.Body.getLimbTypes()) {

				// Limb's "formPartType" Parts candidates.
				List<Part> fromParts = parts.get(limbType.getFromPartType());

				// Limb's "toPartType" Parts candidates.
				List<Part> toParts = parts.get(limbType.getToPartType());

				if (!CollectionUtils.isEmpty(fromParts) && !CollectionUtils.isEmpty(toParts)) {
					// Candidates are sorted by PAF score.
					PriorityQueue<Limb> limbCandidatesQueue = findLimbCandidates(limbType, fromParts, toParts, tensorData);

					// Determine the final Limb instances
					limbs.put(limbType, selectFinalLimbs(limbType, limbCandidatesQueue));
				}
			}

			if (poseProperties.isDebugVisualisationEnabled()) {
				DebugVisualizationUtility.visualizeLimbCandidates((byte[]) processorContext.get("inputImage"),
						limbs, poseProperties.getDebugVisualizationOutputPath() + "/LimbCandidates.jpg");
			}

			// ---------------------------------------------------------------
			// 3. Assembles the selected Limbs and Parts into Bodies
			// ---------------------------------------------------------------

			List<Body> bodies = assembleBodies(limbs);

			return bodies;
		}
	}

	/**
	 * For a {@link org.springframework.cloud.stream.app.pose.estimation.model.Model.PartType} identifies the
	 * body parts locations that have higher confidence to belong to the requested type.
	 *
	 * The Non-maximum Suppression (NMS) algorithm is used to extract the parts locations out of a heatmap and
	 * to suppress part overlapping.
	 *
	 * @param partType Part Type for which part candidates will e searched
	 * @param height Image height (1/8 of the input image height)
	 * @param width Image width (1/8 of the input image height)
	 * @param outputTensor The output tensor contains contains 18 part confidence maps (e.g. heatmaps).
	 *                        Each confidence map is a 2D representation of the belief that a particular body
	 *                        Part occurs at each pixel location.
	 * @return Returns a list of part candidates for the given Part Type.
	 */
	private List<Part> findHighConfidenceParts(Model.PartType partType, int height, int width,
			float[][][] outputTensor) {

		final int minNmsRadius = -(poseProperties.getNmsWindowSize() - 1) / 2;
		final int maxNmsRadius = (poseProperties.getNmsWindowSize() + 1) / 2;

		List<Part> partsPerType = new ArrayList<>();

		for (int y = Math.abs(minNmsRadius); y < height - maxNmsRadius; y++) {
			for (int x = Math.abs(minNmsRadius); x < width - maxNmsRadius; x++) {
				float maxPartScore = 0;
				for (int stepY = minNmsRadius; stepY < maxNmsRadius; stepY++) {
					for (int stepX = minNmsRadius; stepX < maxNmsRadius; stepX++) {
						maxPartScore = Math.max(maxPartScore, outputTensor[y + stepY][x + stepX][partType.getId()]);
					}
				}
				if (maxPartScore > poseProperties.getNmsThreshold()) {
					if (maxPartScore == outputTensor[y][x][partType.getId()]) {
						// Add another name center to the list (e.g. remember the cell with the higher score)
						partsPerType.add(new Part(partType, partsPerType.size(), y, x, maxPartScore));
					}
				}
			}
		}

		return partsPerType;
	}

	/**
	 *
	 * The Part Affinity Field (PAF) is a 2D vector field for each limb. For each pixel in the area belonging to a
	 * particular limb, a 2D vector encodes the direction that points from one part of the limb to the other.
	 * Each type of limb has a corresponding affinity field joining its two associated body parts.
	 *
	 * @param limbType Limb Type to find limb candidates form.
	 * @param fromParts
	 * @param toParts
	 * @param outputTensor
	 * @return Returns a list of Limb candidates sorted by their total PAF score in a descending order.
	 */
	private PriorityQueue<Limb> findLimbCandidates(Model.LimbType limbType, List<Part> fromParts, List<Part> toParts,
			float[][][] outputTensor) {

		// Use priority queue to keeps the limb instance candidates in descending order.
		int initialSize = (fromParts.size() * toParts.size()) / 2 + 1;
		PriorityQueue<Limb> limbCandidatesQueue = new PriorityQueue<>(initialSize,
				(limb1, limb2) -> {
					if (limb1.getPafScore() == limb2.getPafScore())
						return 0;
					return (limb1.getPafScore() > limb2.getPafScore()) ? -1 : 1;
				});

		// For every {from -> to} pair compute a line integral over the Limb-PAF vector field toward the line
		// connecting both Parts. Computed value is used as a Limb candidate score. The higher the value the
		// higher the chance for connection between those Parts.
		for (Part fromPart : fromParts) {
			for (Part toPart : toParts) {

				float deltaX = toPart.getY() - fromPart.getY();
				float deltaY = toPart.getX() - fromPart.getX();
				float norm = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

				// Skip self-pointing edges (e.g. fromPartInstance == toPartInstance)
				if (norm > 1e-12) {

					float dx = deltaX / norm;
					float dy = deltaY / norm;

					int STEP_PAF = 10;
					float pafScores[] = new float[STEP_PAF];
					int stepPafScoreCount = 0;
					float totalPafScore = 0.0f;
					for (int t = 0; t < STEP_PAF; t++) {
						int tx = (int) ((float) fromPart.getY() + (t * deltaX / STEP_PAF) + 0.5);
						int ty = (int) ((float) fromPart.getX() + (t * deltaY / STEP_PAF) + 0.5);

						float pafScoreX = outputTensor[tx][ty][limbType.getPafIndexX()];
						float pafScoreY = outputTensor[tx][ty][limbType.getPafIndexY()];

						pafScores[t] = (dy * pafScoreX) + (dx * pafScoreY);

						totalPafScore += pafScores[t];

						// Filter out the step PAF scores below a given, pre-defined stepPafScoreThreshold
						if (pafScores[t] > poseProperties.getStepPafScoreThreshold()) {
							stepPafScoreCount++;
						}
					}

					if (totalPafScore > poseProperties.getTotalPafScoreThreshold()
							&& stepPafScoreCount >= poseProperties.getPafCountThreshold()) {
						limbCandidatesQueue.add(
								new Limb(limbType, totalPafScore, fromPart, toPart));
					}
				}
			}
		}

		return limbCandidatesQueue;
	}

	/**
	 * From all possible limb candidates for a given Limb Type, select those that maximize the total PAF score.
	 * The algorithm starts from the limb candidates with higher PAF score. Also the algorithm tracks the parts
	 * already assigned t a final limbs and rejects limb candidates with already assigned parts.
	 *
	 * @param limbType Limb Type for which final limbs a selected.
	 * @param limbCandidatesQueue possible Limb candidates, sorted by total PAF score in a descending order.
	 * @return Returns the final list of Limbs for a given {@link org.springframework.cloud.stream.app.pose.estimation.model.Model.LimbType}
	 */
	private List<Limb> selectFinalLimbs(Model.LimbType limbType, PriorityQueue<Limb> limbCandidatesQueue) {

		List<Limb> finalLimbs = new ArrayList<>();

		// Parts assigned to final limbs.
		Set<Part> assignedParts = new HashSet<>();

		// Start from the candidates with higher PAF score and progress in descending order
		while (!limbCandidatesQueue.isEmpty()) {

			Limb limbCandidate = limbCandidatesQueue.poll();

			Assert.isTrue(limbType == limbCandidate.getLimbType(), "Incorrect Limb Type!");

			// Ignore candidate limbs with parts already assigned a final Limb from earlier iteration.
			if (!assignedParts.contains(limbCandidate.getFromPart())
					&& !assignedParts.contains(limbCandidate.getToPart())) {

				// Make the candidate final.
				finalLimbs.add(limbCandidate);

				// Mark limb's parts as assigned.
				assignedParts.add(limbCandidate.getFromPart());
				assignedParts.add(limbCandidate.getToPart());
			}
		}

		return finalLimbs;
	}

	/**
	 * Grows the body out of it parts
	 * @param limbsMap Limb candidates to connected into bodies
	 * @return Final list of body postures
	 */
	private List<Body> assembleBodies(Map<Model.LimbType, List<Limb>> limbsMap) {

		AtomicInteger bodyId = new AtomicInteger();

		Map<Part, Body> partToBodyIndex = new ConcurrentHashMap<>();

		for (Model.LimbType limbType : limbsMap.keySet()) {
			for (Limb limb : limbsMap.get(limbType)) {

				Body fromBody = partToBodyIndex.get(limb.getFromPart());
				Body toBody = partToBodyIndex.get(limb.getToPart());

				Body bodyCandidate;

				if (fromBody == null && toBody == null) {
					bodyCandidate = new Body(bodyId.getAndIncrement());
				}
				else if (fromBody != null && toBody != null) {
					bodyCandidate = fromBody;
					if (!fromBody.equals(toBody)) {
						bodyCandidate.getLimbs().addAll(toBody.getLimbs());
						bodyCandidate.getParts().addAll(toBody.getParts());
						toBody.getParts().forEach(p -> partToBodyIndex.put(p, bodyCandidate));
					}
				}
				else {
					bodyCandidate = (fromBody != null) ? fromBody : toBody;
				}

				bodyCandidate.addLimb(limb);
				partToBodyIndex.put(limb.getFromPart(), bodyCandidate);
				partToBodyIndex.put(limb.getToPart(), bodyCandidate);
			}
		}

		// Filter out the body duplicates and bodies with too few parts
		List<Body> bodies = partToBodyIndex.values().stream()
				.distinct()
				.filter(body -> body.getParts().size() > poseProperties.getMinBodyPartCount())
				.collect(Collectors.toList());

		return bodies;
	}

}
