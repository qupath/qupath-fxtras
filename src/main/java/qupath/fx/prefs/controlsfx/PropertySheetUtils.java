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

import javafx.beans.property.Property;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.textfield.CustomTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.controlsfx.items.DefaultPropertyItem;
import qupath.fx.prefs.controlsfx.items.PropertyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Static untility methods for working with {@link PropertySheet} objects.
 */
public class PropertySheetUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertySheetUtils.class);

    /**
     * Create a property sheet with the default mode (CATEGORY) and editor factory.
     * @return
     */
    public static PropertySheet createDefaultPropertySheet() {
        var propSheet = new PropertySheet();
        propSheet.setMode(PropertySheet.Mode.CATEGORY);
        propSheet.setPropertyEditorFactory(new PropertyEditorFactory());
        return propSheet;
    }

    /**
     * Create a default {@link PropertySheet.Item} for a generic property.
     * @param <T> type of the property
     * @param property the property
     * @param cls the property type
     * @return a new {@link PropertyItem}
     */
    public static <T> PropertyItem createPropertySheetItem(Property<T> property, Class<? extends T> cls) {
        return new DefaultPropertyItem<>(property, cls);
    }

    /**
     * Refresh the editors in a {@link PropertySheet}.
     * This can be useful to respond to locale changes.
     * @param propSheet
     * @implNote this involves removing all items, then re-adding them.
     *           It also involves resetting the search text field.
     */
    public static void refreshEditors(PropertySheet propSheet) {
        logger.trace("Refreshing property sheet editors");
        var items = new ArrayList<>(propSheet.getItems());
        propSheet.getItems().clear();
        propSheet.getItems().addAll(items);
        // Try to reset any filter text - when the locale changes, this stops being meaningful.
        // We need to do it via the text field, since it isn't bidirectionally bound to
        // propSheet.titleFilterProperty()
        String filterText = propSheet.getTitleFilter();
        if (filterText != null && !filterText.isEmpty()) {
            for (var node : propSheet.lookupAll(".custom-text-field")) {
                if (node instanceof CustomTextField tf) {
                    if (Objects.equals(filterText, tf.getText())) {
                        tf.clear();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Parse the annotated properties of an object into a list of {@link PropertySheet.Item} objects.
     * @param obj the object that contains annotated JavaFX properties
     * @return the list of items from all found properties
     * @see PropertyItemParser#parseAnnotatedItems(Object)
     */
    public static List<PropertySheet.Item> parseAnnotatedItems(Object obj) {
        return parseAnnotatedItemsWithResources(null, obj);
    }

    /**
     * Parse the annotated properties of an object into a list of {@link PropertySheet.Item} objects, while setting
     * the localized resource manager of the parser.
     * @param obj the object that contains annotated JavaFX properties
     * @return the list of items from all found properties
     * @see PropertyItemParser#parseAnnotatedItems(Object)
     */
    public static List<PropertySheet.Item> parseAnnotatedItemsWithResources(LocalizedResourceManager manager, Object obj) {
        return new PropertyItemParser()
                .setResourceManager(manager)
                .parseAnnotatedItems(obj);
    }

}
