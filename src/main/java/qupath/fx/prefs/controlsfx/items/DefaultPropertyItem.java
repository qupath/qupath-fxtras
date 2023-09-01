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
import javafx.beans.value.ObservableValue;
import qupath.fx.localization.LocalizedResourceManager;

import java.util.Optional;

public class DefaultPropertyItem<T> extends PropertyItem {

    private Property<T> prop;
    private Class<? extends T> cls;

    public DefaultPropertyItem(final Property<T> prop, final Class<? extends T> cls) {
        this(null, prop, cls);
    }

    public DefaultPropertyItem(final LocalizedResourceManager manager, final Property<T> prop, final Class<? extends T> cls) {
        super(manager);
        this.prop = prop;
        this.cls = cls;
    }

    @Override
    public Class<?> getType() {
        return cls;
    }

    @Override
    public Object getValue() {
        return prop.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(Object value) {
        prop.setValue((T) value);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
        return Optional.of(prop);
    }

}
