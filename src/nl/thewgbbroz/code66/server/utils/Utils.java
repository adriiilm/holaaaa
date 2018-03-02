package nl.thewgbbroz.code66.server.utils;

public class Utils {
	private Utils() {
	}

	public static String formatTime(int secs) {
		if (secs < 60)
			return secs + " second(s)";

		double mins = (double) secs / 60d;
		if (mins < 60)
			return (int) mins + " minute(s)";

		double hrs = mins / 60d;
		if (hrs < 24)
			return (int) hrs + " hour(s)";

		double days = hrs / 24d;
		return (int) days + " day(s)";
	}
}
