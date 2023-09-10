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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for showing notifications using ControlsFX.
 * Separated from the main {@link Dialogs} class to help make ControlsFX optional.
 */
class ControlsFXNotifications {

    private static final Logger logger = LoggerFactory.getLogger(ControlsFXNotifications.class);

    /**
     * Show notification, making sure it is on the application thread
     */
    static void showNotifications(String title, String message, Alert.AlertType type) {
        if (Dialogs.isHeadless()) {
            logger.warn("Cannot show notifications in headless mode!");
            return;
        }
        if (Platform.isFxApplicationThread()) {
            switch (type) {
                case CONFIRMATION:
                    createNotifications().title(title).text(message).showConfirm();
                    break;
                case ERROR:
                    createNotifications().title(title).text(message).showError();
                    break;
                case INFORMATION:
                    createNotifications().title(title).text(message).showInformation();
                    break;
                case WARNING:
                    createNotifications().title(title).text(message).showWarning();
                    break;
                case NONE:
                default:
                    createNotifications().title(title).text(message).show();
                    break;
            }
        } else
            Platform.runLater(() -> showNotifications(title, message, type));
    }


    /**
     * Necessary to have owner when calling notifications (bug in controlsfx?).
     */
    private static Notifications createNotifications() {
        var stage = Dialogs.getDefaultOwner();
        var notifications = Notifications.create();
        if (stage == null)
            return notifications;

        // 'Notifications' has a fixed color based on light/dark mode
        // Here, we instead use the default color for text based on the current css for the scene
        var scene = stage.getScene();
        if (scene != null) {
            var url = Dialogs.class.getClassLoader().getResource("qupath/fx/dialogs/notificationscustom.css");
            String stylesheetUrl = url.toExternalForm();
            if (!scene.getStylesheets().contains(stylesheetUrl))
                scene.getStylesheets().add(stylesheetUrl);
            notifications.styleClass("custom");
        }
        // It looks better to use a screen as an owner, not a stage
        var screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        if (screens.isEmpty() || screens.size() > 1)
            return notifications;
        else
            return notifications.owner(screens.get(0));
    }

}
