/**
 * Copyright 2023 The University of Edinburgh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package qupath.fx.prefs.converters;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFileConverter {

    private static List<File> provideFiles() {
        return Arrays.asList(
                new File("."),
                new File(".").getAbsoluteFile(),
                new File("/some/other/file"),
                null);
    }

    @ParameterizedTest
    @MethodSource("provideFiles")
    void testToStringFromString(File value) {
        var converter = new FileConverter();
        String string = converter.toString(value);
        File converted = converter.fromString(string);
        assertEquals(value, converted);
    }

}