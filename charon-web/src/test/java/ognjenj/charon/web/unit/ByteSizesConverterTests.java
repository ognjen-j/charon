package ognjenj.charon.web.unit;

import ognjenj.charon.web.util.ByteSizesConverter;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizesConverterTests {
    @Test
    public void testBytesToHumanReadable() {
        String convertedValue = ByteSizesConverter.bytesToHumanReadable(19451);
        Assert.assertTrue(convertedValue.startsWith("18."));
        Assert.assertTrue(convertedValue.endsWith(" kB"));
    }

    @Test
    public void testBytesToHumanReadableNegative() {
        String convertedValue = ByteSizesConverter.bytesToHumanReadable(-19451);
        Assert.assertTrue(convertedValue.startsWith("-18."));
        Assert.assertTrue(convertedValue.endsWith(" kB"));
    }

    @Test
    public void testHumanReadableToBytes() {
        Assert.assertEquals(1024L, ByteSizesConverter.humanReadableToBytes("1k"));
        Assert.assertEquals(1024L, ByteSizesConverter.humanReadableToBytes("1kB"));
        Assert.assertEquals(1024L, ByteSizesConverter.humanReadableToBytes("1kiB"));
        Assert.assertEquals(1024L * 1024L, ByteSizesConverter.humanReadableToBytes("1M"));
        Assert.assertEquals(2L * 1024L * 1024L, ByteSizesConverter.humanReadableToBytes("2MB"));
        Assert.assertEquals(3L * 1024L * 1024L * 1024L, ByteSizesConverter.humanReadableToBytes("3G"));
        Assert.assertEquals(3L * 1024L * 1024L * 1024L, ByteSizesConverter.humanReadableToBytes("3GiB"));
        Assert.assertEquals(2L * 1024L * 1024L * 1024L * 1024L, ByteSizesConverter.humanReadableToBytes("2TB"));
        Assert.assertEquals((long) (3.5 * 1024 * 1024 * 1024), ByteSizesConverter.humanReadableToBytes("3.5GB"));
        Assert.assertEquals(0L, ByteSizesConverter.humanReadableToBytes("3.5 GB"));
    }
}
