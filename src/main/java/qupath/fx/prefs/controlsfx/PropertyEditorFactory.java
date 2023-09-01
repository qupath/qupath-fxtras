package qupath.fx.prefs.controlsfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;
import qupath.fx.prefs.controlsfx.editors.ChoiceEditor;
import qupath.fx.prefs.controlsfx.editors.DirectoryEditor;
import qupath.fx.prefs.controlsfx.editors.SearchableChoiceEditor;
import qupath.fx.prefs.controlsfx.items.ChoicePropertyItem;
import qupath.fx.prefs.controlsfx.items.PropertyItem;
import qupath.fx.utils.FXUtils;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Extends {@link DefaultPropertyEditorFactory} to handle setting directories and creating choice editors.
 */
public class PropertyEditorFactory extends DefaultPropertyEditorFactory {

    /**
     * We may wish to reformat the display of some types, e.g. enums that are
     * otherwise shown with all capitals.
     */
    private Map<Class<?>, Function<?, String>> reformatTypes = Map.of();

    /**
     * Helper method to reformat the display of enums (or other all-capital representations).
     * @param obj
     * @return
     */
    private static String reformatEnum(Object obj) {
        var s = Objects.toString(obj);
        s = s.replaceAll("_", " ");
        if (Objects.equals(s, s.toUpperCase()))
            return s.substring(0, 1) + s.substring(1).toLowerCase();
        return s;
    }

    // Set this to true to automatically update labels & tooltips
    // (but not categories, unfortunately, so it can look odd)
    private boolean bindLabelText = false;

    // Need to cache editors, since the property sheet is rebuilt often
    // (but isn't smart enough to detach the editor listeners, so old ones hang around
    // and respond to 'setValue()' calls)
    private Map<PropertySheet.Item, PropertyEditor<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public PropertyEditor<?> call(PropertySheet.Item item) {
        PropertyEditor<?> editor = cache.getOrDefault(item, null);
        if (editor != null)
            return editor;

        if (item.getType() == File.class) {
            editor = new DirectoryEditor(item, new TextField());
        } else if (item instanceof ChoicePropertyItem) {
            var choiceItem = ((ChoicePropertyItem<?>)item);
            if (choiceItem.makeSearchable()) {
                editor = new SearchableChoiceEditor<>(choiceItem, choiceItem.getChoices());
            } else
                // Use this rather than Editors.createChoiceEditor() because it wraps an existing ObservableList where available
                editor = new ChoiceEditor<>(choiceItem, choiceItem.getChoices());
        } else
            editor = super.call(item);

        if (reformatTypes.containsKey(item.getType()) && editor.getEditor() instanceof ComboBox) {
            @SuppressWarnings("rawtypes")
            var combo = (ComboBox)editor.getEditor();
            var formatter = reformatTypes.get(item.getType());
            combo.setCellFactory(obj -> FXUtils.createCustomListCell(formatter));
            combo.setButtonCell(FXUtils.createCustomListCell(formatter));
        }

//        // Make it easier to reset default locale
//        if (Locale.class.equals(item.getType())) {
//            editor.getEditor().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
//                if (e.getClickCount() == 2) {
//                    if (Dialogs.showConfirmDialog(
//                            QuPathResources.getString("Prefs.localeReset"),
//                            QuPathResources.getString("Prefs.localeResetMessage"))) {
//                        item.setValue(Locale.US);
//                    }
//                }
//            });
//        }

        if (bindLabelText && item instanceof PropertyItem) {
            var listener = new ParentChangeListener((PropertyItem)item, editor.getEditor());
            editor.getEditor().parentProperty().addListener(listener);
        }

        cache.put(item, editor);

        return editor;
    }

    /**
     * Listener to bind the label & tooltip text (since these aren't accessible via the PropertySheet)
     */
    private static class ParentChangeListener implements ChangeListener<Parent> {

        private PropertyItem item;
        private Node node;

        private ParentChangeListener(PropertyItem item, Node node) {
            this.item = item;
            this.node = node;
        }

        @Override
        public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
            if (newValue == null)
                return;

            for (var labelLookup : newValue.lookupAll(".label")) {
                if (labelLookup instanceof Label label) {
                    if (label.getLabelFor() == node) {
                        if (!label.textProperty().isBound())
                            label.textProperty().bind(item.nameProperty());
                        var tooltip = label.getTooltip();
                        if (tooltip != null && !tooltip.textProperty().isBound())
                            tooltip.textProperty().bind(item.descriptionProperty());
                        break;
                    }
                }
            }
        }

    }

}