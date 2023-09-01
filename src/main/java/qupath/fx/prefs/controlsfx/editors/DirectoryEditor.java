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

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import qupath.fx.dialogs.FileChoosers;
import qupath.fx.prefs.controlsfx.items.DirectoryPropertyItem;

import java.io.File;

/**
 * Editor for selecting directory paths.
 *
 * Appears as a text field that can be double-clicked to launch a directory chooser.
 */
public class DirectoryEditor extends AbstractPropertyEditor<File, TextField> {

    private ObservableValue<File> value;

    public DirectoryEditor(PropertySheet.Item property, TextField control) {
        super(property, control, true);
        control.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                e.consume();
                File dirNew = FileChoosers.promptForDirectory(control.getScene().getWindow(), null, getValue());
                if (dirNew != null)
                    setValue(dirNew);
            }
        });
        if (property.getDescription() != null) {
            var description = property.getDescription();
            var tooltip = new Tooltip(description);
            tooltip.setShowDuration(Duration.millis(10_000));
            control.setTooltip(tooltip);
        }

        // Bind to the text property
        if (property instanceof DirectoryPropertyItem) {
            var prop = property.getObservableValue().orElse(null);
            if (prop instanceof Property)
                control.textProperty().bindBidirectional((Property<String>)prop);
        }
        value = Bindings.createObjectBinding(() -> {
            String text = control.getText();
            if (text == null || text.trim().isEmpty() || !new File(text).isDirectory())
                return null;
            else
                return new File(text);
        }, control.textProperty());
    }

    @Override
    public void setValue(File value) {
        getEditor().setText(value == null ? null : value.getAbsolutePath());
    }

    @Override
    protected ObservableValue<File> getObservableValue() {
        return value;
    }

}