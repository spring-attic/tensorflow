/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.app.test.tensorflow;

import java.util.Properties;

import org.springframework.cloud.stream.app.test.BinderTestPropertiesInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration for the generated Tensorflow processor app.
 *
 * @author Christian Tzolov
 *
 */
@Configuration
public class TensorflowProcessorTestConfiguration {

	@Bean
	public static BinderTestPropertiesInitializer loadProps(ConfigurableApplicationContext context) {
		// minimal properties for the context to load
		Properties properties = new Properties();
		properties.put("modelLocation", "classpath:tensorflow/model/linear_regression_graph.proto");
		properties.put("outputName", "add");
		return new BinderTestPropertiesInitializer(context, properties);
	}

}