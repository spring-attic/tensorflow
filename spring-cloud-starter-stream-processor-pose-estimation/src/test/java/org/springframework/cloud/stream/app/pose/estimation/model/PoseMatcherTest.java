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

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.DefaultResourceLoader;

/**
 * @author Christian Tzolov
 */
public class PoseMatcherTest {

	public static void main(String[] args) throws IOException {
		Body[] bodies = new ObjectMapper().readValue(new DefaultResourceLoader().getResource("classpath:/pose-tourists.json").getInputStream(), Body[].class);

		PoseMatcher poseMatcher = PoseMatcher.instance();

		Map<String, MatchPair> maxMatch = new ConcurrentHashMap<>();
		for (Body b1 : bodies) {
			for (Body b2 : bodies) {
				String rec1 = description(poseMatcher.squaredBoundingBox(b1.getParts()));
				String rec2 = description(poseMatcher.squaredBoundingBox(b2.getParts()));
				double distance = poseMatcher.weightedDistance(b1, b2);
				System.out.println(String.format("%s -> %s : %f", rec1, rec2, distance));

				if (!rec1.equalsIgnoreCase(rec2)) {
					if (maxMatch.containsKey(rec1)) {
						MatchPair bestMatch = maxMatch.get(rec1);
						if (bestMatch.distance > distance) {
							maxMatch.put(rec1, new MatchPair(rec2, distance));
						}
					}
					else {
						maxMatch.put(rec1, new MatchPair(rec2, distance));
					}
				}
			}
		}

		System.out.println("BEST RESULTS:");
		for (String r : maxMatch.keySet()) {
			System.out.println(r + "->" + maxMatch.get(r).name + " (" + maxMatch.get(r).distance + ")");
		}

	}


	private static class MatchPair {
		final String name;
		final double distance;

		public MatchPair(String name, double distance) {
			this.name = name;
			this.distance = distance;
		}

		@Override
		public String toString() {
			return "" + name + " distance=" + distance;
		}
	}

	private static String description(Rectangle rectangle) {
		return String.format("%dx%dx%d", rectangle.x, rectangle.y, rectangle.width);
	}
}
