package dataRepo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Util {
	public static SimpleDateFormat fmtDate = new SimpleDateFormat("yyyy-mm-dd", Locale.US);
	public static SimpleDateFormat fmtDatetime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.US);

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
		sb.insert(length - xs.length(), xs);
		for (int i = 0; i < length - xs.length(); i++) {
			sb.insert(i, pad);
		}
		return sb.toString();
	}
}
