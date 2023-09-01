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
