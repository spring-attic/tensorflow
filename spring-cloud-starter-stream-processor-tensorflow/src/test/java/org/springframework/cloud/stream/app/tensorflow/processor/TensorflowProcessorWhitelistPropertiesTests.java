/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.tensorflow.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Christian Tzolov
 */
public class TensorflowProcessorWhitelistPropertiesTests {

	public static final String CONFIGURATION_PROPERTIES_CLASSES = "configuration-properties.classes";

	private static final String WHITELIST_PROPERTIES_LOCATION = "/META-INF/spring-configuration-metadata-whitelist.properties";

	@Test
	public void whitelistPropertiesExist() throws IOException, ClassNotFoundException {
		try (InputStream is = new ClassPathResource(WHITELIST_PROPERTIES_LOCATION).getInputStream()) {
			assertNotNull(is);
			Properties properties = new Properties();
			properties.load(is);
			assertEquals(1, properties.size());

			String[] classNames = properties.getProperty(CONFIGURATION_PROPERTIES_CLASSES).split(",");
			assertNotNull(classNames);
			assertEquals(1, classNames.length);

			for (String clazz : classNames) {
				assertNotNull(Class.forName(clazz.trim()));
			}
		}
	}

}
