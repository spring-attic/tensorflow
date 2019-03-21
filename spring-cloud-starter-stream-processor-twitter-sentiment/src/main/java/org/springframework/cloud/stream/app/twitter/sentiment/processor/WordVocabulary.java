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

package org.springframework.cloud.stream.app.twitter.sentiment.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Tzolov
 */
public class WordVocabulary implements AutoCloseable {

	/**
	 *  This contains the word vocabulary used to train the TensorFlow model.
	 */
	private final ConcurrentHashMap<String, Integer> vocabulary;

	public WordVocabulary(InputStream vocabularyInputStream) throws IOException {
		vocabulary = buildVocabulary(vocabularyInputStream);
	}

	public int[][] vectorizeSentence(String sentence) {
		int[][] vectorizedText = new int[1][128];
		String[] words = clearText(sentence).split(" ");
		for (int i = 0; i < words.length; i++) {
			Integer v = vocabulary.get(words[i]);
			vectorizedText[0][i] = (v != null)? v : 0;
		}
		return vectorizedText;
	}

	private ConcurrentHashMap<String, Integer> buildVocabulary(InputStream input) throws IOException {

		ConcurrentHashMap<String, Integer> vocabulary = new ConcurrentHashMap<>();

		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			String l = buffer.readLine();
			while (l != null ) {
				String p[] = l.split(",");
				if (p[1].length() > 1) {
					vocabulary.put(p[0], Integer.valueOf(p[1]));
				}
				l = buffer.readLine();
			}
		}
		return vocabulary;
	}

	private String clearText(String sentence) {
		return sentence
				.trim()
				.replaceAll("[^A-Za-z0-9(),!?\\'\\`]", " ")
				.replaceAll("(.)\\1+", "\\1\\1")
				.replaceAll("\\'s", " \\'s")
				.replaceAll("\\'ve", " \\'ve")
				.replaceAll("n\\'t", " n\\'t")
				.replaceAll("\\'re", " \\'re")
				.replaceAll("\\'d", " \\'d")
				.replaceAll("\\'ll", " \\'ll")
				.replaceAll(",", " , ")
				.replaceAll("!", " ! ")
				.replaceAll("\\(", " \\( ")
				.replaceAll("\\)", " \\) ")
				.replaceAll("\\?", " \\? ")
				.replaceAll("\\s{2,}", " ")
				.toLowerCase();
	}

	@Override
	public void close() throws Exception {
		vocabulary.clear();
	}

}

