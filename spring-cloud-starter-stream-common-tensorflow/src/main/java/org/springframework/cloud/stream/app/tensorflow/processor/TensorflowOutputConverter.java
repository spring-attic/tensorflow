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

package org.springframework.cloud.stream.app.tensorflow.processor;

import org.springframework.tuple.Tuple;
import org.tensorflow.Tensor;

import java.util.Map;

/**
 * The TensorFlow evaluation result is represented by a list of (non-serializable) {@link Tensor} instances.
 *
 * Implementations of this interface are responsible to convert the result {@link Tensor} instances into
 * a serializable type that can be send as a Message.
 *
 * The default implementation coverts the the {@link Tensor}s into {@link Tuple}. Each tensor is encoded as:
 *
 * <li>
 *     	"type"  : TensorFlow {@link org.tensorflow.DataType} name
 *      "shape" : TensorFlow's shape
 *      "value" : ByteBuffer encoded Tensor's value.
 * </li>
 *
 * It is the responsibility of the consumers of this message to decode the Tuple back into {@link Tensor} instance.
 * The helper {@link TensorTupleConverter#toTensor(Tuple)} static method helps to do this.
 *
 * A better approach is to provide a custom {@link TensorflowOutputConverter} implementation.
 * @see <a href="https://bit.ly/2pKBghe">TwitterSentimentTensorflowOutputConverter.java</a> for how to build custom {@link TensorflowOutputConverter}.
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
