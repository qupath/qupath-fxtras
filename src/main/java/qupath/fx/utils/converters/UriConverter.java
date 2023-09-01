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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * General {@link StringConverter} for URI objects, using their default string representation.
 */
public class UriConverter extends StringConverter<URI> {

    private final static Logger logger = LoggerFactory.getLogger(UriConverter.class);

    @Override
    public String toString(URI uri) {
        return uri == null ? null : uri.toString();
    }

    @Override
    public URI fromString(String string) {
        if (string == null)
            return null;
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            logger.error("Could not parse URI from " + string, e);
            return null;
        }
    }
}
