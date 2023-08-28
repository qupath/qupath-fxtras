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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathConverter extends StringConverter<Path> {

    private final static Logger logger = LoggerFactory.getLogger(PathConverter.class);

    @Override
    public String toString(Path path) {
        return path == null ? null : path.toString();
    }

    @Override
    public Path fromString(String string) {
        if (string == null)
            return null;
        try {
            return Paths.get(string);
        } catch (InvalidPathException e) {
            logger.error("Could not parse path from " + string, e);
            return null;
        }
    }
}
