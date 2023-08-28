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

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUriConverter {

    private static List<URI> provideUris() {
        return Arrays.asList(
                Paths.get(".").toUri(),
                Paths.get(".").toAbsolutePath().toUri(),
                Paths.get(".").resolve("anything.txt").toUri(),
                Paths.get("/some/other/file").toUri(),
                URI.create("https://www.github.com"),
                null);
    }

    @ParameterizedTest
    @MethodSource("provideUris")
    void testToStringFromString(URI value) {
        var converter = new UriConverter();
        String string = converter.toString(value);
        URI converted = converter.fromString(string);
        assertEquals(value, converted);
    }

}