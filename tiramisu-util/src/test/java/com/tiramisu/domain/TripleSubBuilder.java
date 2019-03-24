package com.tiramisu.domain;
import com.tiramisu.domain.objects.Base;
import com.tiramisu.domain.objects.SomeFieldClass;
import com.tiramisu.domain.objects.Sub;
import com.tiramisu.domain.objects.TripleSub;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess", "SameParameterValue"})
public final class TripleSubBuilder {

  /** This is a utility class. */
  private TripleSubBuilder() {}

  private TripleSub _result_tripleSub = new TripleSub();
  private long longy;
  private double doubley;
  private String hereInSub;
  private String dateInSub;
  private char someChar;
  private List list;
  private short shorty;
  private Date dateInBase;
  private String hereInBase;
  private int hi;
  private SomeFieldClass fieldWithBuilder;
  private String triple;
  private Long noLightInTheDark;
  private String thereInSub;
  private float floaty;
  private byte bytey;
  private boolean booleany;
  private Integer thereInBase;

  private Set<String> fieldsToSet = new HashSet<>();

  public static TripleSubBuilder create() {
    return new TripleSubBuilder();
  }

  /** {@link TripleSub#longy}. */
  public TripleSubBuilder with(long longy) {
    this.longy = longy;
    fieldsToSet.add("longy");
    return this;
  }

  /** {@link TripleSub#doubley}. */
  public TripleSubBuilder with(double doubley) {
    this.doubley = doubley;
    fieldsToSet.add("doubley");
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

  /** {@link TripleSub#shorty}. */
  public TripleSubBuilder with(short shorty) {
    this.shorty = shorty;
    fieldsToSet.add("shorty");
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

  /** {@link Base#fieldWithBuilder}. */
  public TripleSubBuilder with(SomeFieldClass fieldWithBuilder) {
    this.fieldWithBuilder = fieldWithBuilder;
    fieldsToSet.add("fieldWithBuilder");
    return this;
  }

  /** {@link Base#fieldWithBuilder}. */
  public TripleSubBuilder with(SomeFieldClassBuilder fieldWithBuilderBuilder) {
    this.fieldWithBuilder = fieldWithBuilderBuilder.build();
    fieldsToSet.add("fieldWithBuilder");
    return this;
  }

  /** {@link TripleSub#triple}. */
  public TripleSubBuilder withTriple(String triple) {
    this.triple = triple;
    fieldsToSet.add("triple");
    return this;
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

  /** {@link TripleSub#floaty}. */
  public TripleSubBuilder with(float floaty) {
    this.floaty = floaty;
    fieldsToSet.add("floaty");
    return this;
  }

  /** {@link TripleSub#bytey}. */
  public TripleSubBuilder with(byte bytey) {
    this.bytey = bytey;
    fieldsToSet.add("bytey");
    return this;
  }

  /** {@link TripleSub#booleany}. */
  public TripleSubBuilder with(boolean booleany) {
    this.booleany = booleany;
    fieldsToSet.add("booleany");
    return this;
  }

  /** {@link Base#there}. */
  public TripleSubBuilder with(Integer thereInBase) {
    this.thereInBase = thereInBase;
    fieldsToSet.add("thereInBase");
    return this;
  }

  /**
    * Sets all fields to the default value that the
    * <a href="https://docs.oracle.com/javase/specs/jvms/se8/jvms8.pdf">JVM specification</a>
    * defines in sections 2.3 and 2.4.
    */
  public void clear() {
    with(0L);
    with(0.0);
    withHereInSub(null);
    withDateInSub(null);
    with('\u0000');
    with((List) null);
    with((short) 0);
    with((Date) null);
    withHereInBase(null);
    with(0);
    with((SomeFieldClass) null);
    withTriple(null);
    with((Long) null);
    withThereInSub(null);
    with(0.0f);
    with((byte) 0);
    with(false);
    with((Integer) null);
  }

  /**
    * Sets all fields to default values predefined in the builder.<br /><br />
    * <b>NOTE:</b> all calls to the builder before this method is called will have no effect.
    * TODO implement default values!
    */
  public void _withDefaults() {
    with(longy); // TODO implement me!
    with(doubley); // TODO implement me!
    withHereInSub(hereInSub); // TODO implement me!
    withDateInSub(dateInSub); // TODO implement me!
    with(someChar); // TODO implement me!
    with(list); // TODO implement me!
    with(shorty); // TODO implement me!
    with(dateInBase); // TODO implement me!
    withHereInBase(hereInBase); // TODO implement me!
    with(hi); // TODO implement me!
    with(fieldWithBuilder); // TODO implement me!
    withTriple(triple); // TODO implement me!
    with(noLightInTheDark); // TODO implement me!
    withThereInSub(thereInSub); // TODO implement me!
    with(floaty); // TODO implement me!
    with(bytey); // TODO implement me!
    with(booleany); // TODO implement me!
    with(thereInBase); // TODO implement me!
  }

  /**
    * Allows to start building from an existing instance. If the instance you pass to this is
    * {@code null}, the call to this method will be ignored.
    */
  public void startingFrom(TripleSub instance) {
    if (instance != null) {
      clear();
      _result_tripleSub = instance;
    }
  }

  public TripleSub build() {
    if(fieldsToSet.contains("longy")) {
       setField("longy", TripleSub.class, longy);
    }
    if(fieldsToSet.contains("doubley")) {
       setField("doubley", TripleSub.class, doubley);
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
    if(fieldsToSet.contains("shorty")) {
       setField("shorty", TripleSub.class, shorty);
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
    if(fieldsToSet.contains("fieldWithBuilder")) {
       setField("fieldWithBuilder", Base.class, fieldWithBuilder);
    }
    if(fieldsToSet.contains("triple")) {
       setField("triple", TripleSub.class, triple);
    }
    if(fieldsToSet.contains("noLightInTheDark")) {
       setField("noLightInTheDark", TripleSub.class, noLightInTheDark);
    }
    if(fieldsToSet.contains("thereInSub")) {
       setField("there", Sub.class, thereInSub);
    }
    if(fieldsToSet.contains("floaty")) {
       setField("floaty", TripleSub.class, floaty);
    }
    if(fieldsToSet.contains("bytey")) {
       setField("bytey", TripleSub.class, bytey);
    }
    if(fieldsToSet.contains("booleany")) {
       setField("booleany", TripleSub.class, booleany);
    }
    if(fieldsToSet.contains("thereInBase")) {
       setField("there", Base.class, thereInBase);
    }

    return _result_tripleSub;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(_result_tripleSub, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
