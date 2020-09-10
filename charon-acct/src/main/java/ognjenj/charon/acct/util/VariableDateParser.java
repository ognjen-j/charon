package ognjenj.charon.acct.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VariableDateParser {
	public static LocalDateTime parseDateTimeWithVariablePadding(String dateTimeString, String format) {
		String modified = dateTimeString.replaceAll("( ){2,}", " ");
		return LocalDateTime.parse(modified, DateTimeFormatter.ofPattern(format));
	}
}
