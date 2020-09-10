package ognjenj.charon.web.unit;

import org.apache.commons.codec.digest.Crypt;
import org.junit.Assert;
import org.junit.Test;

public class CryptTests {
  @Test
  public void testCrypt() {
    Assert.assertEquals(
        Crypt.crypt(
            "ognjen",
            "$6$1234567$9qFfSaGmaQD/DhJh48k7BBDjxlpKAvGbKv6F.0LAnRVKfxP1Bm7pclhzgADBx61TzDQcr6fMgPeF80mb2OILn0"),
        "$6$1234567$9qFfSaGmaQD/DhJh48k7BBDjxlpKAvGbKv6F.0LAnRVKfxP1Bm7pclhzgADBx61TzDQcr6fMgPeF80mb2OILn0");
  }
}
