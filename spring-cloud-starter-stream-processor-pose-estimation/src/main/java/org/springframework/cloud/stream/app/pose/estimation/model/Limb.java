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
public class Limb {

	private final Model.LimbType limbType;
	private final float pafScore;
	private final Part fromPart;
	private final Part toPart;

	public Limb(Model.LimbType limbType, float pafScore, Part fromPartInstance, Part toPartInstance) {
		this.limbType = limbType;
		this.pafScore = pafScore;
		this.fromPart = fromPartInstance;
		this.toPart = toPartInstance;
	}

	@JsonProperty("score")
	public float getPafScore() {
		return this.pafScore;
	}

	@JsonProperty("from")
	public Part getFromPart() {
		return this.fromPart;
	}

	@JsonProperty("to")
	public Part getToPart() {
		return this.toPart;
	}

	@JsonIgnore
	//@JsonProperty("type")
	public Model.LimbType getLimbType() {
		return this.limbType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Limb that = (Limb) o;
		return getLimbType() == that.getLimbType() &&
				Objects.equals(getFromPart(), that.getFromPart()) &&
				Objects.equals(getToPart(), that.getToPart());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getLimbType(), getFromPart(), getToPart());
	}

	@Override
	public String toString() {
		return "Limb{" + limbType +
				", score=" + pafScore +
				", from=" + fromPart +
				", to=" + toPart +
				'}';
	}
}
