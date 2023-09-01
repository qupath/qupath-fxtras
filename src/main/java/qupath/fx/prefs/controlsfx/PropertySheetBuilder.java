package qupath.fx.prefs.controlsfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.fx.localization.LocalizedResourceManager;
import qupath.fx.prefs.annotations.BooleanPref;
import qupath.fx.prefs.annotations.ColorPref;
import qupath.fx.prefs.annotations.DirectoryPref;
import qupath.fx.prefs.annotations.DoublePref;
import qupath.fx.prefs.annotations.IntegerPref;
import qupath.fx.prefs.annotations.LocalePref;
import qupath.fx.prefs.annotations.Pref;
import qupath.fx.prefs.annotations.PrefCategory;
import qupath.fx.prefs.annotations.StringPref;
import qupath.fx.prefs.controlsfx.items.DefaultPropertyItem;
import qupath.fx.prefs.controlsfx.items.PropertyItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for building a ControlsFX {@link org.controlsfx.control.PropertySheet}.
 */
public class PropertySheetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PropertySheetBuilder.class);

    private final PropertySheet sheet;

    private LocalizedResourceManager localeManager = LocalizedResourceManager.createInstance(null);

    public PropertySheetBuilder() {
        this(createDefaultPropertySheet());
    }

    public PropertySheetBuilder(PropertySheet sheet) {
       this.sheet = sheet == null ? createDefaultPropertySheet() : sheet;
    }

    public static PropertySheet createDefaultPropertySheet() {
        var propSheet = new PropertySheet();
        propSheet.setMode(PropertySheet.Mode.CATEGORY);
        propSheet.setPropertyEditorFactory(new PropertyEditorFactory());
        return propSheet;
    }

    public PropertySheetBuilder resourceManager(LocalizedResourceManager manager) {
        this.localeManager = manager;
        return this;
    }

    public PropertySheetBuilder addItems(PropertySheet.Item... items) {
        for (var item : items)
            sheet.getItems().add(item);
        return this;
    }

    public PropertySheetBuilder addItems(Collection<? extends PropertySheet.Item> items) {
        for (var item : items)
            sheet.getItems().add(item);
        return this;
    }

    /**
     * Install properties that are the public fields of an object, configured using annotations.
     * The properties themselves are accessed using reflection.
     * <p>
     * If the provided object has a {@link PrefCategory} annotation, this defines the category
     * for all the identified properties.
     * Each property should then have a {@link Pref} annotation, or an alternative
     * such as {@link DoublePref}, {@link ColorPref}, {@link DirectoryPref}, {@link IntegerPref}.
     * @param object
     * @since v0.5.0
     */
    public PropertySheetBuilder addAnnotatedProperties(Object object) {
        var items = parseAnnotatedItemsWithResources(localeManager, object);
        sheet.getItems().addAll(items);
        return this;
    }

    public static List<PropertySheet.Item> parseAnnotatedItems(Object obj) {
        return parseAnnotatedItemsWithResources(null, obj);
    }

    public static List<PropertySheet.Item> parseAnnotatedItemsWithResources(LocalizedResourceManager manager, Object obj) {

        var cls = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        List<PropertySheet.Item> items = new ArrayList<>();

        // Look for category annotation from the parent class
        String categoryBundle = null;
        String categoryKey = "Prefs.General";
        if (cls.isAnnotationPresent(PrefCategory.class)) {
            var annotation = cls.getAnnotation(PrefCategory.class);
            categoryBundle = annotation.bundle().isBlank() ? null : annotation.bundle();
            categoryKey = annotation.value();
        }

        for (var field : cls.getDeclaredFields()) {
            if (!field.canAccess(obj) || !Property.class.isAssignableFrom(field.getType()))
                continue;
            PropertyItem item = null;
            try {
                // Skip null fields
                if (field.get(obj) == null)
                    continue;

                if (field.isAnnotationPresent(Pref.class)) {
                    item = parseItem((Property)field.get(obj), field.getAnnotation(Pref.class), obj, manager);
                } else if (field.isAnnotationPresent(BooleanPref.class)) {
                    item = parseItem((BooleanProperty)field.get(obj), field.getAnnotation(BooleanPref.class), manager);
                } else if (field.isAnnotationPresent(IntegerPref.class)) {
                    item = parseItem((IntegerProperty)field.get(obj), field.getAnnotation(IntegerPref.class), manager);
                } else if (field.isAnnotationPresent(DoublePref.class)) {
                    item = parseItem((DoubleProperty)field.get(obj), field.getAnnotation(DoublePref.class), manager);
                } else if (field.isAnnotationPresent(StringPref.class)) {
                    item = parseItem((Property<String>)field.get(obj), field.getAnnotation(StringPref.class), manager);
                } else if (field.isAnnotationPresent(LocalePref.class)) {
                    item = parseItem((Property<Locale>)field.get(obj), field.getAnnotation(LocalePref.class), manager);
                } else if (field.isAnnotationPresent(ColorPref.class)) {
                    item = parseItem((Property<Integer>)field.get(obj), field.getAnnotation(ColorPref.class), manager);
                } else if (field.isAnnotationPresent(DirectoryPref.class)) {
                    item = parseItem((Property<String>)field.get(obj), field.getAnnotation(DirectoryPref.class), manager);
                }

                // Handle direct category annotation
                if (field.isAnnotationPresent(PrefCategory.class)) {
                    var annotation = cls.getAnnotation(PrefCategory.class);
                    String localeCategoryBundle = annotation.bundle().isBlank() ? null : annotation.bundle();
                    String localeCategoryKey = annotation.value();
                    item.categoryKey(localeCategoryBundle, localeCategoryKey);
                } else {
                    item.categoryKey(categoryBundle, categoryKey);
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    private static PropertyItem parseItem(Property property, Pref annotation, Object parent, LocalizedResourceManager manager) {

        var builder = buildItem(property, annotation.type())
                .resourceManager(manager)
                .key(annotation.value())
                .bundle(annotation.bundle());

        var choiceMethod = annotation.choiceMethod();
        if (!choiceMethod.isBlank() && parent != null) {
            var cls = parent.getClass();
            try {
                var method = cls.getDeclaredMethod(choiceMethod);
                var result = method.invoke(parent);
                if (result instanceof ObservableList) {
                    builder.choices((ObservableList)result);
                } else if (result instanceof Collection) {
                    builder.choices((Collection)result);
                }
            } catch (Exception e) {
                logger.error("Unable to parse choices from " + annotation + ": " + e.getLocalizedMessage(), e);
            }
        }

        return builder.build();
    }

    private static PropertyItem parseItem(BooleanProperty property, BooleanPref annotation, LocalizedResourceManager manager) {
        return buildItem(property, Boolean.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .build();
    }

    private static PropertyItem parseItem(IntegerProperty property, IntegerPref annotation, LocalizedResourceManager manager) {
        return buildItem(property, Integer.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .build();
    }

    private static PropertyItem parseItem(DoubleProperty property, DoublePref annotation, LocalizedResourceManager manager) {
        return buildItem(property, Double.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .build();
    }

    private static PropertyItem parseItem(Property<String> property, StringPref annotation, LocalizedResourceManager manager) {
        return buildItem(property, String.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .build();
    }

    private static PropertyItem parseItem(Property<Locale> property, LocalePref annotation, LocalizedResourceManager manager) {
        logger.warn("LOCALE PREF NOT IMPLEMENTED");
        return buildItem(property, Locale.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                // TODO: REINSTATE LOCALE!
//                .choices(annotation.availableLanguagesOnly() ? localeManager.getAvailableLocales() : localeManager.getAllLocales())
                .propertyType(PropertyItemBuilder.PropertyType.SEARCHABLE_CHOICE)
                .build();
    }

    private static PropertyItem parseItem(Property<Integer> property, ColorPref annotation, LocalizedResourceManager manager) {
        return buildItem(property, Integer.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .propertyType(PropertyItemBuilder.PropertyType.COLOR)
                .build();
    }

    private static PropertyItem parseItem(Property<String> property, DirectoryPref annotation, LocalizedResourceManager manager) {
        return buildItem(property, String.class)
                .key(annotation.value())
                .resourceManager(manager)
                .bundle(annotation.bundle())
                .propertyType(PropertyItemBuilder.PropertyType.DIRECTORY)
                .build();
    }


    private static <T> PropertyItemBuilder<T> buildItem(Property<T> prop, final Class<? extends T> cls) {
        return new PropertyItemBuilder(prop, cls);
    }


    /**
     * Create a default {@link PropertySheet.Item} for a generic property.
     * @param <T> type of the property
     * @param property the property
     * @param cls the property type
     * @return a new {@link PropertyItem}
     */
    public static <T> PropertyItem createPropertySheetItem(Property<T> property, Class<? extends T> cls) {
        return new DefaultPropertyItem<>(property, cls);
    }


    public PropertySheet build() {
        return sheet;
    }


}
