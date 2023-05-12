package dataRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import apiTest.APIReq;
import apiTest.AuthPolicy;
import json.JsonPrimitive;

public class DataRepo {
	private static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-mm-dd", Locale.US);

	private static List<Stock> testStocks() {
		try {
			return List
					.of(new Stock("SAP SE", "SAP", "XETRA", fmt.parse("1994-02-01"), fmt.parse("2023-05-12")),
							new Stock("Siemens Energy AG", "ENR", "XETRA", fmt.parse("2020-09-28"),
									fmt.parse("2023-05-12")));
		} catch (Exception e) {
			return List.of();
		}
	}

	public static enum FilterFlag {
		STOCK(1 << 0),
		ETF(1 << 1),
		INDEX(1 << 2),
		ALL((1 << 0) | (1 << 1) | (1 << 2));

		private final int x;

		FilterFlag(int x) {
			this.x = x;
		}

		public int getVal() {
			return x;
		}
	}

	public static int getFilterVal(FilterFlag... flags) {
		int val = 0;
		for (FilterFlag flag : flags) {
			val |= flag.getVal();
		}
		return val;
	}

	private static final String[] apiToksLeeway = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm",
			"r7splaijduabfpcu2z2l14", "o5npdx6elm2pcpp395uaun", "2ja5gszii8g63hzjd41x78" };
	private static final String[] apiToksMarketstack = { "4b6a78c092537f07bbdedff8f134372d",
			"0c2e8a9c96f2a74c0049f4b662f47b40",
			"621fc5e0add038cc7d9697bcb7f15caa", "4312dfd8788579ec14ee9e9c9bec4557",
			"0a99047c49080d975013978d3609ca9e" };
	private static final String[] apiToksTwelvedata = { "04ed9e666cbb4873ac6d29651e2b4d7e",
			"7e51ed4d1d5f4cbfa6e6bcc8569c1e54",
			"98888ec975884e98a9555233c3dd59da", "af38d2454c2c4a579768b8262d3e039e",
			"facbd6808e6d436e95c4935ab8cc082e" };

	private static APIReq apiTwelvedata = new APIReq("https://api.twelvedata.com/", apiToksTwelvedata, AuthPolicy.QUERY,
			"apikey");

	private static List<Stock> stocks = new ArrayList<>(128);
	private static List<ETF> etfs = new ArrayList<>(128);
	private static List<Index> indices = new ArrayList<>(128);

	public static void init() {
		stocks = testStocks();

		// For debugging only: @Cleanup
		// apiTwelvedata.setQuery("exchange", "XNYS");

		// try {
		// stocks = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "stocks")
		// .applyList(x -> new Stock(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));

		// // @Cleanup Don't repeat the same code 3 times here
		// // @Performance this could be done much faster by using an unsorted remove
		// // The most performant option would probably be to write a custom ArrayList,
		// // which only does unsorted removes and keeps an internal removedAmount
		// counter
		// for (int i = 0; i < stocks.size(); i++) {
		// Sonifiable x = stocks.get(i);
		// HashMap<String, JsonPrimitive<?>> m = apiTwelvedata.getJSON(json ->
		// json.asMap(),
		// "earliest_timestamp", "symbol", x.symbol, "timezone", "UTC", "interval",
		// "1day");
		// if (m.get("status").asStr() != "ok") {
		// // If an error happens, I assume that we don't have access to the given
		// symbol
		// // with our free plan
		// // this might be a wrong assumption in some or even many cases
		// stocks.remove(i);
		// } else {
		// x.earliest = new Date(m.get("unix_time").asLong());
		// System.out.println(x);
		// }
		// }

		// etfs = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "etf")
		// .applyList(x -> new ETF(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));
		// for (int i = 0; i < etfs.size(); i++) {
		// Sonifiable x = etfs.get(i);
		// HashMap<String, JsonPrimitive<?>> m = apiTwelvedata.getJSON(json ->
		// json.asMap(),
		// "earliest_timestamp", "symbol", x.symbol, "timezone", "UTC", "interval",
		// "1day");
		// if (m.get("status").asStr() != "ok") {
		// etfs.remove(i);
		// } else {
		// x.earliest = new Date(m.get("unix_time").asLong());
		// System.out.println(x);
		// }
		// }

		// indices = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "indices")
		// .applyList(x -> new Index(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));
		// for (int i = 0; i < indices.size(); i++) {
		// Sonifiable x = indices.get(i);
		// HashMap<String, JsonPrimitive<?>> m = apiTwelvedata.getJSON(json ->
		// json.asMap(),
		// "earliest_timestamp", "symbol", x.symbol, "timezone", "UTC", "interval",
		// "1day");
		// if (m.get("status").asStr() != "ok") {
		// indices.remove(i);
		// } else {
		// x.earliest = new Date(m.get("unix_time").asLong());
		// System.out.println(x);
		// }
		// }

		// System.out.println("Amount of stocks: " + stocks.size());
		// System.out.println("Amount of etfs: " + etfs.size());
		// System.out.println("Amount of indices: " + indices.size());
		// System.out.println("Init done");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static List<Sonifiable> findByPrefix(String prefix, FilterFlag... filters) {
		int flag = getFilterVal(filters);
		List<Sonifiable> l = new ArrayList<>(128);
		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			findByPrefix(prefix, stocks, l);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			findByPrefix(prefix, etfs, l);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			findByPrefix(prefix, indices, l);
		return l;
	}

	private static void findByPrefix(String prefix, List<? extends Sonifiable> src, List<Sonifiable> dst) {
		for (Sonifiable s : src) {
			if (s.name.toLowerCase().startsWith(prefix.toLowerCase())
					|| s.symbol.toLowerCase().startsWith(prefix.toLowerCase())) {
				dst.add(s);
			}
		}
	}

	public static List<Sonifiable> getAll(FilterFlag... filters) {
		int flag = getFilterVal(filters);
		List<Sonifiable> l = new ArrayList<>(128);
		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			l.addAll(stocks);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			l.addAll(etfs);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			l.addAll(indices);
		return l;
	}

	public static Stock getStock(String symbol) {
		return getSonifable(symbol, stocks);
	}

	public static ETF getETF(String symbol) {
		return getSonifable(symbol, etfs);
	}

	public static Index getIndex(String symbol) {
		return getSonifable(symbol, indices);
	}

	private static <T extends Sonifiable> T getSonifable(String symbol, List<T> list) {
		for (T x : list) {
			if (x.symbol == symbol)
				return x;
		}
		return null;
	}
}
