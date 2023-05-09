package json;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Parser {
	final static String DEFAULT_FILENAME = "Unknown";

	private int lc = 1;
	private int bol = 0;
	private int cur = 0;
	private String filename = "Unknown";
	private String json = "";
	private int len = 0;

	private char curChar() {
		// System.out.println("Reading '" + json.charAt(cur) + "'");
		return json.charAt(cur);
	}

	private boolean startsWith(String prefix) {
		return json.startsWith(prefix, cur);
	}

	private void chopWord(String word) {
		// Assumes that cur + word.length() < len
		for (int i = 0; i < word.length(); i++)
			chopChar();
	}

	private Location curLoc() {
		return new Location(filename, lc, cur - bol);
	}

	private char chopChar() {
		char c = curChar();
		// System.out.println("Chopping '" + c + "'");
		if (c == '\n') {
			bol = cur;
			lc++;
		}
		cur++;
		return c;
	}

	private void trimLeft() {
		while (cur < len && Character.isWhitespace(curChar()))
			chopChar();
		// System.out.println("trimLeft done");
	}

	private String parseNumberStr(boolean checkDot) {
		assert Character.isDigit(curChar());
		StringBuilder sb = new StringBuilder(8);
		while (cur < len && Character.isDigit(curChar())) {
			sb.append(chopChar());
		}
		if (checkDot && cur < len && curChar() == '.') {
			sb.append(chopChar());
			sb.append(parseNumberStr(false));
		}
		return sb.toString();
	}

	private Number parseNumber() {
		String s = parseNumberStr(true);
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return Double.parseDouble(s);
		}
	}

	private JsonPrimitive<Number> parseNum() {
		return new JsonPrimitive<Number>(parseNumber());
	}

	private String parseString() {
		// Assumes that curChar() == '"'
		assert curChar() == '"';
		StringBuilder sb = new StringBuilder(32);
		chopChar();
		boolean escaped = false;
		while (cur < len && (curChar() != '"' || escaped)) {
			char c = chopChar();
			if (escaped)
				escaped = false;
			else if (c == '\\')
				escaped = true;

			if (!escaped)
				sb.append(c);
		}
		if (cur >= len)

		{
			throw new InvalidJson("Unexpected end of File", curLoc());
		} else {
			chopChar();
			return sb.toString();
		}
	}

	private JsonPrimitive<String> parseStr() {
		return new JsonPrimitive<String>(parseString());
	}

	private JsonPrimitive<?> parseObj() {
		// Assumes that curChar() == '{'
		assert curChar() == '{';
		HashMap<String, JsonPrimitive<?>> obj = new HashMap<>(128);
		chopChar();
		trimLeft();
		if (curChar() != '}') {
			while (cur < len) {
				if (curChar() != '"') {
					throw new InvalidJson(
							"JSON objects require keys to start with \". Instead received: '" + curChar() + "'",
							curLoc());
				}
				String key = parseString();
				trimLeft();
				if (cur >= len || curChar() != ':') {
					throw new InvalidJson(
							"Expected a ':' after key '" + key + "'. Instead received: '" + curChar() + "'", curLoc());
				}
				chopChar();
				trimLeft();
				JsonPrimitive<?> val = parse();
				obj.put(key, val);

				trimLeft();
				if (curChar() == '}') {
					break;
				} else if (curChar() == ',') {
					chopChar();
					trimLeft();
				} else {
					throw new InvalidJson(
							"Invalid symbol after key-value-pair. Expected ',' or '}'. Instead received: '"
									+ curChar() + "'",
							curLoc());
				}
			}
		}
		if (cur >= len) {
			throw new InvalidJson("Unexpected end of File", curLoc());
		} else {
			chopChar();
			return new JsonPrimitive<HashMap<String, JsonPrimitive<?>>>(obj);
		}
	}

	private JsonPrimitive<List<JsonPrimitive<?>>> parseArr() {
		// Assumes curChar() == '['
		assert curChar() == '[';
		List<JsonPrimitive<?>> arr = new ArrayList<>(128);
		chopChar();
		trimLeft();
		if (curChar() != ']') {
			while (cur < len) {
				JsonPrimitive<?> x = parse();
				arr.add(x);

				trimLeft();
				if (curChar() == ']') {
					break;
				} else if (curChar() == ',') {
					chopChar();
					trimLeft();
				} else {
					throw new InvalidJson("Invalid symbol after Array value. Expected ',' or ']'. Instead received: '"
							+ curChar() + "'", curLoc());
				}
			}
		}
		if (cur >= len) {
			throw new InvalidJson("Unexpected end of File", curLoc());
		} else {
			chopChar();
			return new JsonPrimitive<List<JsonPrimitive<?>>>(arr);
		}
	}

	private JsonPrimitive<?> parse() {
		trimLeft();
		if (cur < len) {
			JsonPrimitive<?> res = null;
			if (curChar() == '{') {
				res = parseObj();
			} else if (curChar() == '[') {
				res = parseArr();
			} else if (curChar() == '"') {
				res = parseStr();
			} else if (Character.isDigit(curChar())) {
				res = parseNum();
			} else if (startsWith("null")) {
				chopWord("null");
				res = JsonPrimitive.Null();
			} else if (startsWith("true")) {
				chopWord("true");
				res = new JsonPrimitive<Boolean>(true);
			} else if (startsWith("false")) {
				chopWord("false");
				res = new JsonPrimitive<Boolean>(false);
			} else {
				throw new InvalidJson("Unexpected Symbol: '" + curChar() + "'", curLoc());
			}
			return res;
		} else {
			throw new InvalidJson("Unexpected End of File", curLoc());
		}
	}

	public JsonPrimitive<?> parse(String json) {
		this.json = json;
		this.len = json.length();
		this.lc = 1;
		this.bol = 0;
		this.cur = 0;
		return parse();
	}

	public JsonPrimitive<?> parse(String filename, String json) {
		this.filename = filename;
		return parse(json);
	}

	/**
	 * Parses the given JSON-String and returns an object of type `T`
	 *
	 * @param <T>      The type of the return object
	 * @param filename Name of the JSON-File (only used for error messages)
	 * @param json     The JSON-String to parse
	 * @param func     Function to convert the JSON-object to an object of type `T`
	 * @return An object of type `T`
	 */
	public <T> T parse(String filename, String json, Function<JsonPrimitive<?>, T> func) {
		JsonPrimitive<?> res = parse(filename, json);

		// Check that we parsed the complete file
		trimLeft();
		if (cur < len)
			throw new InvalidJson("Expected end of File. Instead received: '" + curChar() + "'", curLoc());

		return func.apply(res);
	}

	/**
	 * Parses the given JSON-String and returns an object of type `T`
	 *
	 * @param <T>  The type of the return object
	 * @param json The JSON-String to parse
	 * @param func Function to convert the JSON-object to an object of type `T`
	 * @return An object of type `T`
	 */
	public <T> T parse(String json, Function<JsonPrimitive<?>, T> func) {
		return parse(DEFAULT_FILENAME, json, func);
	}

	public static <T> List<T> applyList(JsonPrimitive<?> l, Function<JsonPrimitive<?>, T> func) {
		List<JsonPrimitive<?>> list = l.asList();
		List<T> res = new ArrayList<>(list.size());
		for (JsonPrimitive<?> x : list) {
			res.add(func.apply(x));
		}
		return res;
	}

	public <T> List<T> parseList(String filename, String json, Function<JsonPrimitive<?>, T> func) {
		return parse(filename, json, l -> applyList(l, func));
	}

	public <T> List<T> parseList(String json, Function<JsonPrimitive<?>, T> func) {
		return parseList(DEFAULT_FILENAME, json, func);
	}
}
