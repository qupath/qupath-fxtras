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
import qupath.fx.localization.LocalizedResourceManager;

/**
 * Create a property item that handles directories based on String paths.
 */
public class DirectoryPropertyItem extends AbstractFilePropertyItem {

    public DirectoryPropertyItem(LocalizedResourceManager manager, Property<String> prop) {
        super(manager, prop);
    }
}
