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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.controlsfx.control.PropertySheet;
import qupath.fx.localization.LocalizedResourceManager;

/**
 * Base implementation of {@link PropertySheet.Item}.
 */
public abstract class PropertyItem implements PropertySheet.Item {

    private static LocalizedResourceManager DEFAULT_MANAGER = LocalizedResourceManager.createInstance(null);

    private final LocalizedResourceManager manager;

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    PropertyItem(LocalizedResourceManager manager) {
        this.manager = manager == null ? DEFAULT_MANAGER : manager;
    }

    /**
     * Support fluent interface to define a category.
     * @param category
     * @return
     */
    public PropertyItem category(final String category) {
        this.category.set(category);
        return this;
    }

    /**
     * Support fluent interface to set the description.
     * @param description
     * @return
     */
    public PropertyItem description(final String description) {
        this.description.set(description);
        return this;
    }

    /**
     * Support fluent interface to set the name.
     * @param name
     * @return
     */
    public PropertyItem name(String name) {
        this.name.set(name);
        return this;
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public StringProperty descriptionProperty() {
        return this.description;
    }

    public PropertyItem key(String bundle, String key) {
        if (bundle != null && bundle.isBlank())
            bundle = null;
        manager.registerProperty(name, bundle, key);
        if (manager.hasString(bundle, key + ".description"))
            manager.registerProperty(description, bundle, key + ".description");
        return this;
    }

    public PropertyItem categoryKey(final String bundle, final String key) {
        manager.registerProperty(category, bundle, key);
        return this;
    }

    @Override
    public String getCategory() {
        return category.get();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getDescription() {
        return description.get();
    }


}