package com.tiramisu.domain;
import com.tiramisu.domain.objects.Base;
import com.tiramisu.domain.objects.Sub;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.lang.String;
import java.util.List;
import java.lang.Integer;
import java.util.Date;

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess"})
public final class SubBuilder {

  /** This is a utility class. */
  private SubBuilder() {}

  private Sub sub = new Sub();
  private String thereInSub;
  private String hereInSub;
  private String dateInSub;
  private char someChar;
  private List list;
  private Integer thereInBase;
  private Date dateInBase;
  private String hereInBase;
  private int hi;

  private Set<String> fieldsToSet = new HashSet<>();

  public static SubBuilder create() {
    return new SubBuilder();
  }

  /** {@link Sub#there}. */
  public SubBuilder withThereInSub(String thereInSub) {
    this.thereInSub = thereInSub;
    fieldsToSet.add("thereInSub");
    return this;
  }

  /** {@link Sub#here}. */
  public SubBuilder withHereInSub(String hereInSub) {
    this.hereInSub = hereInSub;
    fieldsToSet.add("hereInSub");
    return this;
  }

  /** {@link Sub#date}. */
  public SubBuilder withDateInSub(String dateInSub) {
    this.dateInSub = dateInSub;
    fieldsToSet.add("dateInSub");
    return this;
  }

  /** {@link Sub#someChar}. */
  public SubBuilder with(char someChar) {
    this.someChar = someChar;
    fieldsToSet.add("someChar");
    return this;
  }

  /** {@link Sub#list}. */
  public SubBuilder with(List list) {
    this.list = list;
    fieldsToSet.add("list");
    return this;
  }

  /** {@link Base#there}. */
  public SubBuilder with(Integer thereInBase) {
    this.thereInBase = thereInBase;
    fieldsToSet.add("thereInBase");
    return this;
  }

  /** {@link Base#date}. */
  public SubBuilder with(Date dateInBase) {
    this.dateInBase = dateInBase;
    fieldsToSet.add("dateInBase");
    return this;
  }

  /** {@link Base#here}. */
  public SubBuilder withHereInBase(String hereInBase) {
    this.hereInBase = hereInBase;
    fieldsToSet.add("hereInBase");
    return this;
  }

  /** {@link Base#hi}. */
  public SubBuilder with(int hi) {
    this.hi = hi;
    fieldsToSet.add("hi");
    return this;
  }

  /** Sets all field to the default value the JVM initializes for the respective types. */
  public void clear() {
    withThereInSub(null);
    withHereInSub(null);
    withDateInSub(null);
    with('\u0000');
    with((List) null);
    with((Integer) null);
    with((Date) null);
    withHereInBase(null);
    with(0);
  }

  /**
    * Sets all fields to default values predefined in the builder.
    * TODO implement default values!
    */
  public void _withDefaults() {
    fieldsToSet.clear();
    // withThereInSub(TODO implement me!);
    // withHereInSub(TODO implement me!);
    // withDateInSub(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // withHereInBase(TODO implement me!);
    // with(TODO implement me!);
  }

  public Sub build() {
    if(fieldsToSet.contains("thereInSub")) {
       setField("there", Sub.class, thereInSub);
    }
    if(fieldsToSet.contains("hereInSub")) {
       setField("here", Sub.class, hereInSub);
    }
    if(fieldsToSet.contains("dateInSub")) {
       setField("date", Sub.class, dateInSub);
    }
    if(fieldsToSet.contains("someChar")) {
       setField("someChar", Sub.class, someChar);
    }
    if(fieldsToSet.contains("list")) {
       setField("list", Sub.class, list);
    }
    if(fieldsToSet.contains("thereInBase")) {
       setField("there", Base.class, thereInBase);
    }
    if(fieldsToSet.contains("dateInBase")) {
       setField("date", Base.class, dateInBase);
    }
    if(fieldsToSet.contains("hereInBase")) {
       setField("here", Base.class, hereInBase);
    }
    if(fieldsToSet.contains("hi")) {
       setField("hi", Base.class, hi);
    }

    return sub;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(sub, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
