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
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import qupath.fx.dialogs.FileChoosers;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.controlsfx.items.FilePropertyItem;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Abstract editor for selecting file or directory paths.
 *
 * Appears as a text field with associated button to launch a chooser.
 */
abstract class AbstractFileEditor extends AbstractPropertyEditor<File, DirectoryEditor.FileChoiceControl> {

    AbstractFileEditor(PropertySheet.Item property, Collection<? extends FileChooser.ExtensionFilter> filters) {
        super(property, new FileChoiceControl(filters), true);
        init();
    }

    AbstractFileEditor(PropertySheet.Item property) {
        super(property, new DirectoryEditor.DirectoryChoiceControl(), true);
        init();
    }

    private void init() {
        configureTooltip();
        // TODO: Consider using listeners & paying attention to focus to avoid repeated calls
        //       when the user is entering text, but it is not yet a valid file or directory
        if (getProperty() instanceof FilePropertyItem prop) {
            getEditor().getTextField().textProperty().bindBidirectional(prop.getFilePathProperty());
        }
    }

    private void configureTooltip() {
        var control = getEditor();
        var property = getProperty();
        if (property.getDescription() != null) {
            var description = property.getDescription();
            var tooltip = new Tooltip(description);
            tooltip.setShowDuration(Duration.millis(10_000));
            Tooltip.install(control, tooltip);
        }
    }

    @Override
    public void setValue(File value) {
        getEditor().getTextField().setText(value == null ? null : value.getAbsolutePath());
    }

    @Override
    protected ObservableValue<File> getObservableValue() {
        return getEditor().getObservableFile();
    }

    /**
     * Simple control to input a file path, or choose from a file chooser.
     */
    public static class FileChoiceControl extends HBox {

        static LocalizedResourceManager resources = LocalizedResourceManager.createInstance("qupath.fx.localization.strings");
        private TextField textField = new TextField();
        private Button button = new Button();
        private ObservableValue<File> value;

        private FileChooser.ExtensionFilter[] extensionFilters;

        private FileChoiceControl(Collection<? extends FileChooser.ExtensionFilter> extensionFilters) {
            this.extensionFilters = extensionFilters == null ? new FileChooser.ExtensionFilter[0] :
                    extensionFilters.toArray(FileChooser.ExtensionFilter[]::new);
            button = createButton();
            textField = createTextField();
            configureButton(button);
            configureTextField(textField);
            setSpacing(5.0);
            getChildren().addAll(textField, button);
        }

        private Button createButton() {
            var button = new Button();
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            button.setGraphic(createIcon(12));
            return button;
        }

        /**
         * Configure the button by setting the tooltip and action.
         */
        protected void configureButton(Button button) {
            var tooltip = new Tooltip();
            tooltip.textProperty().bind(
                    resources.createProperty("chooseFile")
            );
            button.setOnAction(e -> {
                File dirNew = FileChoosers.promptForFile(button.getScene().getWindow(), null, extensionFilters);
                if (dirNew != null)
                    textField.textProperty().set(dirNew.getAbsolutePath());
                e.consume();
            });
        }

        private TextField createTextField() {
            var textField = new TextField();
            textField.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textField, Priority.ALWAYS);
            return textField;
        }

        private void configureTextField(TextField textField) {
            // Bind to the text property
            var textProperty = textField.textProperty();
            value = Bindings.createObjectBinding(() -> textToFile(textField.getText()), textProperty);
        }

        /**
         * Convert the context of the text field to a valid file, or null if no valid value is possible.
         *
         * @param text
         * @return
         */
        protected File textToFile(String text) {
            if (text == null || text.trim().isEmpty() || !new File(text).isFile())
                return null;
            else
                return new File(text);
        }

        ObservableValue<File> getObservableFile() {
            return value;
        }

        /**
         * Get the text field to edit the directory path.
         *
         * @return
         */
        public TextField getTextField() {
            return textField;
        }

        /**
         * Get the button to open a directory chooser.
         *
         * @return
         */
        public Button getButton() {
            return button;
        }

        /**
         * Get the glyph to display in the button.
         * @return
         */
        protected FontAwesome.Glyph getButtonGlyph() {
            return FontAwesome.Glyph.FILE_ALT;
        }

        private Node createIcon(int size) {
            var font = GlyphFontRegistry.font("FontAwesome");
            Glyph g = font.create(getButtonGlyph()).size(size);
            g.setAlignment(Pos.CENTER);
            g.setContentDisplay(ContentDisplay.CENTER);
            g.setTextAlignment(TextAlignment.CENTER);
            g.setStyle("-fx-text-fill: -fx-text-base-color;");
            return g;
        }

    }

    public static class DirectoryChoiceControl extends FileChoiceControl {

        private DirectoryChoiceControl() {
            super(Collections.emptyList());
        }

        @Override
        protected File textToFile(String text) {
            if (text == null || text.trim().isEmpty() || !new File(text).isDirectory())
                return null;
            else
                return new File(text);
        }

        @Override
        protected FontAwesome.Glyph getButtonGlyph() {
            return FontAwesome.Glyph.FOLDER_OPEN_ALT;
        }

        @Override
        protected void configureButton(Button button) {
            var tooltip = new Tooltip();
            tooltip.textProperty().bind(
                    resources.createProperty("chooseDirectory")
            );
            button.setOnAction(e -> {
                File dirNew = FileChoosers.promptForDirectory(button.getScene().getWindow(), null, getObservableFile().getValue());
                if (dirNew != null)
                    getTextField().textProperty().set(dirNew.getAbsolutePath());
                e.consume();
            });
        }

    }

}
