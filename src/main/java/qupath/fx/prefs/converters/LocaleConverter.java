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

import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class LocaleConverter extends StringConverter<Locale> {

    private final static Logger logger = LoggerFactory.getLogger(LocaleConverter.class);

    @Override
    public String toString(Locale locale) {
        return locale == null ? null : locale.getDisplayName(Locale.US);
//        return locale == null ? null : locale.toLanguageTag();
    }

    @Override
    public Locale fromString(String string) {
        if (string == null)
            return null;
        try {
            return Arrays.stream(Locale.getAvailableLocales())
                    .filter(l -> Objects.equals(l.getDisplayName(Locale.US), string))
                    .findFirst().orElse(null);
//            // Note the Locale javadocs state that toLanguageTag and forLanguageTag aren't guaranteed to round-trip!
//            return Locale.forLanguageTag(string);
        } catch (Exception e) {
            logger.error("Could not parse file from " + string, e);
            return null;
        }
    }
}
