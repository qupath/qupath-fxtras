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

package qupath.fx.prefs.controlsfx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.controlsfx.control.PropertySheet;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.controlsfx.items.*;

import java.util.Collection;

/**
 * Builder class for creating {@link PropertySheet.Item} instances, using a fluent interface and optionally supporting
 * localized strings.
 * This works as an alternative to {@link PropertyItemParser} without requiring the use of annotations.
 * @param <T>
 */
public class PropertyItemBuilder<T> {

    public enum PropertyType {GENERAL, FILE, DIRECTORY, COLOR, CHOICE, SEARCHABLE_CHOICE}

    private LocalizedResourceManager manager;

    private Property<T> property;
    private Class<? extends T> cls;

    private PropertyType propertyType = PropertyType.GENERAL;
    private ObservableList<T> choices;

    // Preferred methods of setting name/description/category (with localized strings)
    private String bundle;
    private String key;
    private String categoryKey;

    // Alternative methods of setting name/description/category (with non-localized strings)
    private String name;
    private String description;
    private String category;
    private ObservableList<? extends FileChooser.ExtensionFilter> extensionFilters;

    /**
     * Create a new builder for a property.
     * @param prop the property
     * @param cls the type of the property value
     */
    public PropertyItemBuilder(Property<T> prop, final Class<? extends T> cls) {
        this.property = prop;
        this.cls = cls;
    }

    /**
     * Set the resource manager to use for localization.
     * If provided, this enables the use of {@link #key(String)} and {@link #categoryKey(String)} - and also
     * updating these values if the locale changes.
     * @param manager
     * @return
     */
    public PropertyItemBuilder<T> resourceManager(LocalizedResourceManager manager) {
        this.manager = manager;
        return this;
    }

    /**
     * Set the resource bundle key for the category.
     * If this is set, it will be used to look up the name for the property.
     * Furthermore, if {@code key + ".description"} is found in the bundle, it will be used to set the description.
     * @param key
     * @return
     */
    public PropertyItemBuilder<T> key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Set the resource bundle key for the category.
     * @param key
     * @return
     */
    public PropertyItemBuilder<T> categoryKey(String key) {
        this.categoryKey = key;
        return this;
    }

    /**
     * Set the resource bundle name, if required.
     * This is used in combination with #key(String) and/or #categoryKey(String).
     * It may not be required if #resourceManager(LocalizedResourceManager) is used with the relevant default bundle.
     * @param name
     * @return
     */
    public PropertyItemBuilder<T> bundle(String name) {
        this.bundle = name;
        return this;
    }

    /**
     * Set the type of the property to create.
     * @param type
     * @return
     */
    public PropertyItemBuilder<T> propertyType(PropertyType type) {
        this.propertyType = type;
        return this;
    }

    /**
     * Set the name directly, using a non-localized string.
     * @param name
     * @return
     * @see #key(String)
     */
    public PropertyItemBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the description directly, using a non-localized string.
     * @param description
     * @return
     * @see #key(String)
     */
    public PropertyItemBuilder<T> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the category directly, using a non-localizes string.
     * @param category
     * @return
     * @see #categoryKey(String)
     */
    public PropertyItemBuilder<T> category(String category) {
        this.category = category;
        return this;
    }

    /**
     * Provide a collection of choices for a choice property.
     * @param choices
     * @return
     * @see #choices(ObservableList)
     */
    public PropertyItemBuilder<T> choices(Collection<T> choices) {
        return choices(FXCollections.observableArrayList(choices));
    }

    /**
     * Provide an observable list of choices for a choice property.
     * This may be used directly, and so changes can influence the property.
     * @param choices
     * @return
     * @see #choices(Collection)
     */
    public PropertyItemBuilder<T> choices(ObservableList<T> choices) {
        this.choices = choices;
        this.propertyType = PropertyType.CHOICE;
        return this;
    }

    /**
     * Provide a collection of file extension filters to use with a file property.
     * For non-file properties, this are ignored.
     * @param filters
     * @return
     */
    public PropertyItemBuilder<T> extensionFilters(Collection<? extends FileChooser.ExtensionFilter> filters) {
        this.extensionFilters = filters == null ?
                FXCollections.emptyObservableList() :
                FXCollections.observableArrayList(filters);
        return this;
    }

    /**
     * Build the property item.
     * @return
     */
    public PropertySheet.Item build() {
        PropertyItem item;
        switch (propertyType) {
            case DIRECTORY:
                item = new DirectoryPropertyItem(manager, (Property<String>) property);
                break;
            case FILE:
                item = new FilePropertyItem(manager, (Property<String>) property, extensionFilters);
                break;
            case COLOR:
                item = new ColorPropertyItem(manager, (IntegerProperty) property);
                break;
            case CHOICE:
                item = new ChoicePropertyItem<>(manager, property, choices, cls, false);
                break;
            case SEARCHABLE_CHOICE:
                item = new ChoicePropertyItem<>(manager, property, choices, cls, true);
                break;
            case GENERAL:
            default:
                item = new DefaultPropertyItem<>(manager, property, cls);
                break;
        }
        // Set the name, category & description - they may be overridden by localized strings anyway
        item.name(name);
        item.category(category);
        item.description(description);

        if (key != null)
            item.key(bundle, key);

        if (categoryKey != null) {
            item.categoryKey(bundle, categoryKey);
        }
        return item;
    }

}
