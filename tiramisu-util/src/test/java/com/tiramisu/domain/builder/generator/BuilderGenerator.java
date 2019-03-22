package com.tiramisu.domain.builder.generator;

import com.tiramisu.domain.test.TestClass;
import lombok.NoArgsConstructor;

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
import static lombok.AccessLevel.PRIVATE;

/**
 * Generate a builder that creates objects via reflection. The class for which the builder is created needs to have a
 * no-args constructor, and all classes in the type hierarchy need to be public. Another limitation of this class is
 * generics; due to type erasure, only setters for raw types will be generated, you'll have to generify manually.
 */
@NoArgsConstructor(access = PRIVATE)
public final class BuilderGenerator {

  /*-
   * HowTo:
   *      0. Set TARGET_CLASS to the class you want to generate a builder for
   *      1. Set BUILDER_PACKAGE to the package where you will put the java builder class
   *      2. Set builderClass to the name of your java builder class (should end with "Builder")
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

  private final Class<TestClass> TARGET_CLASS = TestClass.class;
  private String resultClassName = TARGET_CLASS.getSimpleName();
  private String resultFieldName = lowercase(TARGET_CLASS.getSimpleName());

  private String builderClass = TARGET_CLASS.getSimpleName() + "Builder";

  private static final boolean ALLOW_FILE_OVERWRITING = true;

  private static final boolean DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE = true;

  /** Constructors may initialize fields to non-null values (or non-default for primitives). To make sure the
   * builder doesn't override that, set this to true if you want the builder to only set the fields that a
   * {@code with<field>}-method was called for.*/
  private static final boolean BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR = true;

  private static final boolean IGNORE_FIELD_NAME_FOR_FIRST_BUILDER_METHOD_PER_RETURN_TYPE = false;

  private PrintStream stream = null;

  private Set<Field> fields;
  private Map<Field, String> fieldToBuilderFieldName; // DO NOT REMOVE - used for type hierarchy with same names
  private Map<String, Field> builderFieldNameToField;
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
    File builderFile = new File(builderClass + ".java");

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

    builderFieldNameToField = new HashMap<>();
    fieldToBuilderFieldName.forEach((K,V) -> builderFieldNameToField.put(V, K));

    fieldToSetterMethodName = new HashMap<>();
    fields.forEach(field -> fieldToSetterMethodName.put(field, "with" + capitalize(field.getName())));
  }

  private void fields() {
    fields = new HashSet<>();

    for(Class<?> clazz = TARGET_CLASS; clazz.getSuperclass() != null; clazz = clazz.getSuperclass()) {
      fields.addAll(asList(clazz.getDeclaredFields()));
    }
  }

  private void fieldToBuilderFieldName() {
    fieldToBuilderFieldName = new HashMap<>();




    fields.forEach(field -> fieldToBuilderFieldName.put(field, field.getName()));
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

    addImportForClassIfMissing(Field.class, alreadyImportedClasses);

    if (!DEFAULT_CONSTRUCTOR_OF_TARGET_CLASS_IS_ACCESSIBLE) {
      addImportForClassIfMissing(InvocationTargetException.class, alreadyImportedClasses);
    }

    if (BUILDER_SHOULD_ONLY_SET_FIELDS_THAT_BUILDER_METHOD_WAS_CALLED_FOR) {
      addImportForClassIfMissing(HashSet.class, alreadyImportedClasses);
      addImportForClassIfMissing(Set.class, alreadyImportedClasses);
    }

    fields.stream()
          .map(Field::getType)
          .filter(type -> !type.isPrimitive())
          .forEach(type -> addImportForClassIfMissing(type, alreadyImportedClasses));

    stream.println();
  }

  private void addImportForClassIfMissing(Class<?> type, Set<Class<?>> alreadyImportedClasses) {
    if(!alreadyImportedClasses.contains(type)) {
      alreadyImportedClasses.add(type);
      stream.println("import " + type.getCanonicalName() + ";");
    }
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                generateClassHeader                                               */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateClassHeader() {
    stream.println("@SuppressWarnings(\"unused\")");
    stream.println("public final class " + builderClass + " {");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                             generateBuilderConstructor                                           */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateBuilderConstructor() {
    stream.println("  /** This is a utility class. */");
    stream.println("  private " + builderClass+ "() {}");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                                  generateFields                                                  */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateFields() {
    stream.println("  private " + resultClassName + " " + resultFieldName + " = new " + resultClassName + "();");
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
    stream.println("  public static " + builderClass + " create() {");
    stream.println("    return new " + builderClass + "();");
    stream.println("  }");
    stream.println();
  }

  /* ---------------------------------------------------------------------------------------------------------------- */
  /*                                             generateWithFieldMethods                                             */
  /* ---------------------------------------------------------------------------------------------------------------- */
  private void generateWithFieldMethods() {
    for (Field field: fields) {
      stream.println("  /** {@link " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + "}. */");
      stream.println("  public " + builderClass + " " + fieldToSetterMethodName.get(field)
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
    stream.println("  public " + resultClassName + " build() {");
  }

  private void generateReflectionBasedBuildMethodStart() {
    stream.println("  public " + resultClassName + " build() throws NoSuchMethodException, IllegalAccessException,"
          + " InvocationTargetException,\n     InstantiationException {\n");
    stream.println("    " + resultClassName + " " + resultFieldName + " = "
          + resultClassName + ".class.getDeclaredConstructor().newInstance();");
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
