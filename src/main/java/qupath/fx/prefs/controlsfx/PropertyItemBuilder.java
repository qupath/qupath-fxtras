package qupath.fx.prefs.controlsfx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.controlsfx.items.ChoicePropertyItem;
import qupath.fx.prefs.controlsfx.items.ColorPropertyItem;
import qupath.fx.prefs.controlsfx.items.DefaultPropertyItem;
import qupath.fx.prefs.controlsfx.items.DirectoryPropertyItem;
import qupath.fx.prefs.controlsfx.items.PropertyItem;

import java.util.Collection;

public class PropertyItemBuilder<T> {

    public enum PropertyType {GENERAL, DIRECTORY, COLOR, CHOICE, SEARCHABLE_CHOICE}

    private LocalizedResourceManager manager;

    private Property<T> property;
    private Class<? extends T> cls;

    private PropertyType propertyType = PropertyType.GENERAL;
    private ObservableList<T> choices;

    private String bundle;
    private String key;
    private String categoryKey;

    public PropertyItemBuilder(Property<T> prop, final Class<? extends T> cls) {
        this.property = prop;
        this.cls = cls;
    }

    public PropertyItemBuilder<T> resourceManager(LocalizedResourceManager manager) {
        this.manager = manager;
        return this;
    }

    public PropertyItemBuilder<T> key(String key) {
        this.key = key;
        return this;
    }

    public PropertyItemBuilder<T> propertyType(PropertyType type) {
        this.propertyType = type;
        return this;
    }

    public PropertyItemBuilder<T> choices(Collection<T> choices) {
        return choices(FXCollections.observableArrayList(choices));
    }

    public PropertyItemBuilder<T> choices(ObservableList<T> choices) {
        this.choices = choices;
        this.propertyType = PropertyType.CHOICE;
        return this;
    }

    public PropertyItemBuilder<T> bundle(String name) {
        this.bundle = name;
        return this;
    }

    public PropertyItem build() {
        PropertyItem item;
        switch (propertyType) {
            case DIRECTORY:
                item = new DirectoryPropertyItem(manager, (Property<String>) property);
                break;
            case COLOR:
                item = new ColorPropertyItem(manager, (IntegerProperty) property);
                break;
            case CHOICE:
                item = new ChoicePropertyItem<>(manager, property, choices, cls, false);
                break;
            case SEARCHABLE_CHOICE:
                item = new ChoicePropertyItem<>(manager, property, choices, cls, true);
                break;
            case GENERAL:
            default:
                item = new DefaultPropertyItem<>(manager, property, cls);
                break;
        }
        if (key != null)
            item.key(bundle, key);
        if (categoryKey != null) {
            item.categoryKey(bundle, categoryKey);
        }
        return item;
    }

}
