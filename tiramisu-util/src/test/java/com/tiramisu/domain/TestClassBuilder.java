package com.tiramisu.domain;
import com.tiramisu.domain.test.Base;
import com.tiramisu.domain.test.TestClass;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.lang.String;
import java.util.List;
import java.util.Date;

@SuppressWarnings("unused")
public final class TestClassBuilder {

  /** This is a utility class. */
  private TestClassBuilder() {}

  private TestClass testClass = new TestClass();
  private String there;
  private List testList;
  private Date date;
  private String here;

  private Set<String> fieldsToSet = new HashSet<>();

  public static TestClassBuilder create() {
    return new TestClassBuilder();
  }

  /** {@link TestClass#there}. */
  public TestClassBuilder withThere(String there) {
    this.there = there;
    fieldsToSet.add("there");
    return this;
  }

  /** {@link TestClass#testList}. */
  public TestClassBuilder withTestList(List testList) {
    this.testList = testList;
    fieldsToSet.add("testList");
    return this;
  }

  /** {@link Base#date}. */
  public TestClassBuilder withDate(Date date) {
    this.date = date;
    fieldsToSet.add("date");
    return this;
  }

  /** {@link TestClass#here}. */
  public TestClassBuilder withHere(String here) {
    this.here = here;
    fieldsToSet.add("here");
    return this;
  }

  public TestClass build() {
    if(fieldsToSet.contains("there")) {
       setField("there", TestClass.class, there);
    }
    if(fieldsToSet.contains("testList")) {
       setField("testList", TestClass.class, testList);
    }
    if(fieldsToSet.contains("date")) {
       setField("date", Base.class, date);
    }
    if(fieldsToSet.contains("here")) {
       setField("here", TestClass.class, here);
    }

    return testClass;
  }

  private void setField(String fieldName, Class<?> type, Object fieldValue) {
    try {
      Field objectField = type.getDeclaredField(fieldName);
      objectField.setAccessible(true);
      objectField.set(testClass, fieldValue);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
