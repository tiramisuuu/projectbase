package com.tiramisu.domain;
import com.tiramisu.domain.objects.DoubleSub;
import com.tiramisu.domain.objects.Sub;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.lang.String;
import java.lang.Long;
import java.util.List;

@SuppressWarnings("unused")
public final class DoubleSubBuilder {

  /** This is a utility class. */
  private DoubleSubBuilder() {}

  private DoubleSub doubleSub = new DoubleSub();
  private String there;
  private String here;
  private Long allAlone;
  private List list;
  private String everywhere;

  private Set<String> fieldsToSet = new HashSet<>();

  public static DoubleSubBuilder create() {
    return new DoubleSubBuilder();
  }

  /** {@link Sub#there}. */
  public DoubleSubBuilder withThere(String there) {
    this.there = there;
    fieldsToSet.add("there");
    return this;
  }

  /** {@link Sub#here}. */
  public DoubleSubBuilder withHere(String here) {
    this.here = here;
    fieldsToSet.add("here");
    return this;
  }

  /** {@link DoubleSub#allAlone}. */
  public DoubleSubBuilder withAllAlone(Long allAlone) {
    this.allAlone = allAlone;
    fieldsToSet.add("allAlone");
    return this;
  }

  /** {@link Sub#list}. */
  public DoubleSubBuilder withList(List list) {
    this.list = list;
    fieldsToSet.add("list");
    return this;
  }

  /** {@link DoubleSub#everywhere}. */
  public DoubleSubBuilder withEverywhere(String everywhere) {
    this.everywhere = everywhere;
    fieldsToSet.add("everywhere");
    return this;
  }

  public DoubleSub build() {
    if(fieldsToSet.contains("there")) {
       setField("there", Sub.class, there);
    }
    if(fieldsToSet.contains("here")) {
       setField("here", Sub.class, here);
    }
    if(fieldsToSet.contains("allAlone")) {
       setField("allAlone", DoubleSub.class, allAlone);
    }
    if(fieldsToSet.contains("list")) {
       setField("list", Sub.class, list);
    }
    if(fieldsToSet.contains("everywhere")) {
       setField("everywhere", DoubleSub.class, everywhere);
    }

    return doubleSub;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(doubleSub, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
