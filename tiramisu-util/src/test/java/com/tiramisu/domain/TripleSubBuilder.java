package com.tiramisu.domain;
import com.tiramisu.domain.objects.Base;
import com.tiramisu.domain.objects.Sub;
import com.tiramisu.domain.objects.TripleSub;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import java.lang.Integer;
import java.util.Date;

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess"})
public final class TripleSubBuilder {

  /** This is a utility class. */
  private TripleSubBuilder() {}

  private TripleSub tripleSub = new TripleSub();
  private Long noLightInTheDark;
  private String thereInSub;
  private String hereInSub;
  private String dateInSub;
  private char someChar;
  private List list;
  private Integer thereInBase;
  private Date dateInBase;
  private String hereInBase;
  private int hi;
  private String triple;

  private Set<String> fieldsToSet = new HashSet<>();

  public static TripleSubBuilder create() {
    return new TripleSubBuilder();
  }

  /** {@link TripleSub#noLightInTheDark}. */
  public TripleSubBuilder with(Long noLightInTheDark) {
    this.noLightInTheDark = noLightInTheDark;
    fieldsToSet.add("noLightInTheDark");
    return this;
  }

  /** {@link Sub#there}. */
  public TripleSubBuilder withThereInSub(String thereInSub) {
    this.thereInSub = thereInSub;
    fieldsToSet.add("thereInSub");
    return this;
  }

  /** {@link Sub#here}. */
  public TripleSubBuilder withHereInSub(String hereInSub) {
    this.hereInSub = hereInSub;
    fieldsToSet.add("hereInSub");
    return this;
  }

  /** {@link Sub#date}. */
  public TripleSubBuilder withDateInSub(String dateInSub) {
    this.dateInSub = dateInSub;
    fieldsToSet.add("dateInSub");
    return this;
  }

  /** {@link Sub#someChar}. */
  public TripleSubBuilder with(char someChar) {
    this.someChar = someChar;
    fieldsToSet.add("someChar");
    return this;
  }

  /** {@link Sub#list}. */
  public TripleSubBuilder with(List list) {
    this.list = list;
    fieldsToSet.add("list");
    return this;
  }

  /** {@link Base#there}. */
  public TripleSubBuilder with(Integer thereInBase) {
    this.thereInBase = thereInBase;
    fieldsToSet.add("thereInBase");
    return this;
  }

  /** {@link Base#date}. */
  public TripleSubBuilder with(Date dateInBase) {
    this.dateInBase = dateInBase;
    fieldsToSet.add("dateInBase");
    return this;
  }

  /** {@link Base#here}. */
  public TripleSubBuilder withHereInBase(String hereInBase) {
    this.hereInBase = hereInBase;
    fieldsToSet.add("hereInBase");
    return this;
  }

  /** {@link Base#hi}. */
  public TripleSubBuilder with(int hi) {
    this.hi = hi;
    fieldsToSet.add("hi");
    return this;
  }

  /** {@link TripleSub#triple}. */
  public TripleSubBuilder withTriple(String triple) {
    this.triple = triple;
    fieldsToSet.add("triple");
    return this;
  }

  /** Sets all field to the default value the JVM initializes for the respective types. */
  public void clear() {
    with((Long) null);
    withThereInSub(null);
    withHereInSub(null);
    withDateInSub(null);
    with('\u0000');
    with((List) null);
    with((Integer) null);
    with((Date) null);
    withHereInBase(null);
    with(0);
    withTriple(null);
  }

  /**
    * Sets all fields to default values predefined in the builder.
    * TODO implement default values!
    */
  public void _withDefaults() {
    fieldsToSet.clear();
    // with(TODO implement me!);
    // withThereInSub(TODO implement me!);
    // withHereInSub(TODO implement me!);
    // withDateInSub(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // with(TODO implement me!);
    // withHereInBase(TODO implement me!);
    // with(TODO implement me!);
    // withTriple(TODO implement me!);
  }

  public TripleSub build() {
    if(fieldsToSet.contains("noLightInTheDark")) {
       setField("noLightInTheDark", TripleSub.class, noLightInTheDark);
    }
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
    if(fieldsToSet.contains("triple")) {
       setField("triple", TripleSub.class, triple);
    }

    return tripleSub;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(tripleSub, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
