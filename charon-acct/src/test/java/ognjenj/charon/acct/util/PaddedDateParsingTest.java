package ognjenj.charon.acct.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import org.junit.Assert;
import org.junit.Test;

public class PaddedDateParsingTest {
	@Test
	public void testDateStringsWithPadding() {
		String formatString = "EEE LLL d H:mm:ss yyyy";
		String dateString1 = "Sat Sep  5 13:27:43 2020";
		String dateString2 = "Sat Sep 5 13:27:43 2020";
		String dateString3 = "Sat Sep  19 13:27:43 2020";
		LocalDateTime date1 = VariableDateParser.parseDateTimeWithVariablePadding(dateString1, formatString);
		LocalDateTime date2 = VariableDateParser.parseDateTimeWithVariablePadding(dateString2, formatString);
		LocalDateTime date3 = VariableDateParser.parseDateTimeWithVariablePadding(dateString3, formatString);
		Assert.assertEquals(date1, date2);
		Assert.assertEquals(date3.get(ChronoField.DAY_OF_MONTH), 19);
	}
}
