package com.tiramisu.domain;
import com.tiramisu.domain.objects.SomeFieldClass;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.lang.String;

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess", "SameParameterValue"})
public final class SomeFieldClassBuilder {

  /** This is a utility class. */
  private SomeFieldClassBuilder() {}

  private SomeFieldClass _result_someFieldClass = new SomeFieldClass();
  private String stringBoy;
  private char someChar;

  private Set<String> fieldsToSet = new HashSet<>();

  public static SomeFieldClassBuilder create() {
    return new SomeFieldClassBuilder();
  }

  /** {@link SomeFieldClass#stringBoy}. */
  public SomeFieldClassBuilder with(String stringBoy) {
    this.stringBoy = stringBoy;
    fieldsToSet.add("stringBoy");
    return this;
  }

  /** {@link SomeFieldClass#someChar}. */
  public SomeFieldClassBuilder with(char someChar) {
    this.someChar = someChar;
    fieldsToSet.add("someChar");
    return this;
  }

  /**
    * Sets all fields to the default value that the
    * <a href="https://docs.oracle.com/javase/specs/jvms/se8/jvms8.pdf">JVM specification</a>
    * defines in sections 2.3 and 2.4.
    */
  public void clear() {
    with((String) null);
    with('\u0000');
  }

  /**
    * Sets all fields to default values predefined in the builder.<br /><br />
    * <b>NOTE:</b> all calls to the builder before this method is called will have no effect.
    * TODO implement default values!
    */
  public void _withDefaults() {
    fieldsToSet.clear();
    with(stringBoy); // TODO implement me!
    with(someChar); // TODO implement me!
  }

  /**
    * Allows to start building from an existing instance. If the instance you pass to this is
    * {@code null}, the call to this method will be ignored.
    */
  public void startingFrom(SomeFieldClass instance) {
    if (instance != null) {
      fieldsToSet.clear();
      _result_someFieldClass = instance;
    }
  }

  public SomeFieldClass build() {
    if(fieldsToSet.contains("stringBoy")) {
       setField("stringBoy", SomeFieldClass.class, stringBoy);
    }
    if(fieldsToSet.contains("someChar")) {
       setField("someChar", SomeFieldClass.class, someChar);
    }

    return _result_someFieldClass;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(_result_someFieldClass, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
