package qupath.fx.prefs.controlsfx.editors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.SearchableComboBox;

import java.util.Collection;

/**
 * Editor for choosing from a longer list of items, aided by a searchable combo box.
 * @param <T>
 */
public class SearchableChoiceEditor<T> extends AbstractChoiceEditor<T, SearchableComboBox<T>> {

    public SearchableChoiceEditor(PropertySheet.Item property, Collection<? extends T> choices) {
        this(property, FXCollections.observableArrayList(choices));
    }

    public SearchableChoiceEditor(PropertySheet.Item property, ObservableList<T> choices) {
        super(new SearchableComboBox<>(), property, choices);
    }

}