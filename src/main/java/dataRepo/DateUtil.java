package dataRepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil {
	public static SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static SimpleDateFormat fmtDatetime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
	public static DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static Calendar calFromDateStr(String dateStr) throws ParseException {
		Calendar cal = new GregorianCalendar();
		cal.setTime(fmtDate.parse(dateStr));
		return cal;
	}

	public static String formatDate(Calendar date) {
		return paddedParse(date.get(Calendar.YEAR), 4, '0') + "-" + paddedParse(date.get(Calendar.MONTH), 2, '0') + "-"
				+ paddedParse(date.get(Calendar.DAY_OF_MONTH), 2, '0');
	}

	public static String paddedParse(int x, int length, char pad) {
		StringBuffer sb = new StringBuffer(length);
		sb.setLength(length);
		String xs = String.valueOf(x);
		for (int i = 0; i < xs.length(); i++) {
			sb.setCharAt(i, xs.charAt(i));
		}
		for (int i = xs.length(); i < length; i++) {
			sb.setCharAt(i, pad);
		}
		return sb.toString();
	}

	public static Calendar localDateToCalendar(LocalDate ld) throws ParseException {
		return calFromDateStr(ld.format(dateTimeFmt));
	}

	public static LocalDate calendarToLocalDate(Calendar c) {
		if (c == null) return null;
		return LocalDate.ofInstant(c.toInstant(), ZoneId.systemDefault());
	}
}
