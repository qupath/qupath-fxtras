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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.controlsfx.control.PropertySheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.dialogs.FileChoosers;
import qupath.fx.localization.LocaleManager;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.annotations.BooleanPref;
import qupath.fx.prefs.annotations.ColorPref;
import qupath.fx.prefs.annotations.DirectoryPref;
import qupath.fx.prefs.annotations.DoublePref;
import qupath.fx.prefs.annotations.FilePref;
import qupath.fx.prefs.annotations.IntegerPref;
import qupath.fx.prefs.annotations.LocalePref;
import qupath.fx.prefs.annotations.Pref;
import qupath.fx.prefs.annotations.PrefCategory;
import qupath.fx.prefs.annotations.StringPref;
import qupath.fx.prefs.controlsfx.items.PropertyItem;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A parser for JavaFX properties annotated with {@link Pref} and related annotations.
 * <p>
 * This can be used to create a list of {@link PropertySheet.Item} objects for use with a {@link PropertySheet},
 * while writing minimal code (i.e. just define the properties and annotate them appropriately).
 * <p>
 * For an alternative that does not use annotations, see {@link PropertyItemBuilder}.
 */
public class PropertyItemParser {

    private static final Logger logger = LoggerFactory.getLogger(PropertyItemParser.class);

    private LocalizedResourceManager resourceManager;

    private LocaleManager localeManager;

    /**
     * Set the resource manager. This is used to obtain localized strings for annotated preferences from a
     * ResourceBundle.
     * @param manager
     * @return
     */
    public PropertyItemParser setResourceManager(LocalizedResourceManager manager) {
        this.resourceManager = manager;
        return this;
    }

    /**
     * Set the locale manager. This is used to control the available locales for any preference annotated with
     * {@link LocalePref}.
     * @param manager
     * @return
     */
    public PropertyItemParser setLocaleManager(LocaleManager manager) {
        this.localeManager = manager;
        return this;
    }

    /**
     * Parse all the JavaFX properties with annotations (e.g. {@link Pref}) that are accessible fields of the given
     * object.
     * @param obj
     * @return
     */
    public List<PropertySheet.Item> parseAnnotatedItems(Object obj) {

        var cls = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        List<PropertySheet.Item> items = new ArrayList<>();

        // Look for category annotation from the parent class
        String categoryBundle = null;
        String categoryKey = null;
        if (cls.isAnnotationPresent(PrefCategory.class)) {
            var annotation = cls.getAnnotation(PrefCategory.class);
            categoryBundle = annotation.bundle().isBlank() ? null : annotation.bundle();
            categoryKey = annotation.value();
        }

        for (var field : cls.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !Property.class.isAssignableFrom(field.getType()))
                continue;
            PropertySheet.Item item = null;
            try {
                if (!field.canAccess(obj))
                    field.setAccessible(true);

                // Skip null fields
                if (field.get(obj) == null)
                    continue;

                if (field.isAnnotationPresent(Pref.class)) {
                    item = parseItem((Property)field.get(obj), field.getAnnotation(Pref.class), obj);
                } else if (field.isAnnotationPresent(BooleanPref.class)) {
                    item = parseItem((BooleanProperty)field.get(obj), field.getAnnotation(BooleanPref.class));
                } else if (field.isAnnotationPresent(IntegerPref.class)) {
                    item = parseItem((IntegerProperty)field.get(obj), field.getAnnotation(IntegerPref.class));
                } else if (field.isAnnotationPresent(DoublePref.class)) {
                    item = parseItem((DoubleProperty)field.get(obj), field.getAnnotation(DoublePref.class));
                } else if (field.isAnnotationPresent(StringPref.class)) {
                    item = parseItem((Property<String>)field.get(obj), field.getAnnotation(StringPref.class));
                } else if (field.isAnnotationPresent(LocalePref.class)) {
                    item = parseItem((Property<Locale>)field.get(obj), field.getAnnotation(LocalePref.class));
                } else if (field.isAnnotationPresent(ColorPref.class)) {
                    item = parseItem((Property<Integer>)field.get(obj), field.getAnnotation(ColorPref.class));
                } else if (field.isAnnotationPresent(FilePref.class)) {
                    item = parseItem((Property<String>)field.get(obj), field.getAnnotation(FilePref.class));
                } else if (field.isAnnotationPresent(DirectoryPref.class)) {
                    item = parseItem((Property<String>)field.get(obj), field.getAnnotation(DirectoryPref.class));
                }

                // Handle direct category annotation
                if (item instanceof PropertyItem propertyItem) {
                    if (field.isAnnotationPresent(PrefCategory.class)) {
                        var annotation = cls.getAnnotation(PrefCategory.class);
                        String localeCategoryBundle = annotation.bundle().isBlank() ? null : annotation.bundle();
                        String localeCategoryKey = annotation.value();
                        propertyItem.categoryKey(localeCategoryBundle, localeCategoryKey);
                    } else {
                        propertyItem.categoryKey(categoryBundle, categoryKey);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private PropertySheet.Item parseItem(Property property, Pref annotation, Object parent) {

        var builder = buildItem(property, annotation.type())
                .resourceManager(resourceManager)
                .key(annotation.value())
                .bundle(annotation.bundle());

        var choiceMethod = annotation.choiceMethod();
        if (!choiceMethod.isBlank() && parent != null) {
            var cls = parent.getClass();
            try {
                var method = cls.getDeclaredMethod(choiceMethod);
                method.setAccessible(true);
                var result = method.invoke(parent);
                if (result instanceof ObservableList list) {
                    builder.choices(list);
                } else if (result instanceof Collection collection) {
                    builder.choices(collection);
                }
            } catch (Exception e) {
                logger.error("Unable to parse choices from " + annotation + ": " + e.getMessage(), e);
            }
        }

        return builder.build();
    }

    private PropertySheet.Item parseItem(BooleanProperty property, BooleanPref annotation) {
        return buildItem(property, Boolean.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .build();
    }

    private PropertySheet.Item parseItem(IntegerProperty property, IntegerPref annotation) {
        return buildItem(property, Integer.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .build();
    }

    private PropertySheet.Item parseItem(DoubleProperty property, DoublePref annotation) {
        return buildItem(property, Double.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .build();
    }

    private PropertySheet.Item parseItem(Property<String> property, StringPref annotation) {
        return buildItem(property, String.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .build();
    }

    private PropertySheet.Item parseItem(Property<Locale> property, LocalePref annotation) {
        var localeManager = this.localeManager == null ? new LocaleManager() : this.localeManager;
        return buildItem(property, Locale.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .choices(annotation.availableLanguagesOnly() ? localeManager.getAvailableLocales() : localeManager.getAllLocales())
                .propertyType(PropertyItemBuilder.PropertyType.SEARCHABLE_CHOICE)
                .build();
    }

    private PropertySheet.Item parseItem(Property<Integer> property, ColorPref annotation) {
        return buildItem(property, Integer.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .propertyType(PropertyItemBuilder.PropertyType.COLOR)
                .build();
    }

    private PropertySheet.Item parseItem(Property<String> property, DirectoryPref annotation) {
        return buildItem(property, String.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .propertyType(PropertyItemBuilder.PropertyType.DIRECTORY)
                .build();
    }

    private PropertySheet.Item parseItem(Property<String> property, FilePref annotation) {
        List<FileChooser.ExtensionFilter> filters = new ArrayList<>();
        if (annotation.extensions() != null && annotation.extensions().length > 0) {
            filters.add(FileChoosers.createExtensionFilter(null, annotation.extensions()));
        }
        return buildItem(property, String.class)
                .key(annotation.value())
                .resourceManager(resourceManager)
                .bundle(annotation.bundle())
                .propertyType(PropertyItemBuilder.PropertyType.FILE)
                .extensionFilters(filters)
                .build();
    }


    private <T> PropertyItemBuilder<T> buildItem(Property<T> prop, final Class<? extends T> cls) {
        return new PropertyItemBuilder(prop, cls);
    }

}
