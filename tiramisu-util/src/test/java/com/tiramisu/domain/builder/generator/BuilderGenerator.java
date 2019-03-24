package com.tiramisu.domain.builder.generator;

import com.tiramisu.domain.SomeFieldClassBuilder;
import com.tiramisu.domain.objects.DoubleSub;
import com.tiramisu.domain.objects.SomeFieldClass;
import com.tiramisu.domain.objects.TripleSub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Generate a builder that creates objects via reflection. The class for which the builder is created needs to have a
 * no-args constructor, and all classes in the type hierarchy need to be public (if there are classes in the type
 * hierarchy that are not public, they will be ignored). Another limitation of this class is generics; due to type
 * erasure, only setters for raw types will be generated, you'll have to generify manually.
 */
@SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "FieldCanBeLocal"})
public final class BuilderGenerator {

  private BuilderGenerator() {}

  /*-
   * HowTo:
   *      1. Set the fields in the configuration section below
   *      2. Execute the main method of this class
   *      3. A class will be generated in the root directory of the project, copy it to the package where you need it
   */

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                   configuration                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */

  /** Class for which you want a builder to be generated. */
  private Class<TripleSub> TARGET_CLASS = TripleSub.class;

  /** The generator covers fields in all classes in the class hierarchy starting from {@link #TARGET_CLASS} up to this
   * upper bound, inclusive. If you set this to {@code Object.class}, {@code null}, or some class that is not an
   * ancestor of {@code TARGET_CLASS}, then the whole hierarchy above {@link #TARGET_CLASS} is included.*/
  private Class<Object> UPPER_BOUND_OF_HIERARCHY = Object.class;

  /** Add the classes in the type hierarchy between {@link #TARGET_CLASS} and {@link #UPPER_BOUND_OF_HIERARCHY} that
   * you do not want setter methods for in this set. */
  private HashSet<Class<?>> IGNORED_CLASSES_IN_HIERARCHY = new HashSet<>(
      asList(
          DoubleSub.class
      )
  );

  /** If you already have some builder classes (i.e. classes with a {@code build()} instance-method (not a static
   *  one) that returns a class instance for some of the classes for which
   *  {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>(instance)}-methods will be generated, then you can add
   *  the classes and their builder-classes to this map and the generator will additionally generate
   *  {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>(builderInstance)}-methods.<br /><br />
   *
   *  <b>Example:</b> if there's a field of type {@code Spaghetti} and you have a {@code SpaghettiBuilder}, then add
   *  {@code put(Spaghetti.class, SpaghettiBuilder.class)} to this map so you get both a
   *  {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>(Spaghetti)}
   *  and a
   *  {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>(SpaghettiBuilder)}
   *  method. */
  private HashMap<Class<?>, Class<?>> FIELD_TYPES_TO_EXISTING_BUILDER_CLASSES = new HashMap<Class<?>, Class<?>>() {
    { // initialize the anonymous map in an initializer block
      put(SomeFieldClass.class, SomeFieldClassBuilder.class);
    }
  };

  /** Constructors may initialize fields to non-null values (or non-default for primitives), or you may want to use
   *  the builder by starting {@link #startingFromInstanceMethod() building from an existing instance}. To make sure the
   *  builder doesn't override that, set this to true if you want the builder to only set the fields that a
   * {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>}-method was called for. */
  private boolean ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR = true;

  /** If a field type is unique among all fields that {@value #SETTER_METHOD_NAME_PREFIX}{@code <fieldName>(fieldType)}
   * -methods are generated for and you want those field types to have their methods just called
   * {@value #SETTER_METHOD_NAME_PREFIX}{@code (fieldType)}, then set this to {@code true}.
   * <br /><br />
   * For example, if you set this variable to {@code true} and there is only one field of type {@code String} in the
   * type hierarchy between {@link #TARGET_CLASS} and {@link #UPPER_BOUND_OF_HIERARCHY}, then if that field is called
   * {@code someStringField}, the generated method will be called
   * {@value #SETTER_METHOD_NAME_PREFIX}{@code (String someStringField)} instead of
   * {@value #SETTER_METHOD_NAME_PREFIX}{@code SomeStringField(String someStringField)}.
   *
   * @see #SETTER_METHOD_NAME_PREFIX set the "with" prefix
   */
  private boolean OMIT_FIELD_NAME_IN_SETTER_METHOD_NAME_FOR_UNIQUE_FIELD_TYPES = true;

  private boolean DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE = true;

  /** If this is {@code true} and a file at the location where the builder will be generated already exists, then it
   * will be overwritten silently. If this is {@code false}, the program exits without changing the file. */
  private boolean ALLOW_FILE_OVERWRITING = true;

  /** Prefix for the names of the methods that set fields in the generated object.
   * @see #OMIT_FIELD_NAME_IN_SETTER_METHOD_NAME_FOR_UNIQUE_FIELD_TYPES
   */
  private String SETTER_METHOD_NAME_PREFIX = "with";

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 generating code                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private String targetClassName = TARGET_CLASS.getSimpleName();
  private String resultFieldName = "_result_" + lowercase(TARGET_CLASS.getSimpleName());

  private String builderClassName = TARGET_CLASS.getSimpleName() + "Builder";

  private PrintStream stream = null;

  private Set<Field> fields;
  private Map<Field, String> fieldToBuilderFieldName;
  private Map<Field, String> fieldToSetterMethodName;

  /**
   * @param args not used
   */
  public static void main(String[] args) {
    new BuilderGenerator().generateBuilder();
  }

  private void generateBuilder() {
    initFileStream();

    if (stream != null) {
      generateBuilderClass();
      stream.close();
    }
  }

  private void initFileStream() {
    File builderFile = new File(builderClassName + ".java");

    try {
      if (ALLOW_FILE_OVERWRITING || !builderFile.exists()) {
        stream = new PrintStream(new FileOutputStream(builderFile), false, "UTF-8");
        System.out.println("Builder created: " + builderFile.getAbsolutePath());
      } else {
        System.err.println("File " + builderFile.getAbsolutePath() + " already exists and may not be overwritten");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void generateBuilderClass() {
    initializeFieldInfo();

    packageStatement();
    imports();
    classHeader();
    builderConstructor();
    builderFields();
    fieldsToSet();
    createBuilderMethod();
    withFieldMethods();
    clearFieldsMethod();
    defaultValuesMethod();
    startingFromInstanceMethod();
    buildMethod();
    setFieldMethod();
    endClass();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                initializeFieldInfo                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void initializeFieldInfo() {
    fields();
    fieldToBuilderFieldName();

    fieldToSetterMethodName = new HashMap<>();
    if (!OMIT_FIELD_NAME_IN_SETTER_METHOD_NAME_FOR_UNIQUE_FIELD_TYPES) {
      fields.forEach(field ->
          fieldToSetterMethodName.put(field, SETTER_METHOD_NAME_PREFIX + capitalize(fieldToBuilderFieldName.get(field))));
    } else {
      HashMap<Class<?>, Integer> fieldTypeToNumberOfFieldsOfSameType = new HashMap<>();
      fields.forEach(field ->
          fieldTypeToNumberOfFieldsOfSameType.merge(field.getType(), 1, Integer::sum));
      fields.forEach(field -> {
        if (fieldTypeToNumberOfFieldsOfSameType.get(field.getType()) > 1) {
          fieldToSetterMethodName.put(field, SETTER_METHOD_NAME_PREFIX + capitalize(fieldToBuilderFieldName.get(field)));
        } else {
          fieldToSetterMethodName.put(field, SETTER_METHOD_NAME_PREFIX);
        }
      });
    }
  }

  private void fields() {
    fields = new HashSet<>();

    for(Class<?> clazz = TARGET_CLASS; clazz != null; clazz = clazz.getSuperclass()) {
      if ((UPPER_BOUND_OF_HIERARCHY == null || clazz != UPPER_BOUND_OF_HIERARCHY.getSuperclass())
          && !IGNORED_CLASSES_IN_HIERARCHY.contains(clazz)) {
        fields.addAll(asList(clazz.getDeclaredFields()));
      }
    }
  }

  /**
   * Do not call this method before calling {@link #fields()}.
   */
  private void fieldToBuilderFieldName() {
    fieldToBuilderFieldName = new HashMap<>();

    HashMap<String, Integer> occurrencesOfFieldNameAndType = new HashMap<>();
    fields.forEach(field ->
        occurrencesOfFieldNameAndType.merge(duplicateIdentifierFor(field), 1, Integer::sum));

    fields.forEach(field -> {
      if(occurrencesOfFieldNameAndType.get(duplicateIdentifierFor(field)) > 1) {
        fieldToBuilderFieldName.put(field,
            lowercase(field.getName() + "In" + field.getDeclaringClass().getSimpleName()));
      } else {
        fieldToBuilderFieldName.put(field, lowercase(field.getName()));
      }
    });
  }

  private String duplicateIdentifierFor(Field field) {
    return field.getName();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 packageStatement                                                 */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void packageStatement() {
    stream.println(TARGET_CLASS.getPackage().toString() + ";");
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                     imports                                                      */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void imports() {
    Set<Class<?>> alreadyImportedClasses = new HashSet<>();

    addImportIfMissing(Field.class, alreadyImportedClasses);

    if (!DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE) {
      addImportIfMissing(InvocationTargetException.class, alreadyImportedClasses);
    }

    if (ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      addImportIfMissing(HashSet.class, alreadyImportedClasses);
      addImportIfMissing(Set.class, alreadyImportedClasses);
    }

    fields.stream()
          .map(Field::getType)
          .filter(type -> !type.isPrimitive())
          .forEach(type -> addImportIfMissing(type, alreadyImportedClasses));

    stream.println();
  }

  private void addImportIfMissing(Class<?> type, Set<Class<?>> alreadyImportedClasses) {
    if(!alreadyImportedClasses.contains(type)) {
      alreadyImportedClasses.add(type);
      stream.println("import " + type.getCanonicalName() + ";");
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                    classHeader                                                   */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void classHeader() {
    stream.println("@SuppressWarnings({\"unused\", \"UnusedReturnValue\", \"WeakerAccess\", \"SameParameterValue\"})");
    stream.println("public final class " + builderClassName + " {");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 builderConstructor                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void builderConstructor() {
    stream.println("  /** This is a utility class. */");
    stream.println("  private " + builderClassName + "() {}");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                  builderFields                                                   */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void builderFields() {
    stream.println("  private " + targetClassName + " " + resultFieldName + " = new " + targetClassName + "();");
    fields.forEach(field ->
        stream.println("  private " + field.getType().getSimpleName() + " " + fieldToBuilderFieldName.get(field) + ";")
    );
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                    fieldsToSet                                                   */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void fieldsToSet() {
    if (ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      stream.println("  private Set<String> fieldsToSet = new HashSet<>();");
      stream.println();
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                createBuilderMethod                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void createBuilderMethod() {
    stream.println("  public static " + builderClassName + " create() {");
    stream.println("    return new " + builderClassName + "();");
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 withFieldMethods                                                 */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void withFieldMethods() {
    for (Field field: fields) {
      stream.println("  /** {@link " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + "}. */");
      stream.println("  public " + builderClassName + " " + fieldToSetterMethodName.get(field)
                                 + "(" + field.getType().getSimpleName() + " " + fieldToBuilderFieldName.get(field) + ") {");
      stream.println("    this." + fieldToBuilderFieldName.get(field) + " = " + fieldToBuilderFieldName.get(field) + ";");

      if (ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
        stream.println("    fieldsToSet.add(\"" + fieldToBuilderFieldName.get(field) + "\");");
      }

      stream.println("    return this;");
      stream.println("  }");
      stream.println();

      if (FIELD_TYPES_TO_EXISTING_BUILDER_CLASSES.containsKey(field.getType())) {
        Class<?> builderClass = FIELD_TYPES_TO_EXISTING_BUILDER_CLASSES.get(field.getType());

        stream.println("  /** {@link " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + "}. */");
        stream.println("  public " + builderClassName + " " + fieldToSetterMethodName.get(field)
            + "(" + builderClass.getSimpleName() + " " + fieldToBuilderFieldName.get(field) + "Builder) {");
        stream.println("    this." + fieldToBuilderFieldName.get(field)
                                   + " = " + fieldToBuilderFieldName.get(field) + "Builder.build();");

        if (ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
          stream.println("    fieldsToSet.add(\"" + fieldToBuilderFieldName.get(field) + "\");");
        }

        stream.println("    return this;");
        stream.println("  }");
        stream.println();
      }
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 clearFieldsMethod                                                */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void clearFieldsMethod() {
    stream.println("  /**");
    stream.println("    * Sets all fields to the default value that the");
    stream.println("    * <a href=\"https://docs.oracle.com/javase/specs/jvms/se8/jvms8.pdf\">JVM specification</a>");
    stream.println("    * defines in sections 2.3 and 2.4.");
    stream.println("    */");
    stream.println("  public void clear() {");
    fields.forEach(field ->
        stream.println("    " + fieldToSetterMethodName.get(field) + "(" + clearedValueForField(field) + ");")
    );
    stream.println("  }");
    stream.println();
  }

  private String clearedValueForField(Field field) {
    Class<?> type = field.getType();

    if (type.isPrimitive()) {
      if (type.equals(boolean.class)) return "false";
      if (type.equals(byte.class))    return "(byte) 0";
      if (type.equals(short.class))   return "(short) 0";
      if (type.equals(int.class))     return "0";
      if (type.equals(long.class))    return "0L";
      if (type.equals(char.class))    return "'\\u0000'";
      if (type.equals(float.class))   return "0.0f";
      if (type.equals(double.class))  return "0.0";
    }

    if (fieldToSetterMethodName.get(field).equals(SETTER_METHOD_NAME_PREFIX)
        || FIELD_TYPES_TO_EXISTING_BUILDER_CLASSES.containsKey(field.getType())) {
      return "(" + field.getType().getSimpleName() + ") null";
    } else {
      return "null";
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                defaultValuesMethod                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void defaultValuesMethod() {
    stream.println("  /**");
    stream.println("    * Sets all fields to default values predefined in the builder.<br /><br />");
    stream.println("    * <b>NOTE:</b> all calls to the builder before this method is called will have no effect.");
    stream.println("    * TODO implement default values!");
    stream.println("    */");
    stream.println("  public void _" + SETTER_METHOD_NAME_PREFIX + "Defaults() {");
    fields.forEach(field ->
        stream.println("    " + fieldToSetterMethodName.get(field)
                              + "(" + fieldToBuilderFieldName.get(field) + "); // TODO implement me!")
    );
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                            startingFromInstanceMethod                                            */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void startingFromInstanceMethod() {
    stream.println("  /**");
    stream.println("    * Allows to start building from an existing instance. If the instance you pass to this is");
    stream.println("    * {@code null}, the call to this method will be ignored.");
    stream.println("    */");
    stream.println("  public void startingFrom(" + targetClassName + " instance) {");
    stream.println("    if (instance != null) {");
    stream.println("      clear();");
    stream.println("      " + resultFieldName + " = instance;");
    stream.println("    }");
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                    buildMethod                                                   */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void buildMethod() {
    if(DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE) {
      generateSimpleBuildMethodStart();
    } else {
      generateReflectionBasedBuildMethodStart();
    }

    fields.forEach(this::generateFieldSetter);

    stream.println();
    stream.println("    return " + resultFieldName + ";");
    stream.println("  }");
    stream.println();
  }

  private void generateSimpleBuildMethodStart() {
    stream.println("  public " + targetClassName + " build() {");
  }

  private void generateReflectionBasedBuildMethodStart() {
    stream.println("  public " + targetClassName + " build() throws NoSuchMethodException, IllegalAccessException,"
          + " InvocationTargetException,\n     InstantiationException {\n");
    stream.println("    " + targetClassName + " " + resultFieldName + " = "
          + targetClassName + ".class.getDeclaredConstructor().newInstance();");
    stream.println();
  }

  private void generateFieldSetter(Field field) {
    String builderFieldName = fieldToBuilderFieldName.get(field);
    String fieldClass = field.getDeclaringClass().getSimpleName() + ".class";

    if (ONLY_OVERWRITE_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      stream.println("    if(fieldsToSet.contains(\"" + builderFieldName + "\")) {");
      stream.println("       setField(\"" + field.getName() + "\", " + fieldClass + ", " + builderFieldName + ");");
      stream.println("    }");
    } else {
      stream.println("    setField(\"" + field.getName() + "\", " + fieldClass + ", " + builderFieldName + ");");
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                  setFieldMethod                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void setFieldMethod() {
    stream.println("  private void setField(String fieldName, Class<?> type, Object fieldValue) {");
    stream.println("    try {");
    stream.println("      Field objectField = type.getDeclaredField(fieldName);");
    stream.println("      objectField.setAccessible(true);");
    stream.println("      objectField.set(" + resultFieldName + ", fieldValue);");
    stream.println("    } catch (Exception ex) {");
    stream.println("      ex.printStackTrace();");
    stream.println("    }");
    stream.println("  }");
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                     endClass                                                     */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void endClass() {
    stream.println("}");
  }

  private static String lowercase(String string) {
    return string.substring(0, 1).toLowerCase() + string.substring(1);
  }

  private static String capitalize(String string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }
}
