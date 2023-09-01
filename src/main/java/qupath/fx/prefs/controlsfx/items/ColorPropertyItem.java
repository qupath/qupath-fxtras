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

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import qupath.fx.localization.LocalizedResourceManager;

import java.util.Optional;

public class ColorPropertyItem extends PropertyItem {

    private IntegerProperty prop;
    private ObservableValue<Color> value;

    public ColorPropertyItem(final LocalizedResourceManager manager, final IntegerProperty prop) {
        super(manager);
        this.prop = prop;
        this.value = Bindings.createObjectBinding(() -> integerToColor(prop.getValue()), prop);
    }

    @Override
    public Class<?> getType() {
        return Color.class;
    }

    @Override
    public Object getValue() {
        return value.getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Color color)
            value = colorToInteger(color);
        if (value instanceof Integer)
            prop.setValue((Integer) value);
        else if (value == null)
            prop.setValue(null);
        else
            throw new IllegalArgumentException("Unsupported color value " + value);
    }

    private static Integer colorToInteger(Color color) {
        if (color == null)
            return null;
        int a = (int) Math.round(color.getOpacity() * 255);
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        int argb = (a << 24) | (r << 16) | (g << 8) | b;
        return argb;
    }

    private static Color integerToColor(Integer argb) {
        if (argb == null)
            return null;
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = (argb >> 0) & 0xFF;
        return Color.color(
                r/255.0,
              g/255.0,
               b/255.0,
             a/255.0
        );
    }


    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
        return Optional.of(value);
    }

}
