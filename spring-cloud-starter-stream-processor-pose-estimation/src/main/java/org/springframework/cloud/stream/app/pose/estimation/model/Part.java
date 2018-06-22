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

package org.springframework.cloud.stream.app.pose.estimation.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Christian Tzolov
 */
public class Part {

	private final Model.PartType partType;
	private final int partId;
	private final int y;
	private final int x;
	private final float confidence;

	public Part(Model.PartType partType, int partInstanceId, int y, int x, float score) {
		this.partType = partType;
		this.partId = partInstanceId;
		this.y = y;
		this.x = x;
		this.confidence = score;
	}

	@JsonIgnore
	public float getConfidence() {
		return this.confidence;
	}

	@JsonProperty("type")
	public Model.PartType getPartType() {
		return this.partType;
	}

	@JsonIgnore
	public int getPartId() {
		return this.partId;
	}

	@JsonIgnore
	public int getY() {
		return this.y;
	}

	@JsonIgnore
	public int getX() {
		return this.x;
	}

	@JsonProperty("y")
	public int getNormalizedY() {
		return this.y * 8;
	}

	@JsonProperty("x")
	public int getNormalizedX() {
		return this.x * 8;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Part part = (Part) o;
		return getPartId() == part.getPartId() &&
				getPartType() == part.getPartType();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getPartType(), getPartId());
	}

	@Override
	public String toString() {
		return "Part{" +
				 partType + ":" + partId +
				", x,y=[" + x + ", " + y +
				"], confidence=" + confidence +
				'}';
	}
}
