package com.tiramisu.domain.builder.generator;

import com.tiramisu.domain.objects.DoubleSub;
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
public final class BuilderGenerator {

  private BuilderGenerator() {}

  /*-
   * HowTo:
   *      0. Set TARGET_CLASS to the class you want to generate a builder for
   *      1. Set BUILDER_PACKAGE to the package where you will put the java builder class
   *      2. Set builderClassName to the name of your java builder class (should end with "Builder")
   *      3. Set DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE to the appropriate value
   *      4. Set BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR to the appropriate value, see javadoc
   *      5. Set IGNORE_FIELD_NAME_FOR_FIRST_BUILDER_METHOD_PER_RETURN_TYPE to true if the first builder method that
   *         is generated per return type should only be called "with(..)" instead of "with<FieldName>(..)"
   *      6. Have getters in TARGET_CLASS for all fields you want builder methods for; e.g. use @Getter on
   *         TARGET_CLASS, only necessary during execution of the TestBuilderGenerator, can be removed again after
   *      7. Execute this class (Run As -> Java Application),
   *      8. A class will be generated in the top directory, move it to your location
   *      9. Add your new builder class to DomainBuilder.java
   *     10. Make final adjustments to new builder
   *          * e.g.some fields may need non-null default values
   *          * or some with-methods should have slightly different behavior, e.g. withElements(Collection<> X) may
   *            want to add all elements to an existing collection instead of replacing it, if X is not null
   *          * or maybe there were get-methods for fields that don't exist in TARGET_CLASS that you want
   *            to delete the with<field>-methods for
   *          * or some fields are generics that will have raw-type with<field>-methods generated for them -> generify
   */

  /* ---------------------------------------------------------------------------------------------------------------- */
  /* ------------------------------------------------- configuration ------------------------------------------------ */
  /* ---------------------------------------------------------------------------------------------------------------- */

  /** Class for which you want a builder to be generated. */
  private Class<TripleSub> TARGET_CLASS = TripleSub.class;

  /** The generator covers fields in all classes in the class hierarchy starting from {@link #TARGET_CLASS} up to this
   * upper bound, inclusive. If you set this to {@code Object.class}, {@code null}, or some class that is not an
   * ancestor of {@code TARGET_CLASS}, then the whole hierarchy above {@link #TARGET_CLASS} is included.*/
  private Class<Object> UPPER_BOUND_OF_HIERARCHY = Object.class;

  /** Add the classes in the type hierarchy between {@link #TARGET_CLASS} and {@link #UPPER_BOUND_OF_HIERARCHY} that
   * you do not want setter methods for in this set. */
  @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
  private HashSet<Class<?>> IGNORED_CLASSES_IN_HIERARCHY = new HashSet<>(
      asList(
          DoubleSub.class
      )
  );

  /** Constructors may initialize fields to non-null values (or non-default for primitives). To make sure the
   * builder doesn't override that, set this to true if you want the builder to only set the fields that a
   * {@code with<field>}-method was called for. */
  private boolean BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR = true;

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
  @SuppressWarnings("FieldCanBeLocal")
  private boolean OMIT_FIELD_NAME_IN_SETTER_METHOD_NAME_FOR_UNIQUE_FIELD_TYPES = true;

  private boolean DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE = true;

  /** If this is {@code true} and a file at the location where the builder will be generated already exists, then it
   * will be overwritten silently. If this is {@code false}, the program exits without changing the file. */
  @SuppressWarnings("FieldCanBeLocal")
  private boolean ALLOW_FILE_OVERWRITING = true;

  /** Prefix for the names of the methods that set fields in the generated object.
   * @see #OMIT_FIELD_NAME_IN_SETTER_METHOD_NAME_FOR_UNIQUE_FIELD_TYPES
   */
  private String SETTER_METHOD_NAME_PREFIX = "with";

  /* ---------------------------------------------------------------------------------------------------------------- */
  /* ----------------------------------------------- generating code ------------------------------------------------ */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private String targetClassName = TARGET_CLASS.getSimpleName();
  private String resultFieldName = lowercase(TARGET_CLASS.getSimpleName());

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

    generatePackageStatement();
    generateImports();
    generateClassHeader();
    generateBuilderConstructor();
    generateFields();
    generateFieldsToSet();
    generateCreateBuilderMethod();
    generateWithFieldMethods();
    generateClearFieldsMethod();
    generateDefaultValuesMethod();
    generateBuildMethod();
    generateSetFieldMethod();
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
  /*                                             generatePackageStatement                                             */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generatePackageStatement() {
    stream.println(TARGET_CLASS.getPackage().toString() + ";");
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                 generateImports                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateImports() {
    Set<Class<?>> alreadyImportedClasses = new HashSet<>();

    addImportIfMissing(Field.class, alreadyImportedClasses);

    if (!DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE) {
      addImportIfMissing(InvocationTargetException.class, alreadyImportedClasses);
    }

    if (BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
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
  /*                                                generateClassHeader                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateClassHeader() {
    stream.println("@SuppressWarnings({\"unused\", \"UnusedReturnValue\", \"WeakerAccess\"})");
    stream.println("public final class " + builderClassName + " {");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                             generateBuilderConstructor                                           */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateBuilderConstructor() {
    stream.println("  /** This is a utility class. */");
    stream.println("  private " + builderClassName + "() {}");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                  generateFields                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateFields() {
    stream.println("  private " + targetClassName + " " + resultFieldName + " = new " + targetClassName + "();");
    fields.forEach(field ->
        stream.println("  private " + field.getType().getSimpleName() + " " + fieldToBuilderFieldName.get(field) + ";")
    );
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                generateFieldsToSet                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateFieldsToSet() {
    if (BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      stream.println("  private Set<String> fieldsToSet = new HashSet<>();");
      stream.println();
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                            generateCreateBuilderMethod                                           */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateCreateBuilderMethod() {
    stream.println("  public static " + builderClassName + " create() {");
    stream.println("    return new " + builderClassName + "();");
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                             generateWithFieldMethods                                             */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateWithFieldMethods() {
    for (Field field: fields) {
      stream.println("  /** {@link " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + "}. */");
      stream.println("  public " + builderClassName + " " + fieldToSetterMethodName.get(field)
                                 + "(" + field.getType().getSimpleName() + " " + fieldToBuilderFieldName.get(field) + ") {");
      stream.println("    this." + fieldToBuilderFieldName.get(field) + " = " + fieldToBuilderFieldName.get(field) + ";");
      if (BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
        stream.println("    fieldsToSet.add(\"" + fieldToBuilderFieldName.get(field) + "\");");
      }
      stream.println("    return this;");
      stream.println("  }");
      stream.println();
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                             generateClearFieldsMethod                                            */
  /* ---------------------------------------------------------------------------------------------------------------- */

  private void generateClearFieldsMethod() {
    stream.println("  /** Sets all field to the default value the JVM initializes for the respective types. */");
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
      if (type.equals(int.class)) return "0";
      if (type.equals(long.class)) return "0L";
      if (type.equals(short.class)) return "(short) 0";
      if (type.equals(byte.class)) return "(byte) 0";
      if (type.equals(float.class)) return "0.0f";
      if (type.equals(double.class)) return "0.0";
      if (type.equals(char.class)) return "'\\u0000'";
      if (type.equals(boolean.class)) return "false";
    }

    if (fieldToSetterMethodName.get(field).equals(SETTER_METHOD_NAME_PREFIX)) {
      return "(" + field.getType().getSimpleName() + ") null";
    } else {
      return "null";
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                            generateDefaultValuesMethod                                           */
  /* ---------------------------------------------------------------------------------------------------------------- */

  private void generateDefaultValuesMethod() {
    stream.println("  /**");
    stream.println("    * Sets all fields to default values predefined in the builder.");
    stream.println("    * TODO implement default values!");
    stream.println("    */");
    stream.println("  public void _" + SETTER_METHOD_NAME_PREFIX + "Defaults() {");
    stream.println("    fieldsToSet.clear();");
    fields.forEach(field ->
        stream.println("    // " + fieldToSetterMethodName.get(field) + "(TODO implement me!);")
    );
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                generateBuildMethod                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateBuildMethod() {
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

    if (BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      stream.println("    if(fieldsToSet.contains(\"" + builderFieldName + "\")) {");
      stream.println("       setField(\"" + field.getName() + "\", " + fieldClass + ", " + builderFieldName + ");");
      stream.println("    }");
    } else {
      stream.println("    setField(" + builderFieldName + ");");
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                              generateSetFieldMethod                                              */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateSetFieldMethod() {
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
