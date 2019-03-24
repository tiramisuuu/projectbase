package com.tiramisu.domain.objects;

import java.util.Date;

public class Base {

  private Date date;

  private String here;

  private Integer there;

  private int hi;

  private SomeFieldClass fieldWithBuilder;

  public Date getDateInBase() {
    return date;
  }

  public String getHereInBase() {
    return here;
  }

  public Integer getThereInBase() {
    return there;
  }
}
