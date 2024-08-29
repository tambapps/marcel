package com.tambapps.marcel.charts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import marcel.lang.compile.BooleanDefaultValue;
import marcel.lang.compile.NullDefaultValue;
import marcel.util.primitives.collections.lists.DoubleArrayList;
import marcel.util.primitives.collections.lists.DoubleList;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class LineChart {

  private DoubleList x;
  private DoubleList y;

  public LineChart(@NullDefaultValue DoubleList x,
                   @NullDefaultValue DoubleList y,
                   @NullDefaultValue String title,
                   @NullDefaultValue String xLabel,
                   @NullDefaultValue String yLabel,
                   @BooleanDefaultValue boolean showGrid,
                   @BooleanDefaultValue boolean drawValues,
                   @NullDefaultValue String ySuffix) {
    this.x = x;
    this.y = y;
    this.title = title;
    this.xLabel = xLabel;
    this.yLabel = yLabel;
    this.showGrid = showGrid;
    this.drawValues = drawValues;
    this.ySuffix = ySuffix;
  }

  private String title;
  private String xLabel;
  private String yLabel;
  private boolean showGrid;
  private boolean drawValues;
  private String ySuffix;

  public LineChart x(List<? extends Number> x) {
    return x(toDoubleList(x));
  }

  public LineChart x(DoubleList x) {
    this.x = x;
    return this;
  }

  public LineChart y(List<? extends Number> y) {
    return x(toDoubleList(y));
  }

  public LineChart y(DoubleList y) {
    this.y = y;
    return this;
  }

  private static DoubleList toDoubleList(List<? extends Number> l) {
    DoubleList list = new DoubleArrayList(l.size());
    for (Number n : l) {
      list.add(n.doubleValue());
    }
    return list;
  }
}
