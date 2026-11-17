/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.fabric.client.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Charsets;

import joptsimple.internal.Strings;

public class ObjTokenizer implements AutoCloseable {
	private final BufferedReader lineReader;

	public ObjTokenizer(InputStream inputStream) {
		this.lineReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
	}

	@Nullable
	public String[] readAndSplitLine(boolean ignoreEmptyLines) throws IOException {
		
		do {
			String currentLine = lineReader.readLine();
			if (currentLine == null) {
				return null;
			}

			List<String> lineParts = new ArrayList<>();

			if (currentLine.startsWith("#")) {
				currentLine = "";
			}

			if (currentLine.length() > 0) {

				boolean hasContinuation;
				do {
					hasContinuation = currentLine.endsWith("\\");
					String tmp = hasContinuation ? currentLine.substring(0, currentLine.length() - 1) : currentLine;

					Arrays.stream(tmp.split("[\t ]+")).filter(s -> !Strings.isNullOrEmpty(s)).forEach(lineParts::add);

					if (hasContinuation) {
						currentLine = lineReader.readLine();
						if (currentLine == null) {
							break;
						}

						if (currentLine.length() == 0 || currentLine.startsWith("#")) {
							break;
						}
					}
				} while (hasContinuation);
			}

			if (lineParts.size() > 0) {
				return lineParts.toArray(new String[0]);
			}
		}
		while (ignoreEmptyLines);

		return new String[0];
	}

	@Override
	public void close() throws IOException {
		lineReader.close();
	}
}
