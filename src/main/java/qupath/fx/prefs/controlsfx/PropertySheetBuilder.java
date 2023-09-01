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

import org.controlsfx.control.PropertySheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.prefs.annotations.ColorPref;
import qupath.fx.prefs.annotations.DirectoryPref;
import qupath.fx.prefs.annotations.DoublePref;
import qupath.fx.prefs.annotations.IntegerPref;
import qupath.fx.prefs.annotations.Pref;
import qupath.fx.prefs.annotations.PrefCategory;

import java.util.Collection;

/**
 * Helper class for building a ControlsFX {@link org.controlsfx.control.PropertySheet}.
 */
public class PropertySheetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PropertySheetBuilder.class);

    private final PropertySheet sheet;

    private PropertyItemParser parser = new PropertyItemParser();

    /**
     * Create a builder for a default
     */
    public PropertySheetBuilder() {
        this(PropertySheetUtils.createDefaultPropertySheet());
    }

    private PropertySheetBuilder(PropertySheet sheet) {
       this.sheet = sheet == null ? PropertySheetUtils.createDefaultPropertySheet() : sheet;
    }

    /**
     * Set the parser to use. This is required if you need to control the resource manager or locale manager.
     * @param parser
     * @return this builder
     */
    public PropertySheetBuilder parser(PropertyItemParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * Add one or more pre-existing items to the property sheet.
     * @param items
     * @return this builder
     */
    public PropertySheetBuilder addItems(PropertySheet.Item... items) {
        for (var item : items)
            sheet.getItems().add(item);
        return this;
    }

    /**
     * Add a colleciton of pre-existing items to the property sheet.
     * @param items
     * @return this builder
     */
    public PropertySheetBuilder addItems(Collection<? extends PropertySheet.Item> items) {
        for (var item : items)
            sheet.getItems().add(item);
        return this;
    }

    /**
     * Install properties that are the public fields of an object, configured using annotations.
     * The properties themselves are accessed using reflection.
     * <p>
     * If the provided object has a {@link PrefCategory} annotation, this defines the category
     * for all the identified properties.
     * Each property should then have a {@link Pref} annotation, or an alternative
     * such as {@link DoublePref}, {@link ColorPref}, {@link DirectoryPref}, {@link IntegerPref}.
     * @param object
     * @return this builder
     */
    public PropertySheetBuilder addAnnotatedProperties(Object object) {
        var items = parser.parseAnnotatedItems(object);
        sheet.getItems().addAll(items);
        return this;
    }

    /**
     * Build the property sheet.
     * @return
     */
    public PropertySheet build() {
        return sheet;
    }


}
