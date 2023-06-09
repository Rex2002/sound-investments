package dhbw.si.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author V. Richter
 */
public class DateUtil {
	public static final SimpleDateFormat fmtDate       = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static final SimpleDateFormat fmtDatetime   = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
	public static final SimpleDateFormat germanDateFmt = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
	public static final int germanDateFmtExpectedLen   = 10;
	public static final DateTimeFormatter dateTimeFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static Calendar calFromDateTimeStr(String dateTimeStr) throws ParseException {
		Calendar cal = new GregorianCalendar();
		cal.setTime(fmtDatetime.parse(dateTimeStr));
		return cal;
	}

	public static Calendar calFromDateStr(String dateStr) throws ParseException {
		Calendar cal = new GregorianCalendar();
		cal.setTime(fmtDate.parse(dateStr));
		return cal;
	}

	public static String formatDateGerman(Calendar date) {
		return paddedParse(date.get(Calendar.DAY_OF_MONTH), 2, '0') + "." + paddedParse(date.get(Calendar.MONTH) + 1, 2, '0') + "." + paddedParse(date.get(Calendar.YEAR), 4, '0');
	}

	public static String formatDate(Calendar date) {
		return paddedParse(date.get(Calendar.YEAR), 4, '0') + "-" + paddedParse(date.get(Calendar.MONTH) + 1, 2, '0') + "-" + paddedParse(date.get(Calendar.DAY_OF_MONTH), 2, '0');
	}

	public static String paddedParse(int x, int length, char pad) {
		StringBuffer sb = new StringBuffer(length);
		sb.setLength(length);
		String xs = String.valueOf(x);
		for (int i = length - xs.length(), j = 0; i < length; i++, j++) {
			sb.setCharAt(i, xs.charAt(j));
		}
		for (int i = 0; i < length - xs.length(); i++) {
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

	public static LocalDate localDateFromGermanDateStr(String dateStr) throws ParseException {
		if (dateStr.length() != germanDateFmtExpectedLen)
			throw new ParseException("Invalid length", 0);
		return LocalDate.ofInstant(germanDateFmt.parse(dateStr).toInstant(), ZoneId.systemDefault());
	}

	public static LocalDate getYearZeroDate(){
		return LocalDate.ofYearDay(0, 1);
	}
}