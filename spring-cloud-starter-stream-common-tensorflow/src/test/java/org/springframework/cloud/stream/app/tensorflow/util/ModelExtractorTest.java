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

package org.springframework.cloud.stream.app.tensorflow.util;

import org.junit.Test;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Christian Tzolov
 */
public class ModelExtractorTest {

	@Test
	public void testResourceAndStringArguments() {
		Resource modelResource = new DefaultResourceLoader().getResource("classpath:/tensorflow/model/linear_regression_graph.proto");
		byte[] model1 = new ModelExtractor().getModel(modelResource);
		assertThat(model1.length, is(422));

		byte[] model2 = new ModelExtractor().getModel("classpath:/tensorflow/model/linear_regression_graph.proto");
		assertThat(model2.length, is(422));

		assertThat(model1, equalTo(model2));
	}

	@Test(expected = IllegalStateException.class)
	public void zipArchiveWithDefaultExtensionClasspath() {
		// By default it .pb extension is expected
		byte[] model = new ModelExtractor().getModel("classpath:/tensorflow/model.zip");
		assertThat(model.length, is(422));
	}

	@Test
	public void zipArchiveWithCustomExtensionClasspath() {
		byte[] model = new ModelExtractor(".proto").getModel("classpath:/tensorflow/model.zip");
		assertThat(model.length, is(422));
	}

	@Test
	public void zipArchiveWithFragmentFile() {
		byte[] model = new ModelExtractor().getModel("file:src/test/resources/tensorflow/model.zip#linear_regression_graph.proto");
		assertThat(model.length, is(422));
	}

	@Test
	public void tarGzipArchiveWithFragmentFile() {
		byte[] model = new ModelExtractor().getModel("file:src/test/resources/tensorflow/model.tar.gz#linear_regression_graph.proto");
		assertThat(model.length, is(422));
	}

	@Test
	public void tarGzipArchiveWithCustomExtensionFile() {
		byte[] model = new ModelExtractor(".proto").getModel("file:src/test/resources/tensorflow/model.tar.gz");
		assertThat(model.length, is(422));
	}

	@Test
	public void tarGzipArchiveWithFragmentHttp() {
		byte[] model = new ModelExtractor()
				.getModel("http://download.tensorflow.org/models/deeplabv3_mnv2_pascal_train_aug_2018_01_29.tar.gz#frozen_inference_graph.pb");
		assertThat(model.length, is(8773281));
	}

	@Test
	public void tarGzipArchiveWithDefaultExtensionHttp() {
		byte[] model = new ModelExtractor()
				.getModel("http://download.tensorflow.org/models/deeplabv3_mnv2_pascal_train_aug_2018_01_29.tar.gz");
		assertThat(model.length, is(8773281));
	}
}
