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

import org.junit.Test;
import org.tensorflow.Tensor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * @author Christian Tzolov
 */
public class TensorJsonConverterTests {

	@Test
	public void longArray() {
		long[][] inLongArray = new long[2][2];
		inLongArray[0][0] = 0;
		inLongArray[0][1] = 1;
		inLongArray[1][0] = 2;
		inLongArray[1][1] = 3;

		Tensor inTensor = Tensor.create(inLongArray);

		String jsonTensor = TensorJsonConverter.toJson(inTensor);
		Tensor outTensor = TensorJsonConverter.toTensor(jsonTensor);

		long[][] outLongArray = new long[2][2];
		outLongArray = (long[][]) outTensor.copyTo(outLongArray);

		compareTensors(inTensor, outTensor);
		assertArrayEquals(inLongArray, outLongArray);
	}

	@Test
	public void longScalar() {
		long inLong = 666;

		Tensor inTensor = Tensor.create(inLong);

		String jsonTensor = TensorJsonConverter.toJson(inTensor);

		Tensor outTensor = TensorJsonConverter.toTensor(jsonTensor);

		compareTensors(inTensor, outTensor);
		assertEquals(inLong, outTensor.longValue());
	}

	private void compareTensors(Tensor in, Tensor out) {
		assertEquals(in.dataType(), out.dataType());
		assertEquals(in.numDimensions(), out.numDimensions());
		assertEquals(in.numBytes(), out.numBytes());
		assertArrayEquals(in.shape(), out.shape());
	}

}
