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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.SearchableComboBox;

import java.util.Collection;

/**
 * Editor for choosing from a longer list of items, aided by a searchable combo box.
 * @param <T>
 */
public class SearchableChoiceEditor<T> extends AbstractChoiceEditor<T, SearchableComboBox<T>> {

    public SearchableChoiceEditor(PropertySheet.Item property, Collection<? extends T> choices) {
        this(property, FXCollections.observableArrayList(choices));
    }

    public SearchableChoiceEditor(PropertySheet.Item property, ObservableList<T> choices) {
        super(new SearchableComboBox<>(), property, choices);
    }

}