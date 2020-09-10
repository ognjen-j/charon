package ognjenj.charon.acct.util;

import org.junit.Assert;
import org.junit.Test;

public class BinaryUtilTest {
  @Test
  public void testIntToBinaryConversion1() {
    int i = 50;
    byte[] result = {50, 0, 0, 0};
    byte[] converted = BinaryUtil.longToBinaryArray(i, 4, false);
    Assert.assertArrayEquals(result, converted);
  }

  @Test
  public void testIntToBinaryConversion2() {
    int i = 50;
    byte[] result = {0, 0, 0, 50};
    byte[] converted = BinaryUtil.longToBinaryArray(i, 4, true);
    Assert.assertArrayEquals(result, converted);
  }

  @Test
  public void testIntToBinaryConversion3() {
    int i = 4128;
    byte[] expected = {32, 16, 0, 0};
    byte[] converted = BinaryUtil.longToBinaryArray(i, 4, false);
    Assert.assertArrayEquals(expected, converted);
  }

  @Test
  public void testIntToBinaryConversion4() {
    int i = 4128;
    byte[] expected = {0, 0, 16, 32};
    byte[] converted = BinaryUtil.longToBinaryArray(i, 4, true);
    Assert.assertArrayEquals(expected, converted);
  }
}
