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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class TestLocaleConverter {

    @Test
    void testLocaleConversion() {
        testToStringFromString(Locale.US);

        // Test all available locales with different defaults set
        Locale.setDefault(Locale.US);
        for (var locale : Locale.getAvailableLocales())
            testToStringFromString(locale);

        Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
        for (var locale : Locale.getAvailableLocales())
            testToStringFromString(locale);

        Locale.setDefault(Locale.JAPANESE);
        for (var locale : Locale.getAvailableLocales())
            testToStringFromString(locale);

        Locale.setDefault(Locale.CANADA);
        Locale.setDefault(Locale.Category.DISPLAY, Locale.ITALIAN);
        Locale.setDefault(Locale.Category.FORMAT, Locale.CHINA);
        for (var locale : Locale.getAvailableLocales())
            testToStringFromString(locale);

    }

    @Test
    void testNullLocaleConversion() {
        testToStringFromString(null);
    }

    @Test
    void testInvalidLocaleConversion() {
        var converter = new LocaleConverter();
        var locale = new Locale("abc");
        String string = converter.toString(locale);
        assertEquals(string, "abc");
        Locale localeConverted = converter.fromString(string);
        assertNull(localeConverted);
    }

    private static void testToStringFromString(Locale locale) {
        var converter = new LocaleConverter();
        String string = converter.toString(locale);
        Locale localeConverted = converter.fromString(string);
        assertEquals(locale, localeConverted);
    }

}