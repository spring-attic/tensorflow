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

package org.springframework.cloud.stream.app.image.recognition.processor;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;


/**
 * @author Christian Tzolov
 */
public class ImageRecognitionProcessorPropertiesTest {

	@Test
	public void labelsCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.image.recognition.labels:/remote");
		context.register(Conf.class);
		context.refresh();
		ImageRecognitionProcessorProperties properties = context.getBean(ImageRecognitionProcessorProperties.class);
		assertThat(properties.getLabels(), equalTo(context.getResource("/remote")));
	}

	@Test
	public void responseSizeCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.image.recognition.labels:/remote");
		EnvironmentTestUtils.addEnvironment(context, "tensorflow.image.recognition.responseSize:5");
		context.register(Conf.class);
		context.refresh();
		ImageRecognitionProcessorProperties properties = context.getBean(ImageRecognitionProcessorProperties.class);
		assertThat(properties.getResponseSize(), equalTo(5));
	}

	@Configuration
	@EnableConfigurationProperties(ImageRecognitionProcessorProperties.class)
	static class Conf {

	}

}
