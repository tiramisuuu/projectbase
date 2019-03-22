package com.tiramisu.domain.test;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class TestClass extends Base {

  private String here;

  private String there;

  private List<ZonedDateTime> testList;
}
