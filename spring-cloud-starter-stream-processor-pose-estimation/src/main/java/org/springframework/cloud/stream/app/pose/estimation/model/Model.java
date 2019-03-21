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

/**
 * Static representation of the COCO pose model. Pre-defined set of body part types and connections between them (e.g. limb types).
 *
 * @author Christian Tzolov
 */
public class Model {

	public static final BodyType Body = new BodyType();

	/**
	 * The body type is just a container for the predefined lists of part and limb types.
	 */
	public static class BodyType {
		private BodyType() {
		}

		public PartType[] getPartTypes() {
			return PartType.values();
		}

		public LimbType[] getLimbTypes() {
			return LimbType.values();
		}
	}

	/**
	 * Implements the COCO model Part type specification.
	 */
	public enum PartType {

		nose(0), neck(1), rShoulder(2), rElbow(3), rWist(4),
		lShoulder(5), lElbow(6), lWrist(7), rHip(8),
		rKnee(9), rAnkle(10), lHip(11), lKnee(12), lAnkle(13),
		rEye(14), lEye(15), rEar(16), lEar(17);

		PartType(int id) {
			this.id = id;
		}

		private final int id;

		public static PartType of(int id) {
			for (PartType pt : PartType.values()) {
				if (pt.getId() == id) {
					return pt;
				}
			}
			throw new IllegalArgumentException("No PartType exist with ID:" + id);
		}

		public int getId() {
			return this.id;
		}
	}

	/**
	 * Implements COCO's model limb type spec as an ordered list of part pairs.
	 */
	public enum LimbType {
		limb0(0, PartType.of(1), PartType.of(2)),
		limb1(1, PartType.of(1), PartType.of(5)),
		limb2(2, PartType.of(2), PartType.of(3)),
		limb3(3, PartType.of(3), PartType.of(4)),
		limb4(4, PartType.of(5), PartType.of(6)),
		limb5(5, PartType.of(6), PartType.of(7)),
		limb6(6, PartType.of(1), PartType.of(8)),
		limb7(7, PartType.of(8), PartType.of(9)),
		limb8(8, PartType.of(9), PartType.of(10)),
		limb9(9, PartType.of(1), PartType.of(11)),
		limb10(10, PartType.of(11), PartType.of(12)),
		limb11(11, PartType.of(12), PartType.of(13)),
		limb12(12, PartType.of(1), PartType.of(0)),
		limb13(13, PartType.of(0), PartType.of(14)),
		limb14(14, PartType.of(14), PartType.of(16)),
		limb15(15, PartType.of(0), PartType.of(15)),
		limb16(16, PartType.of(15), PartType.of(17)),
		limb17(17, PartType.of(2), PartType.of(16)),
		limb18(18, PartType.of(5), PartType.of(17));

		// in the output tensor the limb's paf is stored between layers 20 to 57
		private static final int PAF_OFFSET = 19;

		// 19 x 2
		private static final int[][] PAF_INDEX = { { 12, 13 }, { 20, 21 }, { 14, 15 }, { 16, 17 }, { 22, 23 },
				{ 24, 25 }, { 0, 1 }, { 2, 3 }, { 4, 5 }, { 6, 7 }, { 8, 9 }, { 10, 11 }, { 28, 29 }, { 30, 31 },
				{ 34, 35 }, { 32, 33 }, { 36, 37 }, { 18, 19 }, { 26, 27 } };


		private final int id;
		private final PartType fromPartType;
		private final PartType toPartType;

		LimbType(int id, PartType fromPartType, PartType toPartType) {
			this.id = id;
			this.fromPartType = fromPartType;
			this.toPartType = toPartType;
		}

		public int getId() {
			return id;
		}

		public PartType getFromPartType() {
			return fromPartType;
		}

		public PartType getToPartType() {
			return toPartType;
		}

		public int getPafIndexX() {
			return PAF_OFFSET + PAF_INDEX[this.id][0];
		}

		public int getPafIndexY() {
			return PAF_OFFSET + PAF_INDEX[this.id][1];
		}
	}
}
