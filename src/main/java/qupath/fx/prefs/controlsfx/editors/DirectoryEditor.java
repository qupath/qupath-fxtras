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

package qupath.fx.prefs.controlsfx.editors;

import org.controlsfx.control.PropertySheet;

/**
 * Editor for selecting directory paths.
 *
 * Appears as a text field with associated button to launch a directory chooser.
 */
public class DirectoryEditor extends AbstractFileEditor {

    public DirectoryEditor(PropertySheet.Item property) {
        super(property);
    }
}
