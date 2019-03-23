package com.tiramisu.domain.objects;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class Sub extends Base {

  private String here;

  private String there;

  private String date;

  private List<ZonedDateTime> list;

  private char someChar;
}
