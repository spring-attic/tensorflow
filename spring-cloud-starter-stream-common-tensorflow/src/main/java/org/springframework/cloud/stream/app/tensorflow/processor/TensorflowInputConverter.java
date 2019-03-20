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

import org.springframework.messaging.Message;

import java.util.Map;

/**
 * The {@link TensorflowInputConverter} is called by the TensorFlow Processor to convert the incoming {@link Message}s
 * into a data type that matches the input of the TensorFlow model being used.
 *
 * The default implementation assumes that the received data has already been covered (before sent to the processor) and
 * is encoded into key/value Map or flat key/value JSON message. Where each key in the map corresponds to a model input
 * placeholder and the value is compliant with TensorFlow's {@link org.tensorflow.DataType}.
 *
 *  @see <a href="https://bit.ly/2ox4IFG">TwitterSentimentTensorflowInputConverter.java</a> for how to build custom {@link TensorflowInputConverter}.
 *
 * @author Christian Tzolov
 */
public interface TensorflowInputConverter {
	/**
	 *
	 * @param input Processor's input data.
	 * @param processorContext Context used to share information between the Input and Output converters
	 * @return Returns map that corresponds to the TensorFlow model's input format.
	 */
	Map<String, Object> convert(Object input, Map<String, Object> processorContext);

}
