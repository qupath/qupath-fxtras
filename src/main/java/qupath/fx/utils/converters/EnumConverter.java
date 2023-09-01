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

package qupath.fx.utils.converters;

import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General {@link StringConverter} for enums.
 * @param <T> the enum type
 */
public class EnumConverter<T extends Enum> extends StringConverter<T> {

    private final static Logger logger = LoggerFactory.getLogger(EnumConverter.class);

    private Class<? extends T> enumType;

    public EnumConverter(Class<? extends T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String toString(T object) {
        return object == null ? null : object.name();
    }

    @Override
    public T fromString(String string) {
        if (string == null)
            return null;
        try {
            return (T) Enum.valueOf(enumType, string);
        } catch (Exception e) {
            logger.error("Could not parse enum value: " + string, e);
            return null;
        }
    }
}
