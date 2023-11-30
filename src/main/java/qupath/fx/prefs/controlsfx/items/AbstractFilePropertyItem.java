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

package qupath.fx.prefs.controlsfx.items;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.localization.LocalizedResourceManager;

import java.io.File;
import java.util.Optional;

/**
 * Create a property item that handles files or directories based on String paths.
 */
abstract class AbstractFilePropertyItem extends PropertyItem {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFilePropertyItem.class);

    private Property<String> prop;
    private ObservableValue<File> fileValue;

    AbstractFilePropertyItem(final LocalizedResourceManager manager, final Property<String> prop) {
        super(manager);
        this.prop = prop;
        fileValue = Bindings.createObjectBinding(() -> pathToFile(prop.getValue()), prop);
    }

    /**
     * Method to convert a string path to a file object.
     * Default implementation does not check if the file exists.
     * @param path
     * @return
     */
    protected File pathToFile(String path) {
        if (path == null || path.isEmpty())
            return null;
        else
            return new File(path);
    }

    @Override
    public Class<?> getType() {
        return File.class;
    }

    @Override
    public Object getValue() {
        return fileValue.getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            prop.setValue((String) value);
        } else if (value instanceof File)
            prop.setValue(((File) value).getAbsolutePath());
        else if (value == null)
            prop.setValue(null);
        else
            logger.error("Cannot set property {} with value {}", prop, value);
    }

    /**
     * Get the string property that represents the file path.
     * This may be more useful for editors that want to bind to the text property.
     * No check is made to ensure that the path is valid.
     * @return
     */
    public Property<String> getFilePathProperty() {
        return prop;
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
        return Optional.of(fileValue);
    }

}
