/*
 * Copyright 2017-2018 the original author or authors.
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

import java.util.Map;

import org.tensorflow.Tensor;

/**
 * The TensorFlow evaluation result is represented by a list of (non-serializable) {@link Tensor} instances.
 *
 * Implementations of this interface are responsible to convert the result {@link Tensor} instances into
 * a serializable type that can be send as a Message.
 *
 * The default implementation coverts the the {@link Tensor}s into JSON. Each tensor is encoded as:
 *
 * <li>
 *     	"type"  : TensorFlow {@link org.tensorflow.DataType} name
 *      "shape" : TensorFlow's shape
 *      "value" : Base64 encoded Tensor's value.
 * </li>
 *
 * It is the responsibility of the consumers of this message to decode the JSON back into {@link Tensor} instance.
 * The helper {@link TensorJsonConverter#toTensor(String)} static method helps to do this.
 *
 * A better approach is to provide a custom {@link TensorflowOutputConverter} implementation.
 *
 * @author Christian Tzolov
 */
public interface TensorflowOutputConverter<T> {
	/**
	 *
	 * @param resultTensors map of named {@link Tensor} results of the model evaluation.
	 * @param processorContext processorContext Context used to share information between the Input and Output converters
	 * @return Returns the converted {@link Tensor} data.
	 */
	T convert(Map<String, Tensor<?>> resultTensors, Map<String, Object> processorContext);

}
