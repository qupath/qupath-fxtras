/**
 * Copyright 2023, 2025 The University of Edinburgh
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

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.utils.FXUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Control to display mouse and keyboard input when interacting with a window.
 * <p>
 * This is useful for demos and tutorials where shortcut keys are used.
 *
 * @author Pete Bankhead
 */
public class InputDisplay implements EventHandler<InputEvent> {

	private static final Logger logger = LoggerFactory.getLogger(InputDisplay.class);

    // Needed to determine which symbols to use
    private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");

	// Owner window (for stage positioning)
	private final Window owner;

	// All windows to listen to
	private final ObservableList<? extends Window> allWindows;

	private final BooleanProperty showProperty = new SimpleBooleanProperty(false);

	private final BooleanProperty showCloseButton = new SimpleBooleanProperty(true);

	private Stage stage;

	private final FocusListener focusListener = new FocusListener();
	private final KeyFilter keyFilter = new KeyFilter();
	private final MouseFilter mouseFilter = new MouseFilter();
	private final ScrollFilter scrollFilter = new ScrollFilter();

	private static final String inputDisplayClass = "input-display-pane";
	private static final String closeItemClass = "close-item";
	private static final String mouseItemClass = "mouse-item";
	private static final PseudoClass pseudoClassActive = PseudoClass.getPseudoClass("active");

	// Main modifier keys
	private static final Set<KeyCode> MODIFIER_KEYS = Set.of(
			KeyCode.SHIFT,
            KeyCode.SHORTCUT,
            KeyCode.COMMAND,
            KeyCode.CONTROL,
            KeyCode.ALT,
            KeyCode.ALT_GRAPH,
            KeyCode.WINDOWS
	);

    // Buttons
	private final BooleanProperty primaryDown = new SimpleBooleanProperty(false);
	private final BooleanProperty secondaryDown = new SimpleBooleanProperty(false);
	private final BooleanProperty middleDown = new SimpleBooleanProperty(false);

	// Scroll/wheel
	private final BooleanProperty scrollLeft = new SimpleBooleanProperty(false);
	private final BooleanProperty scrollRight = new SimpleBooleanProperty(false);
	private final BooleanProperty scrollUp = new SimpleBooleanProperty(false);
	private final BooleanProperty scrollDown = new SimpleBooleanProperty(false);

    // Optionally skip showing keypresses within text fields, text areas etc.
    private final BooleanProperty skipTextInputControls = new SimpleBooleanProperty(false);

    // Duration of fade effect
    private final ObjectProperty<Duration> fadeDurationProperty = new SimpleObjectProperty<>(Duration.seconds(5.0));

    // Final opacity after fade (0 to disappear)
    private final DoubleProperty fadeToProperty = new SimpleDoubleProperty(0.1);

    // Optionally show symbols (Mac) or short forms (Windows, Linux) for modifier keys
    private final BooleanProperty showSymbolsProperty = new SimpleBooleanProperty(true);

	/**
	 * Create an input display with the specified owner window.
	 * @param owner the owner used to position the input display, and when listening to input events.
	 *              If null, an input display is created to listen to all windows but without any owner.
	 */
	public InputDisplay(Window owner) {
		this(owner, owner == null ? Window.getWindows() : FXCollections.observableArrayList(owner));
	}

	/**
	 * Create an input display with the specified owner window and list of windows to listen to.
	 * To listen to input across all windows, use {@link Window#getWindows()}.
	 * @param owner the owner used to position the input display.
	 * @param windows the windows to listen to.
	 */
	public InputDisplay(Window owner, ObservableList<? extends Window> windows) {
		Objects.requireNonNull(windows, "An observable list of windows must be specified!");
		this.owner = owner;
		this.allWindows = windows;
		showProperty.addListener((v, o, n) -> updateShowStatus(n));
		for (var window : allWindows)
			addListenersToWindow(window);
		allWindows.addListener(this::handleWindowListChange);
	}

	private void handleWindowListChange(ListChangeListener.Change<? extends Window> change) {
		while (change.next()) {
			for (var window : change.getRemoved()) {
				removeListenersFromWindow(window);
			}
			for (var window : change.getAddedSubList()) {
				addListenersToWindow(window);
			}
		}
	}

	private void addListenersToWindow(Window window) {
		window.addEventFilter(InputEvent.ANY, this);
		window.focusedProperty().addListener(focusListener);
	}

	private void removeListenersFromWindow(Window window) {
		window.focusedProperty().removeListener(focusListener);
		window.removeEventFilter(InputEvent.ANY, this);
	}

	private void updateShowStatus(boolean doShow) {
		if (doShow) {
			if (stage == null) {
				stage = createStage();
				stage.initOwner(owner);
			}
			stage.setAlwaysOnTop(true);
            keyFilter.updateCapsLock();
			stage.show();
			stage.setOnCloseRequest(e -> {
				showProperty.set(false);
			});
		} else if (stage != null) {
			if (stage.isShowing())
				stage.hide();
			stage.hide();
		}
	}

	private Stage createStage() {

		logger.trace("Creating stage for input display");

		double keyPaneWidth = 225.0;
		double mousePaneWidth = 120;
		double spacing = 5;

		var pane = new AnchorPane();
		var stylesheetUrl = InputDisplay.class.getResource("/css/input-display.css").toExternalForm();
		pane.getStylesheets().add(stylesheetUrl);
		pane.getStyleClass().add(inputDisplayClass);

		// Add to main pane
		var paneKeys = createKeyPane(keyPaneWidth);
		pane.getChildren().add(paneKeys);
		AnchorPane.setTopAnchor(paneKeys, 0.0);
		AnchorPane.setLeftAnchor(paneKeys, 0.0);

		// Create the mouse pane
		var paneMouse = createMousePane(mousePaneWidth);

		pane.getChildren().add(paneMouse);
		AnchorPane.setTopAnchor(paneMouse, 0.0);
		AnchorPane.setLeftAnchor(paneMouse, keyPaneWidth + 5);


		// Add small node to close
		var closeImage = new SVGPath();
		closeImage.setContent("M 0 0 L 8 8 M 8 0 L 0 8");
		closeImage.getStyleClass().add(closeItemClass);
		var closeButton = new BorderPane(closeImage);
		closeButton.setOnMouseEntered( e -> closeImage.pseudoClassStateChanged(pseudoClassActive, true));
		closeButton.setOnMouseExited( e -> closeImage.pseudoClassStateChanged(pseudoClassActive, false));
		closeButton.setOnMouseClicked(e -> showProperty.set(false));
		closeButton.setCursor(Cursor.DEFAULT);
		pane.getChildren().add(closeButton);
		AnchorPane.setTopAnchor(closeButton, 7.0);
		AnchorPane.setLeftAnchor(closeButton, 7.0);
		closeButton.visibleProperty().bind(showCloseButton);


		// Set default location as the bottom left corner of the screen
		Screen screen = owner == null ? Screen.getPrimary() : FXUtils.getScreenOrPrimary(owner);
		var screenBounds = screen.getVisualBounds();
		double xPad = 10;
		double yPad = 10;

		// Create primary stage for display
		stage = new Stage();
		stage.initStyle(StageStyle.TRANSPARENT);
		var scene = new Scene(pane, keyPaneWidth + mousePaneWidth + spacing, 160, Color.TRANSPARENT);
		stage.setScene(scene);
		FXUtils.makeDraggableStage(stage);


		var tooltipClose = new Tooltip("Display input - double-click to close");
		Tooltip.install(pane, tooltipClose);

		// Locate at bottom left of the screen
		stage.setX(screenBounds.getMinX() + xPad);
		stage.setY(screenBounds.getMaxY() - scene.getHeight() - yPad);

		stage.getScene().setOnMouseClicked(e -> {
			if (!showCloseButton.get() && e.getClickCount() == 2) {
				hide();
			}
		});

		return stage;
	}

    private void bindFadingText(Label label, ObservableValue<String> text) {
        var fade = new FadeTransition();
        fade.durationProperty().bind(fadeDurationProperty);
        fade.setFromValue(1.0);
        fade.toValueProperty().bind(fadeToProperty);
        fade.setInterpolator(Interpolator.EASE_OUT);
        fade.setNode(label);
        String capsLock = keyFilter.getText(KeyCode.CAPS);
        text.addListener((v, o, n) -> {
            if (n == null || n.isEmpty()) {
                if (capsLock.equals(o)) {
                    // We want to handle caps lock immediately
                    label.setOpacity(fade.getToValue());
                } else {
                    fade.playFromStart();
                }
            } else {
                fade.stop();
                label.setText(n);
                label.setOpacity(fade.getFromValue());
            }
        });
    }

    /**
     * Whether the input display is currently showing or not.
     * @return
     */
	public BooleanProperty showProperty() {
		return showProperty;
	}

    /**
     * Show the input display.
     */
	public void show() {
		showProperty.set(true);
	}

    /**
     * Hide the input display.
     */
	public void hide() {
		showProperty.set(false);
	}

    /**
     * Property to control whether keypresses are displayed when typing is performed
     * within a text input control (e.g. text field, text area).
     * @return the property
     */
    public BooleanProperty skipTextInputControlsProperty() {
        return skipTextInputControls;
    }

    /**
     * Set the value of {@link #skipTextInputControlsProperty()}.
     * @param skip true to skip key presses within input controls, false to display them
     */
    public void setSkipTextInputControls(boolean skip) {
        skipTextInputControls.set(skip);
    }

    /**
     * Get the value of {@link #skipTextInputControlsProperty()}.
     * @return
     */
    public boolean getSkipTextInputControls() {
        return skipTextInputControls.get();
    }

    /**
     * Property to control whether modifier keys are shown using symbols or short forms, where available.
     * @return the property
     */
    public BooleanProperty showSymbolsProperty() {
        return showSymbolsProperty;
    }

    /**
     * Set the value of {@link #showSymbolsProperty()}.
     * @param showSymbols true to show symbols where available, false to display text names
     */
    public void setShowSymbols(boolean showSymbols) {
        showSymbolsProperty.set(showSymbols);
    }

    /**
     * Get the value of {@link #showSymbolsProperty()}.
     * @return true if symbols should be shown, false otherwise
     */
    public boolean getShowSymbols() {
        return showSymbolsProperty.get();
    }

    /**
     * Property to control how quickly key presses fade after the keys have been released.
     * @return the property
     */
    public ObjectProperty<Duration> fadeDurationProperty() {
        return fadeDurationProperty;
    }

    /**
     * Set the value of {@link #fadeDurationProperty()}.
     * @param duration) the new fade duration to use
     */
    public void setFadeDuration(Duration duration) {
        fadeDurationProperty.set(duration);
    }

    /**
     * Get the value of {@link #fadeDurationProperty()}.
     * @return the current fade duration
     */
    public Duration getFadeDuration() {
        return fadeDurationProperty.get();
    }

    /**
     * Property to control the final opacity after fading out keypresses.
     * This should be between 0 (completely transparent) and 1 (completely opaque).
     * It can be used to control whether keypresses remain visible indefinitely after the key has been released.
     * @return the property
     */
    public DoubleProperty fadeToProperty() {
        return fadeToProperty;
    }

    /**
     * Set the value of {@link #fadeToProperty()}.
     * @param opacity) the new fade opacity to use
     */
    public void setFadeTo(double opacity) {
        fadeToProperty.set(opacity);
    }

    /**
     * Get the value of {@link #fadeToProperty()}.
     * @return the current fade to opacity value
     */
    public double getFadeTo() {
        return fadeToProperty.get();
    }


	@Override
	public void handle(InputEvent event) {
		// Return quickly if not showing
		if (!showProperty.get())
			return;
		// Handle according to event type
		if (event instanceof KeyEvent keyEvent)
			keyFilter.handle(keyEvent);
		else if (event instanceof MouseEvent mouseEvent)
			mouseFilter.handle(mouseEvent);
		else if (event instanceof ScrollEvent scrollEvent)
			scrollFilter.handle(scrollEvent);
	}


	class FocusListener implements ChangeListener<Boolean> {

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!showProperty.get())
				return;
			if (!newValue) {
				primaryDown.set(false);
				secondaryDown.set(false);
				middleDown.set(false);
				scrollLeft.set(false);
				scrollRight.set(false);
				scrollUp.set(false);
				scrollDown.set(false);
			}
		}
	}


	Pane createKeyPane(double width) {
		// Create labels for displaying keyboard info
		var labModifiers = new Label("");
		var labKeys = new Label("");
        labKeys.setWrapText(true);

        labModifiers.setPrefSize(width, 50);
        labKeys.setPrefSize(width, 50);
        labKeys.setMaxHeight(200);
        labModifiers.setAlignment(Pos.CENTER);
        labKeys.setAlignment(Pos.CENTER);
        labModifiers.getStyleClass().add("modifiers");
        labKeys.getStyleClass().add("keys");

        bindFadingText(labModifiers, keyFilter.modifierText());
        bindFadingText(labKeys,  keyFilter.keyText());

		// Create pane for displaying keyboard info
		var paneKeys = new GridPane();
		paneKeys.add(labModifiers, 0, 0);
		paneKeys.add(labKeys, 0, 1);
		return paneKeys;
	}


	Pane createMousePane(double width) {
		var pane = new AnchorPane();

		var rectPrimary = createButtonRectangle(25, 40);
		var rectSecondary = createButtonRectangle(25, 40);
		var rectMiddle = createButtonRectangle(8, 18);

		double gap = 5;
		rectMiddle.setTranslateX(rectPrimary.getWidth() + gap/2.0 - rectMiddle.getWidth()/2.0);

		rectSecondary.setTranslateX(rectPrimary.getWidth()+gap);

		rectMiddle.setStrokeWidth(8);
		rectMiddle.setStroke(Color.WHITE);
		rectMiddle.setTranslateY((rectPrimary.getHeight()-rectMiddle.getHeight())/2.0);
		var shapePrimary = Shape.subtract(rectPrimary, rectMiddle);
		shapePrimary.getStyleClass().setAll(rectPrimary.getStyleClass());
		var shapeSecondary = Shape.subtract(rectSecondary, rectMiddle);
		shapeSecondary.getStyleClass().setAll(rectSecondary.getStyleClass());
		rectMiddle.setStroke(null);
		rectMiddle.setStrokeWidth(2);

		primaryDown.addListener((v, o, n) -> shapePrimary.pseudoClassStateChanged(pseudoClassActive, n));
		secondaryDown.addListener((v, o, n) -> shapeSecondary.pseudoClassStateChanged(pseudoClassActive, n));
		middleDown.addListener((v, o, n) -> rectMiddle.pseudoClassStateChanged(pseudoClassActive, n));

		var group = new Group();
		group.getChildren().addAll(shapePrimary, shapeSecondary, rectMiddle);

		double arrowBase = 32;
		double arrowHeight = arrowBase / 2.0;

		var arrowUp = createArrow(arrowBase, arrowHeight, 0);
		var arrowDown = createArrow(arrowBase, arrowHeight, 180);
		var arrowLeft = createArrow(arrowBase, arrowHeight, -90);
		var arrowRight = createArrow(arrowBase, arrowHeight, 90);

		scrollUp.addListener((v, o, n) -> arrowUp.pseudoClassStateChanged(pseudoClassActive, n));
		scrollDown.addListener((v, o, n) -> arrowDown.pseudoClassStateChanged(pseudoClassActive, n));
		scrollLeft.addListener((v, o, n) -> arrowLeft.pseudoClassStateChanged(pseudoClassActive, n));
		scrollRight.addListener((v, o, n) -> arrowRight.pseudoClassStateChanged(pseudoClassActive, n));

		pane.getChildren().addAll(
				group,
				arrowUp, arrowDown, arrowLeft, arrowRight
		);

		AnchorPane.setTopAnchor(group, 20.0);
		AnchorPane.setLeftAnchor(group, width/2.0-group.getBoundsInLocal().getWidth()/2.0);

		double y = rectPrimary.getHeight() + 30;
		AnchorPane.setTopAnchor(arrowUp, y);
		AnchorPane.setTopAnchor(arrowDown, y + 60);
		AnchorPane.setTopAnchor(arrowLeft, y + 30);
		AnchorPane.setTopAnchor(arrowRight, y + 30);

		AnchorPane.setLeftAnchor(arrowUp, width/2.0-arrowBase/2.0);
		AnchorPane.setLeftAnchor(arrowDown, width/2.0-arrowBase/2.0);
		AnchorPane.setLeftAnchor(arrowLeft, width/2.0-arrowBase/2.0-arrowBase);
		AnchorPane.setLeftAnchor(arrowRight, width/2.0+arrowBase/2.0);

		return pane;
	}

	Rectangle createButtonRectangle(double width, double height) {
		var rect = new Rectangle(width, height);
		rect.setArcHeight(8);
		rect.setArcWidth(8);
		rect.setStrokeWidth(2);
		rect.getStyleClass().add(mouseItemClass);
		return rect;
	}




	Polygon createArrow(double arrowBase, double arrowHeight, double rotate) {
		var arrow = new Polygon(
				-arrowBase/2.0, arrowHeight/2.0, 0, -arrowHeight/2.0, arrowBase/2.0, arrowHeight/2.0
		);
		arrow.setStrokeWidth(2);
		arrow.setRotate(rotate);
		arrow.getStyleClass().add(mouseItemClass);
		return arrow;
	}



	/**
	 * Handler to log & display key events.
	 * This separates specific modifiers from other keys,
	 * and maintains a 'last shortcut' reference in case a key
	 * is pressed too quickly to catch what happened.
	 */
    private class KeyFilter implements EventHandler<KeyEvent> {

        private final Map<KeyCode, String> currentKeys = new LinkedHashMap<>();
        private final Set<KeyCode> currentModifiers = new TreeSet<>();

        // Holding down a key can result in repeated 'released' then 'pressed' events -
        // so we want to delay removal to avoid responding too eagerly.
        private final Set<KeyCode> removePending = new TreeSet<>();

        private final StringProperty modifierText = new SimpleStringProperty();
        private final StringProperty keyText = new SimpleStringProperty();

		@Override
		public void handle(KeyEvent event) {
            var code = event.getCode();
            if (code == null)
                return;

            if (skipTextInputControls.get() && event.getTarget() instanceof TextInputControl) {
                logger.trace("Skipping text input to {}", event.getTarget());
                return;
            }

            if (code == KeyCode.CAPS) {
                updateCapsLock();
            } else {
                // Handle anything that isn't caps lock according to pressed or released event
                boolean isModifier = MODIFIER_KEYS.contains(code);
                if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                    if (isModifier) {
                        currentModifiers.add(code);
                        updateModifierText();
                    } else {
                        // Inconveniently, we can't get a reliable text representation from the keycode
                        currentKeys.put(code, getText(event));
                        updateModifierText();
                        updateKeyText();
                    }
                    // If we have a not-yet-processed remove request, make sure that doesn't happen
                    removePending.remove(code);
                } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                    if (removePending.add(code)) {
                        // Delay removal in case the key is held down and will be immediately added again
                        Platform.runLater(this::handleRemove);
                    }
                }
            }
        }

        private void updateCapsLock() {
            // We can Handle caps lock by directly querying the key status
            boolean changed = false;
            if (Platform.isKeyLocked(KeyCode.CAPS).orElse(Boolean.FALSE)) {
                changed = changed | currentModifiers.add(KeyCode.CAPS);
            } else {
                changed = changed | currentModifiers.remove(KeyCode.CAPS);
            }
            if (changed)
                updateModifierText();
        }

        private void handleRemove() {
            boolean keysChanged = false;
            boolean modifiersChanged = false;
            for (var toRemove : removePending) {
                modifiersChanged = currentModifiers.remove(toRemove) | modifiersChanged;
                keysChanged = currentKeys.remove(toRemove) != null | keysChanged;
            }
            if (currentModifiers.isEmpty()) {
                updateModifierText();
            }
            if (keysChanged) {
                updateKeyText();
            }
        }

        private String getText(KeyEvent event) {
            String text = event.getText();
            if (event.getCode().isLetterKey())
                return event.getCode().getName().toUpperCase();
            if (text.trim().isEmpty())
                return getText(event.getCode());
            return text;
        }


        private String getText(KeyCode code) {
            if (code.isLetterKey())
                return code.getName().toUpperCase();
            else {
                if (showSymbolsProperty.get()) {
                    var symbol = getSymbol(code);
                    if (symbol != null)
                        return symbol;
                }
                if (code.isModifierKey() || code.getChar().isBlank() || code.isFunctionKey())
                    return code.getName();
                else
                    return code.getChar();
            }
        }

        private static String getSymbol(KeyCode code) {
            return switch (code) {
                case SHIFT -> isMac ? "⇧" : "Shift";
                case CONTROL -> isMac ? "⌃" : "Ctrl";
                case SHORTCUT -> isMac ? "⌘" : "Ctrl";
                case META -> isMac ? "⌘" : "Meta";
                case COMMAND -> isMac ? "⌘" : "Cmd";
                case ALT -> isMac ? "⌥" : "Alt";
                case ENTER -> "⏎";
                case BACK_SPACE -> isMac ? "⌫" : "Backspace";
                case LEFT -> "←";
                case RIGHT -> "→";
                case UP -> "↑";
                case DOWN -> "↓";
                case TAB ->  isMac ? "⇥" : "Tab";
                case SPACE -> isMac ? "␣" : "Space";
                case ESCAPE -> "Esc";
                case WINDOWS -> "Win";
                case CAPS -> "⇪";
                case HOME -> "⇱";
                case END -> "⇲";
                case PAGE_UP -> "⇞";
                case PAGE_DOWN -> "⇟";
                case NUM_LOCK -> "Num lock";
                default -> null;
            };
        }

        private void updateKeyText() {
            keyText.set(String.join("+", currentKeys.values()));
        }

        private void updateModifierText() {
            if (currentModifiers.isEmpty())
                modifierText.set("");
            else {
                // Concatenate with + if we aren't showing Mac symbols
                String delim = isMac && showSymbolsProperty.get() ? "" : "+";
                modifierText.set(currentModifiers.stream().map(this::getText).collect(Collectors.joining(delim)));
            }
        }

        ReadOnlyStringProperty keyText() {
            return keyText;
        }

        ReadOnlyStringProperty modifierText() {
            return modifierText;
        }

	}


    private class MouseFilter implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent event) {
			var type = event.getEventType();
			if (type == MouseEvent.MOUSE_PRESSED) {
				if (event.getButton() == MouseButton.PRIMARY)
					primaryDown.set(true);
				else if (event.getButton() == MouseButton.SECONDARY)
					secondaryDown.set(true);
				else if (event.getButton() == MouseButton.MIDDLE)
					middleDown.set(true);
			} else if (type == MouseEvent.MOUSE_RELEASED) {
				if (event.getButton() == MouseButton.PRIMARY)
					primaryDown.set(false);
				else if (event.getButton() == MouseButton.SECONDARY)
					secondaryDown.set(false);
				else if (event.getButton() == MouseButton.MIDDLE)
					middleDown.set(false);
			}
		}

	}


    private class ScrollFilter implements EventHandler<ScrollEvent> {

		@Override
		public void handle(ScrollEvent event) {
			var type = event.getEventType();
			if (type == ScrollEvent.SCROLL_STARTED || type == ScrollEvent.SCROLL) {
				if (event.isInertia()) {
					scrollUp.set(false);
					scrollDown.set(false);
					scrollLeft.set(false);
					scrollRight.set(false);
					return;
				}
				// Previously, there was support for 'invert scrolling' to do with different
				// macOS behavior. This isn't used anymore, but the code is left in case it's needed again.
				boolean invertScrolling = false;
				double direction = invertScrolling ? -1 : 1;
				scrollUp.set((event.getDeltaY() * direction) < -0.001);
				scrollDown.set((event.getDeltaY() * direction) > 0.001);
				scrollLeft.set((event.getDeltaX() * direction) < -0.001);
				scrollRight.set((event.getDeltaX() * direction) > 0.001);
			} else if (type == ScrollEvent.SCROLL_FINISHED) {
				scrollUp.set(false);
				scrollDown.set(false);
				scrollLeft.set(false);
				scrollRight.set(false);
			}
		}

	}

}
