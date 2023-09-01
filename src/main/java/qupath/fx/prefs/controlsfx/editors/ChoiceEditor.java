package qupath.fx.prefs.controlsfx.editors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.controlsfx.control.PropertySheet;

import java.util.Collection;

/**
 * Editor for choosing from a combo box, which will use an observable list directly if it can
 * (which differs from ControlsFX's default behavior).
 *
 * @param <T>
 */
public class ChoiceEditor<T> extends AbstractChoiceEditor<T, ComboBox<T>> {

    public ChoiceEditor(PropertySheet.Item property, Collection<? extends T> choices) {
        this(property, FXCollections.observableArrayList(choices));
    }

    public ChoiceEditor(PropertySheet.Item property, ObservableList<T> choices) {
        super(new ComboBox<>(), property, choices);
    }

}
