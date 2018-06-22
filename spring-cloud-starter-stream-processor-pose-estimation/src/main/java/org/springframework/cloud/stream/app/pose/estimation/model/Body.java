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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single {@link org.springframework.cloud.stream.app.pose.estimation.model.Model.BodyType} instance.
 *
 * @author Christian Tzolov
 */
public class Body {

	private final int bodyId;
	private final Set<Limb> limbs;
	private final Set<Part> parts;

	public Body(int bodyId) {
		this.bodyId = bodyId;
		this.limbs = new HashSet<>();
		this.parts = new HashSet<>();
	}

	@JsonIgnore
	//@JsonProperty("id")
	public int getBodyId() {
		return this.bodyId;
	}

	//@JsonProperty("parts")
	@JsonIgnore
	public Set<Part> getParts() {
		return this.parts;
	}

	@JsonProperty("limbs")
	public Set<Limb> getLimbs() {
		return this.limbs;
	}

	public void addLimb(Limb limb) {
		this.limbs.add(limb);
		this.parts.add(limb.getFromPart());
		this.parts.add(limb.getToPart());
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Body body = (Body) o;
		return getBodyId() == body.getBodyId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBodyId());
	}

	@Override
	public String toString() {
		return "Body{bodyId=" + bodyId + ", limbs=" + limbs + '}';
	}

}
