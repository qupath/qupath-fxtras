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

package qupath.fx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A text field control for creating a text-based predicate.
 * <p>
 * The main purpose is to allow filtering of a list of objects based on user-defined text,
 * optionally using regular expressions.
 *
 * @param <T>
 */
public class PredicateTextField<T> extends HBox {

    private static final Logger logger = LoggerFactory.getLogger(PredicateTextField.class);

    private final ObjectProperty<Function<T, String>> stringFunction = new SimpleObjectProperty<>();

    private final TextField tfFilter = new TextField("");

    private final StringProperty filterText = tfFilter.textProperty();
    private final BooleanProperty useRegex = new SimpleBooleanProperty(false);
    private final BooleanProperty ignoreCase = new SimpleBooleanProperty(true);

    private final BooleanProperty showRegexButton = new SimpleBooleanProperty(true);

    private final ObjectBinding<Predicate<T>> predicate = createPredicateBinding(filterText);
    private ReadOnlyObjectWrapper<Predicate<T>> predicateWrapper = new ReadOnlyObjectWrapper<>();

    /**
     * Constructor to create a new predicate text field using the default {@link Objects#toString(Object)}
     * method to create a string representation of the object.
     */
    public PredicateTextField() {
        this(Objects::toString);
    }

    /**
     * Constructor to create a new predicate text field with a custom string function.
     * @param stringFunction
     */
    public PredicateTextField(Function<T, String> stringFunction) {
        this.stringFunction.set(stringFunction);
        initialize();
        predicateWrapper.bind(predicate);
    }

    private void initialize() {
        ToggleButton btnRegex = new ToggleButton(".*");
        btnRegex.setTooltip(new Tooltip("Use regular expressions"));
        btnRegex.selectedProperty().bindBidirectional(useRegex);

        HBox.setHgrow(tfFilter, Priority.ALWAYS);
        tfFilter.setMaxWidth(Double.MAX_VALUE);
        getChildren().add(tfFilter);
        if (showRegexButton.get())
            getChildren().add(btnRegex);
        showRegexButton.addListener((v, o, n) -> {
            if (n)
                getChildren().add(btnRegex);
            else
                getChildren().remove(btnRegex);
        });
    }

    /**
     * Get the filter text property.
     * @return
     */
    public StringProperty textProperty() {
        return filterText;
    }

    /**
     * Get the filter text.
     * @return
     */
    public String getText() {
        return filterText.get();
    }

    /**
     * Set the filter text.
     * @param text
     */
    public void setText(String text) {
        filterText.set(text);
    }

    /**
     * Get the prompt text to show in the text field.
     * @return
     */
    public StringProperty promptTextProperty() {
        return tfFilter.promptTextProperty();
    }

    /**
     * Get the prompt text to show when the text field is empty.
     * @return
     */
    public String getPromptText() {
        return promptTextProperty().get();
    }

    /**
     * Set the prompt text to show when the text field is empty.
     * @param text
     */
    public void setPromptText(String text) {
        promptTextProperty().set(text);
    }

    /**
     * Property determining whether the filter should use regular expressions or not.
     * If not, then a simple 'contains' test is performed using the filter text.
     * @return
     */
    public BooleanProperty useRegexProperty() {
        return useRegex;
    }

    /**
     * Set whether the filter should use regular expressions or not.
     * @param use
     */
    public void setUseRegex(boolean use) {
        this.useRegex.set(use);
    }

    /**
     * Get whether the filter should use regular expressions or not.
     * The default is false.
     * @return
     */
    public boolean getUseRegex() {
        return this.useRegex.get();
    }

    /**
     * Property determining whether the a button enabling the use of regular expressions
     * should be shown or not. The display of the button can be controlled independently
     * of whether regular expressions are actually used.
     * @return
     */
    public BooleanProperty showRegexButtonProperty() {
        return showRegexButton;
    }

    /**
     * Set whether a button should be shown that enables the user to toggle the use of
     * regular expressions.
     * @param show
     */
    public void setShowRegexButton(boolean show) {
        this.showRegexButton.set(show);
    }

    /**
     * Get whether a button should be shown that enables the user to toggle the use of
     *      * regular expressions.
     * @return
     */
    public boolean setShowRegexButton() {
        return this.showRegexButton.get();
    }

    /**
     * Property determining whether the filter should ignore case or not.
     * This is only relevant when not using regular expressions.
     * @return
     */
    public BooleanProperty ignoreCaseProperty() {
        return ignoreCase;
    }

    /**
     * Set whether the filter should ignore case or not.
     * This is only relevant when not using regular expressions.
     * @param ignoreCase
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase.set(ignoreCase);
    }

    /**
     * Get whether the filter should ignore case or not.
     * This is only relevant when not using regular expressions.
     * The default is true.
     * @return
     */
    public boolean getIgnoreCase() {
        return this.ignoreCase.get();
    }

    /**
     * Read only property representing the predicate.
     * @return
     */
    public ReadOnlyObjectProperty<Predicate<T>> predicateProperty() {
        return predicateWrapper.getReadOnlyProperty();
    }

    /**
     * Get the predicate.
     * @return
     */
    public Predicate<T> getPredicate() {
        return predicateWrapper.get();
    }

    /**
     * Get the string function property sed to create a string representation of the object.
     * @return
     */
    public ObjectProperty<Function<T, String>> stringFunctionProperty() {
        return stringFunction;
    }

    /**
     * Get the string function used to create a string representation of the object.
     * @return
     */
    public Function<T, String> getStringFunction() {
        return stringFunction.get();
    }

    /**
     * Set the string function used to create a string representation of the object.
     * @param stringFunction
     */
    public void setStringFunction(Function<T, String> stringFunction) {
        this.stringFunction.set(stringFunction);
    }

    private ObjectBinding<Predicate<T>> createPredicateBinding(StringProperty filterText) {
        return Bindings.createObjectBinding(() -> {
            if (useRegex.get())
                return createPredicateFromRegex(stringFunction.get(), filterText.get());
            else
                return createPredicateFromText(stringFunction.get(), filterText.get(), ignoreCase.get());
        }, filterText, useRegex, ignoreCase, stringFunction);
    }

    private static <T> Predicate<T> createPredicateFromRegex(Function<T, String> stringFunction, String regex) {
        if (regex == null || regex.isBlank())
            return info -> true;
        try {
            Pattern pattern = Pattern.compile(regex);
            return item -> checkItemAgainstRegex(stringFunction, item, pattern);
        } catch (PatternSyntaxException e) {
            logger.debug("Invalid regex: {} ({})", regex, e.getMessage());
            return item -> false;
        }
    }

    private static <T> boolean checkItemAgainstRegex(Function<T, String> stringFunction, T item, Pattern pattern) {
        return pattern.matcher(stringFunction.apply(item)).find();
    }

    private static <T> Predicate<T> createPredicateFromText(Function<T, String> stringFunction, String filterText,
                                                            boolean ignoreCase) {
        if (filterText == null || filterText.isBlank())
            return item -> true;
        if (ignoreCase) {
            String text = filterText.toLowerCase();
            return item -> stringFunction.apply(item).toLowerCase().contains(text);
        } else {
            String text = filterText;
            return item -> stringFunction.apply(item).contains(text);
        }
    }

}
