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

package org.springframework.cloud.stream.app.pose.estimation.model;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Set;

import org.springframework.util.CollectionUtils;

/**
 * Based on
 *  - https://medium.com/tensorflow/move-mirror-an-ai-experiment-with-pose-estimation-in-the-browser-using-tensorflow-js-2f7b769f9b23
 *  - https://arxiv.org/pdf/1612.06524.pdf
 *
 * @author Christian Tzolov
 */
public class PoseMatcher {

	private static final double DEFAULT_MATCHING_BOUNDING_BOX_SIZE = 200.0;

	private static final int PART_TYPE_COUNT = Model.PartType.values().length;

	private final double matchingBoundingBoxSize;

	public static PoseMatcher instance() {
		return new PoseMatcher(DEFAULT_MATCHING_BOUNDING_BOX_SIZE);
	}

	public static PoseMatcher instnace(double matchingBoundingBoxSize) {
		return new PoseMatcher(matchingBoundingBoxSize);
	}

	private PoseMatcher(double matchingBoundingBoxSize) {
		this.matchingBoundingBoxSize = matchingBoundingBoxSize;
	}

	/**
	 * Matching index is an array comprised of:
	 *  - Part's X and Y coordinates - 2 x |Part Type| length
	 *  - Part's confidence and - 1 x |Part Type|
	 *  - confidence sum for all parts - last 1 element of the array.
	 *
	 * Parts's coordinates are re-computed to be relative to a fixed-size, scaled bounding box surrounding the body. The
	 * coordinates are also L2 normalized.
	 *
	 * @param body for which the matching index is build
	 * @return Returns an array of size 3 * number of body-part-types + 1.
	 */
	public double[] poseMatchIndex(Body body) {

		double[] matchingIndex = new double[3 * PART_TYPE_COUNT + 1];
		// Note: Initialize with zeros to ensure the confidence and coordinates of missing parts is 0.
		Arrays.fill(matchingIndex, 0.0);

		Set<Part> parts = body.getParts();
		// Perform matching if you have at least 1 part
		if (!CollectionUtils.isEmpty(parts)) {

			Rectangle boundingBox = squaredBoundingBox(parts);

			final double scale = this.matchingBoundingBoxSize / boundingBox.getWidth();

			final double[] normSquares = new double[1];

			parts.stream().forEach(part -> {
				int partTypeIndex = part.getPartType().getId();

				// Switch to bounding-box relative coordinates and scale the size to ensure that matched bounding boxes
				// are of equal size.
				double x = (part.getNormalizedX() - boundingBox.getX()) * scale;
				double y = (part.getNormalizedY() - boundingBox.getY()) * scale;

				// X, Y coordinates
				matchingIndex[2 * partTypeIndex] = x;
				matchingIndex[2 * partTypeIndex + 1] = y;

				// Part confidence
				matchingIndex[2 * PART_TYPE_COUNT + partTypeIndex] = part.getConfidence();

				// Confidence sum
				matchingIndex[3 * PART_TYPE_COUNT] = matchingIndex[3 * PART_TYPE_COUNT] + part.getConfidence();

				normSquares[0] = normSquares[0] + x * x + y * y;
			});

			// L2 normalization for coordinates.
			double norm = Math.sqrt(normSquares[0]);
			for (int i = 0; i < 2 * PART_TYPE_COUNT; i++) {
				matchingIndex[i] = matchingIndex[i] / norm;
			}
		}

		return matchingIndex;
	}

	/**
	 * Compute a square bounding box that surrounds the body (defined by its parts). First finds the the exact bounding
	 * rectangle and then use the maximum from the height and width as a size of the surrounding box.
	 * @param parts of the body to compute the bounding box fof.
	 * @return Square bounding box that surrounds the body parts coordinates.
	 */
	public Rectangle squaredBoundingBox(Set<Part> parts) {

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		// Compute a rectangle (minX, minY, maxX, maxY) surrounding the body
		for (Part p : parts) {
			minX = Math.min(minX, p.getNormalizedX());
			minY = Math.min(minY, p.getNormalizedY());
			maxX = Math.max(maxX, p.getNormalizedX());
			maxY = Math.max(maxY, p.getNormalizedY());
		}

		// Expand the rectangle to a squared bounding box with size the max(height, width).
		int bodyWidth = maxX - minX;
		int bodyHeight = maxY - minY;
		int bboxSize = Math.max(bodyWidth, bodyHeight);

		// Compute the top left corner of the squared BBox
		final int bboxTopLeftX = minX - (bboxSize - bodyWidth) / 2;
		final int bboxTopLeftY = minY - (bboxSize - bodyHeight) / 2;

		// Squared Bounding Box surrounding the body
		return new Rectangle(bboxTopLeftX, bboxTopLeftY, bboxSize, bboxSize);
	}

	/**
	 * Computes the distance between the two bodies.
	 *
	 * @param fromBody
	 * @param toBody
	 * @return
	 */
	public double weightedDistance(Body fromBody, Body toBody) {
		return this.weightedDistance(
				this.poseMatchIndex(fromBody),
				this.poseMatchIndex(toBody));
	}

	public double weightedDistance(double[] poseMatchIndex1, double[] poseMatchIndex2) {

		double oneByConfidenceSum = 1 / poseMatchIndex1[3 * PART_TYPE_COUNT];
		double partsDistanceSum = 0.0;

		for (int i = 0; i < 2 * PART_TYPE_COUNT; i++) {
			int confidenceIndex = 2 * PART_TYPE_COUNT + (int) Math.floor(i / 2);
			partsDistanceSum = partsDistanceSum + poseMatchIndex1[confidenceIndex] * Math.abs(poseMatchIndex1[i] - poseMatchIndex2[i]);
		}
		return oneByConfidenceSum * partsDistanceSum;
	}
}
