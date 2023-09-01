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
