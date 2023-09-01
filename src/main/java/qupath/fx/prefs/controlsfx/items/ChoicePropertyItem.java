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

package qupath.fx.prefs.controlsfx.items;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import qupath.fx.localization.LocalizedResourceManager;

public class ChoicePropertyItem<T> extends DefaultPropertyItem<T> {

    private final ObservableList<T> choices;
    private final boolean makeSearchable;

    public ChoicePropertyItem(final LocalizedResourceManager manager, final Property<T> prop, final ObservableList<T> choices, final Class<? extends T> cls) {
        this(manager, prop, choices, cls, false);
    }

    public ChoicePropertyItem(final LocalizedResourceManager manager, final Property<T> prop, final ObservableList<T> choices, final Class<? extends T> cls, boolean makeSearchable) {
        super(manager, prop, cls);
        this.choices = choices;
        this.makeSearchable = makeSearchable;
    }

    public ObservableList<T> getChoices() {
        return choices;
    }

    public boolean makeSearchable() {
        return makeSearchable;
    }

}
