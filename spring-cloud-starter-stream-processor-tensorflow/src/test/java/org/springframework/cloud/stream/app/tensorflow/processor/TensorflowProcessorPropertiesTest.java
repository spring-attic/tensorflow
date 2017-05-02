/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.tensorflow.processor;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christian Tzolov
 */
public class TensorflowProcessorPropertiesTest {

	private AnnotationConfigApplicationContext context;

	@Before
	public void beforeTest() {
		context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.modelLocation:NONE");
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.outputName:NONE");
	}

	@Test
	public void modelLocationCanBeCustomized() {
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.modelLocation:/remote");
		context.register(Conf.class);
		context.refresh();
		TensorflowProcessorProperties properties = context.getBean(TensorflowProcessorProperties.class);
		assertThat(properties.getModelLocation(), equalTo(context.getResource("/remote")));
	}

	@Test
	public void outputNameCanBeCustomized() {
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.outputName:output1");
		context.register(Conf.class);
		context.refresh();
		TensorflowProcessorProperties properties = context.getBean(TensorflowProcessorProperties.class);
		assertThat(properties.getOutputName(), equalTo("output1"));
	}

	@Test
	public void outputIndexCanBeCustomized() {
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.outputIndex:666");
		context.register(Conf.class);
		context.refresh();
		TensorflowProcessorProperties properties = context.getBean(TensorflowProcessorProperties.class);
		assertThat(properties.getOutputIndex(), equalTo(666));
	}

	@Configuration
	@EnableConfigurationProperties(TensorflowProcessorProperties.class)
	static class Conf {

	}
}
