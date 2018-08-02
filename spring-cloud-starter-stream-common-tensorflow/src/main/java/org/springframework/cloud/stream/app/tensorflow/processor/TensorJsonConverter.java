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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tensorflow.DataType;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

/**
 * Utility that helps to covert {@link Tensor} to Json and in reverse.
 * @author Christian Tzolov
 */
public class TensorJsonConverter {

	public static String toJson(Tensor tensor) {

		// Retrieve all bytes in the buffer
		ByteBuffer buffer = ByteBuffer.allocate(tensor.numBytes());
		tensor.writeTo(buffer);
		buffer.clear();
		byte[] bytes = new byte[buffer.capacity()];
		buffer.get(bytes, 0, bytes.length);

		long[] shape = tensor.shape();

		String bytesBase64 = Base64.getEncoder().encodeToString(bytes);

		return String.format("{ \"type\": \"%s\", \"shape\": %s, \"value\": \"%s\" }",
				tensor.dataType().name(), Arrays.toString(shape), bytesBase64);
	}

	public static Tensor toTensor(String json) {
		try {
			JsonTensor jsonTensor = new ObjectMapper().readValue(json, JsonTensor.class);
			DataType dataType = DataType.valueOf(jsonTensor.getType());
			long[] shape = jsonTensor.getShape();
			byte[] tfValue = Base64.getDecoder().decode(jsonTensor.getValue());
			return Tensor.create(dataTypeToClass(dataType), shape, ByteBuffer.wrap(tfValue));
		}
		catch (Throwable throwable) {
			throw new RuntimeException(String.format("Can not covert json:'%s' into Tensor", json), throwable);
		}
	}

	private static final Map<DataType, Class<?>> typeToClassMap = new HashMap<>();

	static {
		typeToClassMap.put(DataType.FLOAT, Float.class);
		typeToClassMap.put(DataType.DOUBLE, Double.class);
		typeToClassMap.put(DataType.INT32, Integer.class);
		typeToClassMap.put(DataType.UINT8, UInt8.class);
		typeToClassMap.put(DataType.INT64, Long.class);
		typeToClassMap.put(DataType.BOOL, Boolean.class);
		typeToClassMap.put(DataType.STRING, String.class);
	}

	private static Class<?> dataTypeToClass(DataType dataType) {
		Class<?> clazz = typeToClassMap.get(dataType);
		if (clazz == null) {
			throw new IllegalArgumentException("No class found for dataType: " + dataType);
		}
		return clazz;
	}

	public static void main(String[] args) throws IOException {
		long[] l = new long[] { 1, 2, 3, 4 };
		System.out.println(Arrays.toString(l));

		String b = String.format("{ \"type\": \"%s\", \"shape\": %s, \"value\": \"%s\" }",
				"myType", Arrays.toString(l), "boza");

		JsonTensor jt = new ObjectMapper().readValue(b, JsonTensor.class);
		System.out.println(jt.getShape().length);
	}
}
