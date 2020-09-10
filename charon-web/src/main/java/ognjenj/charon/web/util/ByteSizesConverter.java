package ognjenj.charon.web.util;

import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteSizesConverter {
	private static final String FLOAT_REGEX = "[+-]?\\d*\\.?\\d*";
	private static final String SIZE_MULTIPLIER_REGEX = "(T|G|M|k|TB|GB|MB|kB|TiB|GiB|MiB|kiB)";
	private static final Map<String, Integer> powers = Map.ofEntries(new SimpleEntry<>("k", 1),
			new SimpleEntry<>("kB", 1), new SimpleEntry<>("kiB", 1), new SimpleEntry<>("M", 2),
			new SimpleEntry<>("MB", 2), new SimpleEntry<>("MiB", 2), new SimpleEntry<>("G", 3),
			new SimpleEntry<>("GB", 3), new SimpleEntry<>("GiB", 3), new SimpleEntry<>("T", 4),
			new SimpleEntry<>("TB", 4), new SimpleEntry<>("TiB", 4));

	public static String bytesToHumanReadable(long bytes) {
		DecimalFormat format = new DecimalFormat("#.####");
		String[] suffixes = {"TB", "GB", "MB", "kB"};
		for (int i = 0; i < suffixes.length; i++) {
			double divisor = Math.pow(1024, suffixes.length - i);
			if (Math.abs(bytes / divisor) > 1) {
				return String.format("%s %s", format.format(bytes / divisor), suffixes[i]);
			}
		}
		return String.format("%s bytes", format.format(bytes));
	}

	public static long humanReadableToBytes(String humanReadable) {
		if (humanReadable.matches("^(" + FLOAT_REGEX + ")(" + SIZE_MULTIPLIER_REGEX + ")$")) {
			Matcher numericMatcher = Pattern.compile(FLOAT_REGEX).matcher(humanReadable);
			Matcher multiplierMatcher = Pattern.compile(SIZE_MULTIPLIER_REGEX).matcher(humanReadable);
			numericMatcher.find();
			multiplierMatcher.find();
			double numericPart = Double.parseDouble(numericMatcher.group());
			double multiplier = Math.pow(1024, powers.getOrDefault(multiplierMatcher.group(), 0));
			return (long) (numericPart * multiplier);
		} else if (humanReadable.matches("^(" + FLOAT_REGEX + ")$")) {
			return (long) (Double.parseDouble(humanReadable));
		} else {
			return 0;
		}
	}
}
