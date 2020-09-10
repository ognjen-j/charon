package ognjenj.charon.acct.util;

import org.junit.Assert;
import org.junit.Test;

public class StringIpTest {
    @Test
    public void testRouteConversion1() {
        String converted = StringIpHelper.transformRouteNotation("192.168.10.0/24 192.168.10.1");
        Assert.assertEquals("192.168.10.0 255.255.255.0 192.168.10.1", converted);
    }

    @Test
    public void testRouteConversion2() {
        String converted = StringIpHelper.transformRouteNotation("192.168.10.10/21 192.168.10.1");
        Assert.assertEquals("192.168.8.0 255.255.248.0 192.168.10.1", converted);
    }
}
