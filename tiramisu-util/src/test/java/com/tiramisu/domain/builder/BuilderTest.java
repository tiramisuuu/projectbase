package com.tiramisu.domain.builder;

import com.tiramisu.domain.SubBuilder;
import com.tiramisu.domain.objects.Sub;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BuilderTest {

  @Test
  public void TestClassBuilderShouldBuildCorrectObject() {
    // given
    List list = singletonList("sup my dude");
    String hereInSub = "not really here";
    String thereInSub = "not there either";
    String hereInBase = hereInSub + thereInSub;
    Integer thereInBase = 15;
    Date date = Date.from(Instant.ofEpochMilli(5L));

    // when
    Sub sub = SubBuilder.create()
        .with(list)
        .withHereInSub(hereInSub)
        .withHereInBase(hereInBase)
        .withThereInSub(thereInSub)
        .with(thereInBase)
        .with(date)
        .build();

    // then
    assertThat(sub.getList(), is(list));
    assertThat(sub.getHere(), is(hereInSub));
    assertThat(sub.getHereInBase(), is(hereInBase));
    assertThat(sub.getThere(), is(thereInSub));
    assertThat(sub.getThereInBase(), is(thereInBase));
    assertThat(sub.getDateInBase(), is(date));
  }

}