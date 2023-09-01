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

package qupath.fx.localization;

import java.util.Locale;
import java.util.Objects;

/**
 * Snapshot of the current locale settings, including the main, display and format locales.
 * <p>
 * Because locale settings via {@link Locale} are not observable, it is not straightfoward to pick up when they have
 * been changed.
 * This class makes it possible to snapshot the current status, and then check whether it has changed on demand.
 */
public class LocaleSnapshot {

    private Locale main = Locale.getDefault();
    private Locale display = Locale.getDefault(Locale.Category.DISPLAY);
    private Locale format = Locale.getDefault(Locale.Category.FORMAT);

    /**
     * Create a new snapshot of the current locale settings.
     */
    public LocaleSnapshot() {
        refresh();
    }

    /**
     * Refresh the snapshot to the current locale settings.
     */
    public void refresh() {
        main = Locale.getDefault();
        display = Locale.getDefault(Locale.Category.DISPLAY);
        format = Locale.getDefault(Locale.Category.FORMAT);
    }

    /**
     * Get the main locale, as stored in this snapshot.
     * @return
     */
    public Locale getMainLocale() {
        return main;
    }

    /**
     * Get the display locale, as stored in this snapshot.
     * @return
     */
    public Locale getDisplayLocale() {
        return display;
    }

    /**
     * Get the format locale, as stored in this snapshot.
     * @return
     */
    public Locale getFormatLocale() {
        return format;
    }

    /**
     * Check whether this snapshot corresponds to the current locale settings.
     * @return
     */
    public boolean hasChanged() {
        return !Objects.equals(main, Locale.getDefault()) ||
                !Objects.equals(display, Locale.getDefault(Locale.Category.DISPLAY)) ||
                !Objects.equals(format, Locale.getDefault(Locale.Category.FORMAT));
    }

}