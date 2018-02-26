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

package org.springframework.cloud.stream.app.tensorflow.processor;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

/**
 * Create the output message from the original input message and the computed tensorflow score data.
 *
 * @author Christian Tzolov
 */
public interface OutputMessageBuilder {

	/**
	 * Compute an output message from the input and the scored data.
	 * @param inputMessage Original input message
	 * @param computedScore computed score
	 * @return the computed output message
	 */
	MessageBuilder<?> createOutputMessageBuilder(Message<?> inputMessage, Object computedScore);
}
