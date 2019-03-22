package com.tiramisu.domain.builder;

import com.tiramisu.domain.TestClassBuilder;
import com.tiramisu.domain.test.TestClass;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;


public class BuilderTest {

  @Test
  public void TestClassBuilderShouldBuildCorrectObject() {
    // given
    List list = null;
    String here = "not really here";
    String there = "not there either";
    Date date = Date.from(Instant.ofEpochMilli(5L));

    // when
    TestClass testClass = TestClassBuilder.create()
        .withTestList(list)
        .withHere(here)
        .withThere(there)
        .withDate(date)
        .build();

    // then
    assertThat(testClass.getTestList(), Matchers.is(list));
    assertThat(testClass.getHere(), Matchers.is(here));
    assertThat(testClass.getThere(), Matchers.is(there));
    assertThat(testClass.getDate(), Matchers.is(date));
  }

}