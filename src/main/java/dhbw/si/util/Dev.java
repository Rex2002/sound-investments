package dhbw.si.util;

import java.io.PrintStream;

public class Dev {
	public static boolean DEBUG = false;

	public static void disablePrinting() {
		// Code taken from here: https://stackoverflow.com/a/34839209/13764271
		// Also brings some slight performance improvements as mentioned in the Stack Overflow thread
		// It'd be better to just wrap all printing in `if (Dev.DEBUG) { ... }`, however, javafx also prints
		// certain information to System.out and I'm not sure how we could disable that (if we could at all)
		// Therefore, the simplest solution to prevent printing information that the user shouldn't see,
		// is to just override the System.out stream.
		PrintStream ps = new java.io.PrintStream(new java.io.OutputStream() {
				@Override public void write(int b) {}
			}) {
				@Override public void flush() {}
				@Override public void close() {}
				@Override public void write(int b) {}
				@Override public void write(byte[] b) {}
				@Override public void write(byte[] buf, int off, int len) {}
				@Override public void print(boolean b) {}
				@Override public void print(char c) {}
				@Override public void print(int i) {}
				@Override public void print(long l) {}
				@Override public void print(float f) {}
				@Override public void print(double d) {}
				@Override public void print(char[] s) {}
				@Override public void print(String s) {}
				@Override public void print(Object obj) {}
				@Override public void println() {}
				@Override public void println(boolean x) {}
				@Override public void println(char x) {}
				@Override public void println(int x) {}
				@Override public void println(long x) {}
				@Override public void println(float x) {}
				@Override public void println(double x) {}
				@Override public void println(char[] x) {}
				@Override public void println(String x) {}
				@Override public void println(Object x) {}
				@Override public java.io.PrintStream printf(String format, Object... args) { return this; }
				@Override public java.io.PrintStream printf(java.util.Locale l, String format, Object... args) { return this; }
				@Override public java.io.PrintStream format(String format, Object... args) { return this; }
				@Override public java.io.PrintStream format(java.util.Locale l, String format, Object... args) { return this; }
				@Override public java.io.PrintStream append(CharSequence csq) { return this; }
				@Override public java.io.PrintStream append(CharSequence csq, int start, int end) { return this; }
				@Override public java.io.PrintStream append(char c) { return this; }
			};

		System.setOut(ps);
		System.setErr(ps);
	}
}