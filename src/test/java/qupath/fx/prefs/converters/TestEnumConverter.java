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

import org.junit.jupiter.api.Test;
import qupath.fx.utils.converters.EnumConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestEnumConverter {

    private enum TestEnum {
        A, B, C, Longer, Short
    }

    @Test
    void testEnumConversion() {
        var converter = new EnumConverter<>(TestEnum.class);
        for (var val : TestEnum.values())
            testToStringFromString(converter, val);

        testToStringFromString(converter, null);
    }

    private static <T extends Enum> void testToStringFromString(EnumConverter<T> converter, T value) {
        String string = converter.toString(value);
        T converted = converter.fromString(string);
        assertEquals(value, converted);
    }

}