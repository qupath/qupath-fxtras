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

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.prefs.controlsfx.items.PropertyItem;
import qupath.fx.utils.converters.LocaleConverter;

import java.util.Locale;

abstract class AbstractChoiceEditor<T, S extends ComboBox<T>> extends AbstractPropertyEditor<T, S> implements ListChangeListener<T> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractChoiceEditor.class);

    private ObservableList<T> choices;

    AbstractChoiceEditor(S combo, PropertySheet.Item property, ObservableList<T> choices) {
        super(property, combo);
        if (property.getType().equals(Locale.class)) {
            combo.setConverter((StringConverter<T>)new LocaleConverter());
        }
        if (property instanceof PropertyItem item) {
            combo.promptTextProperty().bind(item.promptProperty());
        }
        this.choices = choices == null ? FXCollections.observableArrayList() : choices;
        combo.getItems().setAll(this.choices);
        this.choices.addListener(this);
    }

    @Override
    public void setValue(T value) {
        // Only set the value if it's available as a choice
        var combo = getEditor();
        if (combo.getItems().contains(value))
            combo.getSelectionModel().select(value);
        else {
            if (value != null)
                logger.warn("Value {} not found in choices for property {}", value, getProperty().getName());
            combo.getSelectionModel().clearSelection();
        }
    }

    @Override
    protected ObservableValue<T> getObservableValue() {
        return getEditor().getSelectionModel().selectedItemProperty();
    }

    @Override
    public void onChanged(Change<? extends T> c) {
        syncComboItemsToChoices();
    }

    private void syncComboItemsToChoices() {
        // We need to clear the existing selection
        var selected = getProperty().getValue();
        var comboItems = getEditor().getItems();
        getEditor().getSelectionModel().clearSelection();
        comboItems.setAll(choices);
        setValue((T)selected);
    }

}