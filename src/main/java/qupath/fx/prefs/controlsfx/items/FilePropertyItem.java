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

import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import qupath.fx.dialogs.FileChoosers;
import qupath.fx.localization.LocalizedResourceManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Create a property item that handles files based on String paths.
 */
public class FilePropertyItem extends AbstractFilePropertyItem {

    private ObservableList<FileChooser.ExtensionFilter> extensionFilters = FXCollections.observableArrayList();

    public FilePropertyItem(LocalizedResourceManager manager, Property<String> prop) {
        this(manager, prop, Collections.emptyList());
    }

    public FilePropertyItem(LocalizedResourceManager manager, Property<String> prop,
                            Collection<? extends FileChooser.ExtensionFilter> filters) {
        super(manager, prop);
        if (filters == null || filters.isEmpty()) {
            extensionFilters.setAll(FileChoosers.FILTER_ALL_FILES);
        } else {
            extensionFilters.addAll(filters);
        }
    }

    /**
     * Get extension filters to use.
     * @return
     */
    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

}
