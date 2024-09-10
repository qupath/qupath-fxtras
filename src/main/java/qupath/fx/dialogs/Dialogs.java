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

package qupath.fx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.PopupWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import qupath.fx.utils.FXUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Collection of static methods to help with showing information to a user, 
 * as well as requesting some basic input.
 * <p>
 * In general, 'showABCMessage' produces a dialog box that requires input from the user.
 * By contrast, 'showABCNotification' shows a notification message that will disappear without user input -
 * but only <b>if</b> ControlsFX is available.
 * If ControlsFX is not available, the notification will be shown as a non-blocking dialog box.
 * <p>
 * <b>Important! </b>> It is recommended to call #setPrimaryWindow(Window) before using any of these methods,
 * to help ensure that the dialogs are shown in the correct place when an owner is not specified.
 *
 * @author Pete Bankhead
 *
 */
public class Dialogs {
	
	private static final Logger logger = LoggerFactory.getLogger(Dialogs.class);

	private static Window primaryWindow;

	private static ObservableList<String> knownExtensions = FXCollections.observableArrayList();

	private static boolean controlsFxMissing = false;

	/**
	 * Set the primary window, which will be used as the owner of dialogs
	 * if no other window takes precedence (e.g. because it is modal or in focus).
	 * @param window
	 * @see #getPrimaryWindow()
	 */
	public static void setPrimaryWindow(Window window) {
		primaryWindow = window;
	}

	/**
	 * Get the primary window.
	 * @return
	 * @see #setPrimaryWindow(Window)
	 */
	public static Window getPrimaryWindow() {
		return primaryWindow;
	}

	/**
	 * Get a modifiable list of known file extensions.
	 * This exists to make it possible to override the logic of 'everything after the
	 * last dot is the file extension', and support multi-part extensions such as
	 * {@code .tar.gz} or {@code .ome.tif}.
	 * @return
	 */
	public static ObservableList<String> getKnownFileExtensions() {
		return knownExtensions;
	}

	/**
	 * Show a confirm dialog (OK/Cancel).
	 * @param title
	 * @param text
	 * @return
	 */
	public static boolean showConfirmDialog(String title, String text) {
		return showConfirmDialog(title, createContentLabel(text));
	}
	
	/**
	 * Show a message dialog (OK button only), with the content contained within a Node.
	 * @param title
	 * @param node
	 * @return
	 */
	public static boolean showMessageDialog(final String title, final Node node) {
		return new Builder()
				.buttons(ButtonType.OK)
				.title(title)
				.content(node)
				.resizable()
				.showAndWait()
				.orElse(ButtonType.CANCEL) == ButtonType.OK;
	}
	
	/**
	 * Show a standard message dialog.
	 * @param title
	 * @param message
	 * @return 
	 */
	public static boolean showMessageDialog(String title, String message) {
		return showMessageDialog(title, createContentLabel(message));
	}
	
	/**
	 * Show a confirm dialog (OK/Cancel).
	 * @param title
	 * @param node
	 * @return
	 */
	public static boolean showConfirmDialog(String title, Node node) {
		return new Builder()
				.alertType(AlertType.CONFIRMATION)
				.buttons(ButtonType.OK, ButtonType.CANCEL)
				.title(title)
				.content(node)
				.resizable()
				.showAndWait()
				.orElse(ButtonType.CANCEL) == ButtonType.OK;
	}
	
	/**
	 * Show a Yes/No dialog.
	 * @param title
	 * @param text
	 * @return
	 */
	public static boolean showYesNoDialog(String title, String text) {
		return new Builder()
			.alertType(AlertType.NONE)
			.buttons(ButtonType.YES, ButtonType.NO)
			.title(title)
			.content(createContentLabel(text))
			.showAndWait()
			.orElse(ButtonType.NO) == ButtonType.YES;
	}
	
	/**
	 * Create a content label. This is patterned on the default behavior for {@link DialogPane} but 
	 * sets the min size to be the preferred size, which is necessary to avoid ellipsis when using long 
	 * Strings on Windows with scaling other than 100%.
	 * @param text
	 * @return
	 */
	private static Label createContentLabel(String text) {
		var label = new Label(text);
		label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
	}
	
	/**
	 * Show a Yes/No/Cancel dialog.
	 * @param title dialog box title
	 * @param text prompt message
	 * @return a {@link ButtonType} of YES, NO or CANCEL
	 */
	public static ButtonType showYesNoCancelDialog(String title, String text) {
		return new Builder()
				.alertType(AlertType.NONE)
				.buttons(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
				.title(title)
				.content(createContentLabel(text))
				.showAndWait()
				.orElse(ButtonType.CANCEL);
	}


	/**
	 * Show an input dialog requesting a numeric value. Only scientific notation and digits 
	 * with/without a decimal separator (e.g. ".") are permitted.
	 * <p>
	 * The returned value might still not be in a valid state, as 
	 * limited by {@link FXUtils#restrictTextFieldInputToNumber(javafx.scene.control.TextField, boolean)}.
	 * 
	 * @param title
	 * @param message
	 * @param initialInput
	 * @return Number input by the user, or NaN if no valid number was entered, or null if cancel was pressed.
	 * @see FXUtils#restrictTextFieldInputToNumber(javafx.scene.control.TextField, boolean)
	 */
	public static Double showInputDialog(final String title, final String message, final Double initialInput) {
		if (Platform.isFxApplicationThread()) {
			TextInputDialog dialog = new TextInputDialog(initialInput.toString());
			FXUtils.restrictTextFieldInputToNumber(dialog.getEditor(), true);
			dialog.setTitle(title);
			dialog.initOwner(getDefaultOwner());
			dialog.setHeaderText(null);
			dialog.setContentText(message);
			dialog.setResizable(true);
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				try {
					return Double.parseDouble(result.get());
				} catch (Exception e) {
					// Can still happen since the TextField restrictions allow intermediate (invalid) formats
					logger.error("Unable to parse numeric value from {}", result);
					return Double.NaN;
				}
			}
		} else
			return FXUtils.callOnApplicationThread(() -> showInputDialog(title, message, initialInput));
		return null;
	}
	
	/**
	 * Show an input dialog requesting a String input.
	 * 
	 * @param title
	 * @param message
	 * @param initialInput
	 * @return
	 */
	public static String showInputDialog(final String title, final String message, final String initialInput) {
		if (Platform.isFxApplicationThread()) {
			TextInputDialog dialog = new TextInputDialog(initialInput);
			dialog.setTitle(title);
			dialog.initOwner(getDefaultOwner());
			dialog.setHeaderText(null);
			dialog.setContentText(message);
			dialog.setResizable(true);
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent())
			    return result.get();
		} else {
			return FXUtils.callOnApplicationThread(() -> showInputDialog(title, message, initialInput));
		}
		return null;
	}
	
	/**
	 * Show a choice dialog with an array of choices (selection from ComboBox or similar).
	 * @param <T>
	 * @param title dialog title
	 * @param message dialog prompt
	 * @param choices array of available options
	 * @param defaultChoice initial selected option
	 * @return chosen option, or {@code null} if the user cancels the dialog
	 */
	public static <T> T showChoiceDialog(final String title, final String message, final T[] choices, final T defaultChoice) {
		return showChoiceDialog(title, message, Arrays.asList(choices), defaultChoice);
	}
	
	/**
	 * Show a choice dialog with a collection of choices (selection from ComboBox or similar).
	 * @param <T>
	 * @param title dialog title
	 * @param message dialog prompt
	 * @param choices list of available options
	 * @param defaultChoice initial selected option
	 * @return chosen option, or {@code null} if the user cancels the dialog
	 */
	public static <T> T showChoiceDialog(final String title, final String message, final Collection<T> choices, final T defaultChoice) {
		if (Platform.isFxApplicationThread()) {
			ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
			dialog.setTitle(title);
			dialog.initOwner(getDefaultOwner());
			dialog.getDialogPane().setHeaderText(null);
			if (message != null)
				dialog.getDialogPane().setContentText(message);
			Optional<T> result = dialog.showAndWait();
			return result.orElse(null);
		} else
			return FXUtils.callOnApplicationThread(() -> showChoiceDialog(title, message, choices, defaultChoice));
	}
	
	/**
	 * Show an error message, displaying the localized message of a {@link Throwable}.
	 * @param title
	 * @param e
	 */
	public static void showErrorMessage(final String title, final Throwable e) {
		String message = e.getLocalizedMessage();
		if (message == null)
			message = "This app has encountered a problem, sorry.\n\n" + e;
		showErrorMessage(title, message);
		if (isHeadless())
			logger.error(title, e);
	}
	
	/**
	 * Show an error notification, displaying the localized message of a {@link Throwable}.
	 * @param title
	 * @param e
	 */
	public static void showErrorNotification(final String title, final Throwable e) {
		String message = e.getLocalizedMessage();
		if (isHeadless()) {
			if (message != null && !message.isBlank() && !message.equals(title))
				logger.error(title + ": " + e.getLocalizedMessage(), e);
			else
				logger.error(title , e);
		} else {
			if (message == null || message.isBlank())
				message = "Unknown error (" + e.getClass().getSimpleName() + ")";
			showNotification(title, message, AlertType.ERROR);
		}
	}

	/**
	 * Show an error notification.
	 * @param title
	 * @param message
	 */
	public static void showErrorNotification(final String title, final String message) {
		if (isHeadless())
			logger.error(title + ": " + message);
		else
			showNotification(title, message, AlertType.ERROR);
	}

	/**
	 * Show a warning notification.
	 * @param title
	 * @param message
	 */
	public static void showWarningNotification(final String title, final String message) {
		if (isHeadless())
			logger.warn(title + ": " + message);
		else
			showNotification(title, message, AlertType.WARNING);
	}

	/**
	 * Show an info notification.
	 * @param title
	 * @param message
	 */
	public static void showInfoNotification(final String title, final String message) {
		if (isHeadless())
			logger.info(title + ": " + message);
		else
			showNotification(title, message, AlertType.INFORMATION);
	}

	/**
	 * Show a plain notification.
	 * @param title
	 * @param message
	 */
	public static void showPlainNotification(final String title, final String message) {
		if (isHeadless())
			logger.info(title + ": " + message);
		else
			showNotification(title, message, AlertType.NONE);
	}


	private static void showNotification(String title, String message, Alert.AlertType type) {
		// Attempt to show notification using ControlsFX, but fall back to a dialog if not available
		if (!controlsFxMissing) {
			try {
				qupath.fx.dialogs.ControlsFXNotifications.showNotifications(title, message, type);
				return;
			} catch (NoClassDefFoundError e) {
				logger.warn("ControlsFX notifications are not available, showing dialog instead");
				controlsFxMissing = true;
			}
		}
		new Builder()
				.buttons(ButtonType.OK)
				.title(title)
				.contentText(message)
				.alertType(type)
				.resizable()
				.show();
	}

	/**
	 * Show an error message.
	 * @param title
	 * @param message
	 */
	public static void showErrorMessage(final String title, String message) {
		if (isHeadless())
			logger.error(title + ": " + message);
		else {
			Node content;
			if (message == null || message.isBlank())
				content = createContentLabel("This app has encountered a problem, sorry");
			else
				content = createContentLabel(message);
			showErrorMessage(title, content);
		}
	}
	
	/**
	 * Show an error message, with the content defined within a {@link Node}.
	 * @param title
	 * @param node
	 */
	public static void showErrorMessage(final String title, final Node node) {
		new Builder()
			.alertType(AlertType.ERROR)
			.title(title)
			.content(node)
			.show();
	}

	/**
	 * Show a plain message.
	 * @param title
	 * @param message
	 */
	public static void showPlainMessage(final String title, final String message) {
		if (isHeadless()) {
			logger.info(title + ": " + message);
		} else {
			new Builder()
				.alertType(AlertType.INFORMATION)
				.title(title)
				.content(createContentLabel(message))
				.show();
		}
	}
	
	/**
	 * Show a window containing plain text, with the specified properties.
	 * 
	 * @param owner
	 * @param title
	 * @param contents
	 * @param modality
	 * @param isEditable
	 */
	public static void showTextWindow(final Window owner, final String title, final String contents, final Modality modality, final boolean isEditable) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> showTextWindow(owner, title, contents, modality, isEditable));
			return;
		}
		logger.info("{}\n{}", title, contents);
		Stage dialog = new Stage();
		if (owner == null)
			dialog.initOwner(getDefaultOwner());
		else
			dialog.initOwner(owner);
		dialog.initModality(modality);
		dialog.setTitle(title);
		
		TextArea textArea = new TextArea();
		textArea.setPrefColumnCount(60);
		textArea.setPrefRowCount(25);

		textArea.setText(contents);
		textArea.setWrapText(true);
		textArea.positionCaret(0);
		textArea.setEditable(isEditable);
		
		dialog.setScene(new Scene(textArea));
		dialog.show();
	}


	/**
	 * Get a default owner window.
	 * This is usually main application window, if available, unless we have any modal stages.
	 * If we do have modal stages, and one is in focus, use that.
	 * Otherwise, return null and let JavaFX figure out the owner.
	 * @return
	 */
	static Window getDefaultOwner() {
		// Check modality, then popup status, then focus, then title
		Comparator<Window> comparator = Comparator.comparing((Window w) -> Dialogs.isModal(w) ? -1 : 1) // Prefer modal windows
				.thenComparing(w -> w.isFocused() ? -1 : 1) // Prefer focused windows
				.thenComparing(w -> w == primaryWindow) // Prefer the primary window
				.thenComparing(Dialogs::getTitle); // Finally sort by title
		var owner = Window.getWindows().stream()
				.filter(w -> !(w instanceof PopupWindow)) // Avoid popup windows (they don't work well as owners)
				.sorted(comparator)
				.findFirst()
				.orElse(primaryWindow);
		return owner;
	}

	private static String getTitle(Window window) {
		String title = null;
		if (window instanceof Stage stage)
			title = stage.getTitle();
		return title == null ? "" : title;
	}

	private static boolean isModal(Window window) {
		if (window instanceof Stage stage)
			return stage.getModality() != Modality.NONE;
		return false;
	}


	/**
	 * Create a new builder to generate a custom dialog.
	 * @return
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	
	static boolean isHeadless() {
		return Window.getWindows().isEmpty();
	}

	/**
	 * Builder class to create a custom {@link Dialog}.
	 */
	public static class Builder {
		
		private AlertType alertType;
		private Window owner = null;
		private String title = "";
		private String header = null;
		private String contentText = null;
		private Node expandableContent = null;
		private Node content = null;
		private boolean resizable = false;
		private double width = -1;
		private double height = -1;
		private double prefWidth = -1;
		private double prefHeight = -1;
		private List<ButtonType> buttons = null;
		private Modality modality = Modality.APPLICATION_MODAL;
		
		/**
		 * Specify the dialog title.
		 * @param title dialog title
		 * @return this builder
		 */
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		/**
		 * Specify the dialog header text.
		 * This is text that is displayed prominently within the dialog.
		 * @param header dialog header
		 * @return this builder
		 * @see #contentText(String)
		 */
		public Builder headerText(String header) {
			this.header = header;
			return this;
		}
		
		/**
		 * Specify the dialog content text.
		 * This is text that is displayed within the dialog.
		 * @param content dialog content text
		 * @return this builder
		 * @see #headerText(String)
		 */
		public Builder contentText(String content) {
			this.contentText = content;
			return this;
		}
		
		/**
		 * Specify a {@link Node} to display within the dialog.
		 * @param content dialog content
		 * @return this builder
		 * @see #contentText(String)
		 */
		public Builder content(Node content) {
			this.content = content;
			return this;
		}
		
		/**
		 * Specify a {@link Node} to display within the dialog as expandable content, not initially visible.
		 * @param content dialog expandable content
		 * @return this builder
		 * @see #content(Node)
		 */
		public Builder expandableContent(Node content) {
			this.expandableContent = content;
			return this;
		}
		
		/**
		 * Specify the dialog owner.
		 * @param owner dialog title
		 * @return this builder
		 */
		public Builder owner(Window owner) {
			this.owner = owner;
			return this;
		}
		
		/**
		 * Make the dialog resizable (but default it is not).
		 * @return this builder
		 */
		public Builder resizable() {
			resizable = true;
			return this;
		}

		/**
		 * Specify that the dialog should be non-modal.
		 * By default, most dialogs are modal (and therefore block clicks to other windows).
		 * @return this builder
		 */
		public Builder nonModal() {
			this.modality = Modality.NONE;
			return this;
		}
		
		/**
		 * Specify the modality of the dialog.
		 * @param modality requested modality
		 * @return this builder
		 */
		public Builder modality(Modality modality) {
			this.modality = modality;
			return this;
		}
		
		/**
		 * Create a dialog styled as a specified alert type.
		 * @param type 
		 * @return this builder
		 */
		public Builder alertType(AlertType type) {
			alertType = type;
			return this;
		}
		
		/**
		 * Create a warning alert dialog.
		 * @return this builder
		 */
		public Builder warning() {
			return alertType(AlertType.WARNING);
		}
		
		/**
		 * Create an error alert dialog.
		 * @return this builder
		 */
		public Builder error() {
			return alertType(AlertType.ERROR);
		}
		
		/**
		 * Create an information alert dialog.
		 * @return this builder
		 */
		public Builder information() {
			return alertType(AlertType.INFORMATION);
		}
		
		/**
		 * Create an confirmation alert dialog.
		 * @return this builder
		 */
		public Builder confirmation() {
			return alertType(AlertType.CONFIRMATION);
		}
		
		/**
		 * Specify the buttons to display in the dialog.
		 * @param buttonTypes buttons to use
		 * @return this builder
		 */
		public Builder buttons(ButtonType... buttonTypes) {
			this.buttons = Arrays.asList(buttonTypes);
			return this;
		}
		
		/**
		 * Specify the buttons to display in the dialog.
		 * @param buttonNames names of buttons to use
		 * @return this builder
		 */
		public Builder buttons(String... buttonNames) {
			List<ButtonType> list = new ArrayList<>();
			for (String name : buttonNames) {
				ButtonType type;
				switch (name.toLowerCase()) {
				case "ok": type = ButtonType.OK; break;
				case "yes": type = ButtonType.YES; break;
				case "no": type = ButtonType.NO; break;
				case "cancel": type = ButtonType.CANCEL; break;
				case "apply": type = ButtonType.APPLY; break;
				case "close": type = ButtonType.CLOSE; break;
				case "finish": type = ButtonType.FINISH; break;
				case "next": type = ButtonType.NEXT; break;
				case "previous": type = ButtonType.PREVIOUS; break;
				default: type = new ButtonType(name); break;
				}
				list.add(type);
			}
			this.buttons = list;
			return this;
		}
		
		/**
		 * Specify the dialog width.
		 * @param width requested width
		 * @return this builder
		 */
		public Builder width(double width) {
			this.width = width;
			return this;
		}
		
		/**
		 * Specify the dialog height.
		 * @param height requested height
		 * @return this builder
		 */
		public Builder height(double height) {
			this.height = height;
			return this;
		}
		
		/**
		 * Specify the preferred width of the dialog pane.
		 * @param prefWidth preferred width
		 * @return this builder
		 * @since v0.4.0
		 */
		public Builder prefWidth(double prefWidth) {
			this.prefWidth = prefWidth;
			return this;
		}
		
		/**
		 * Specify the preferred height of the dialog pane.
		 * @param prefHeight preferred height
		 * @return this builder
		 * @since v0.4.0
		 */
		public Builder prefHeight(double prefHeight) {
			this.prefHeight = prefHeight;
			return this;
		}
		
		/**
		 * Specify the dialog height.
		 * @param width requested width
		 * @param height requested height
		 * @return this builder
		 */
		public Builder size(double width, double height) {
			this.width = width;
			this.height = height;
			return this;
		}
		
		/**
		 * Build the dialog.
		 * @return a {@link Dialog} created with the specified features.
		 */
		public Dialog<ButtonType> build() {
			Dialog<ButtonType> dialog;
			if (alertType == null)
				dialog = new Dialog<>();
			else
				dialog = new Alert(alertType);
			
			if (owner == null) {
				dialog.initOwner(getDefaultOwner());
			} else
				dialog.initOwner(owner);
			
			dialog.setTitle(title);
			if (header != null)
				dialog.setHeaderText(header);
			else
				// The alert type can make some rather ugly header text appear
				dialog.setHeaderText(null);
			if (contentText != null)
				dialog.setContentText(contentText);
			if (content != null)
				dialog.getDialogPane().setContent(content);
			if (expandableContent != null)
				dialog.getDialogPane().setExpandableContent(expandableContent);
			if (width > 0)
				dialog.setWidth(width);
			if (height > 0)
				dialog.setHeight(height);
			if (prefWidth > 0)
				dialog.getDialogPane().setPrefWidth(prefWidth);
			if (prefHeight > 0)
				dialog.getDialogPane().setPrefHeight(prefHeight);
			if (buttons != null)
				dialog.getDialogPane().getButtonTypes().setAll(buttons);
			
			// We do need to be able to close the dialog somehow
			if (dialog.getDialogPane().getButtonTypes().isEmpty()) {
				dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> dialog.hide());
			}
			
			dialog.setResizable(resizable);
			dialog.initModality(modality);
			
			// There's sometimes annoying visual bug in dark mode that results in a white/light 
			// thin line at the bottom of the dialog - padding seems to fix it
			if (Insets.EMPTY.equals(dialog.getDialogPane().getPadding()))
				dialog.getDialogPane().setStyle("-fx-background-insets: -1; -fx-padding: 1px;");
			
			return dialog;
		}
		
		
		/**
		 * Show the dialog.
		 * This is similar to {@code build().show()} except that it will automatically 
		 * be called on the JavaFX application thread even if called from another thread.
		 */
		public void show() {
			if (isHeadless()) {
				logger.warn("Cannot show dialog in headless mode!");
				return;
			}
			FXUtils.runOnApplicationThread(() -> build().show());
		}
		
		/**
		 * Show the dialog.
		 * This is similar to {@code build().showAndWait()} except that it will automatically 
		 * be called on the JavaFX application thread even if called from another thread.
		 * Callers should be cautious that this does not result in deadlock (e.g. if called from 
		 * the Swing Event Dispatch Thread on some platforms).
		 * @return 
		 */
		public Optional<ButtonType> showAndWait() {
			return FXUtils.callOnApplicationThread(() -> build().showAndWait());
		}
		
	}

	public static void main(String[] args) {
		// This shows a notification if it can - to help confirm that ControlsFX is really optional
		System.out.println("Starting main...");
		Platform.startup(() -> {
			var stage = new Stage();
			stage.setTitle("Title");
			stage.setScene(new Scene(new Label("Minimal stage")));
			stage.show();
			showInfoNotification("Info", "This is normal info");
			showErrorNotification("Uh-oh", "This is erroneous!");
		});
		System.out.println("Ending main");
	}


}