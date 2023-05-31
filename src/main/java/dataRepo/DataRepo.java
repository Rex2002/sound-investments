package dataRepo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import app.AppError;
import dataRepo.api.APIReq;
import dataRepo.api.AuthPolicy;
import dataRepo.api.HandledPagination;
import dataRepo.json.JsonPrimitive;
import dataRepo.json.Parser;

public class DataRepo {
	private static List<Stock> testStocks() {
		try {
			return List
					.of(
							new Stock("SAP SE", new SonifiableID("SAP", "XETRA"), DateUtil.calFromDateStr("1994-02-01"),
									DateUtil.calFromDateStr("2023-05-17")),
							new Stock("Siemens Energy AG", new SonifiableID("ENR", "XETRA"),
									DateUtil.calFromDateStr("2020-09-28"),
									DateUtil.calFromDateStr("2023-05-17")),
							new Stock("Dropbox Inc", new SonifiableID("1Q5", "XETRA"),
									DateUtil.calFromDateStr("2021-01-07"),
									DateUtil.calFromDateStr("2023-05-17")),
							new Stock("1&1 AG", new SonifiableID("1U1", "XETRA"), DateUtil.calFromDateStr("1998-12-04"),
									DateUtil.calFromDateStr("2023-05-17")),
							new Stock("123fahrschule SE", new SonifiableID("123F", "XETRA"),
									DateUtil.calFromDateStr("2021-11-02"),
									DateUtil.calFromDateStr("2023-05-17")));
		} catch (Exception e) {
			return List.of();
		}
	}

	private static List<Price> testPrices() {
		try {
			String fname = "./src/main/resources/TestPrices.json";
			String json = Files.readString(Path.of(fname));
			Parser parser = new Parser();
			List<Price> prices = parser.parse(fname, json).applyList(x -> {
				try {
					HashMap<String, JsonPrimitive<?>> m = x.asMap();
					Calendar startDay = DateUtil.calFromDateStr(m.get("datetime").asStr());
					Instant startTime = DateUtil.fmtDatetime.parse(m.get("datetime").asStr()).toInstant();
					Instant endTime = IntervalLength.HOUR.addToInstant(startTime);

					return new Price(startDay, startTime, endTime, m.get("open").asDouble(),
							m.get("close").asDouble(), m.get("low").asDouble(), m.get("high").asDouble());
				} catch (Exception e) {
					return null;
				}
			}, true);
			return prices;
		} catch (Exception e) {
			return List.of();
		}
	}

	public static enum API {
		LEEWAY,
		MARKETSTACK,
		TWELVEDATA;
	}

	public static enum IntervalLength {
		MIN,
		// MIN5,
		// MIN30,
		HOUR,
		DAY;

		public Instant addToInstant(Instant x) {
			long millis = 1000;
			millis *= switch (this) {
				case MIN -> 60;
				case HOUR -> 60 * 60;
				case DAY -> 12 * 60 * 60;
			};
			long res = x.toEpochMilli() + millis;
			return Instant.ofEpochMilli(res);
		}

		public String toString(API api) {
			return switch (this) {
				case MIN -> switch (api) {
					case LEEWAY -> "1m";
					default -> "1min";
				};
				case HOUR -> switch (api) {
					case MARKETSTACK -> "1hour";
					default -> "1h";
				};
				case DAY -> switch (api) {
					case LEEWAY -> null; // Leeway doesn't support 24hour intraday -> use /eod endpoint instead
					case MARKETSTACK -> "24hour";
					case TWELVEDATA -> "1day";
				};
			};
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
	private static APIReq apiLeeway = new APIReq("https://api.leeway.tech/api/v1/public/", apiToksLeeway,
			AuthPolicy.QUERY,
			"apitoken");
	private static APIReq apiMarketstack = getApiMarketstack();

	private static APIReq getApiMarketstack() {
		APIReq api = new APIReq("http://api.marketstack.com/v1/", apiToksMarketstack,
				AuthPolicy.QUERY,
				"access_key");
		api.setQueries("limit", "1000");
		return api.setPaginationHandler(json -> {
			JsonPrimitive<?> rest = json.asMap().get("data");
			HashMap<String, JsonPrimitive<?>> pageMap = json.asMap().get("pagination").asMap();
			Integer x = pageMap.get("offset").asInt() + pageMap.get("count").asInt();
			boolean done = x >= 5000; // pageMap.get("total").asInt();
			return new HandledPagination(rest, done);
		}, counter -> {
			api.setQuery("offset", Integer.toString(counter * 1000));
		});
	}

	private static List<Stock> stocks = new ArrayList<>(128);
	private static List<ETF> etfs = new ArrayList<>(128);
	private static List<Index> indices = new ArrayList<>(128);

	public static void init() throws AppError {
		// @Cleanup for Development & Testing only
		stocks = testStocks();
		return;

		// @Cleanup For debugging only
		// apiTwelvedata.setQuery("exchange", "XNYS");

		// try {
		// stocks = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "stocks")
		// .applyList(x -> new Stock(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));
		// setTradingPeriods(stocks);

		// etfs = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "etf")
		// .applyList(x -> new ETF(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));
		// setTradingPeriods(etfs);

		// indices = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "indices")
		// .applyList(x -> new Index(x.asMap().get("name").asStr(),
		// x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));
		// setTradingPeriods(indices);

		// System.out.println("Init done");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	private static <T extends Sonifiable> void setTradingPeriods(List<T> list) {
		// @Cleanup `i < 10` is only for debugging
		for (int i = 0; i < list.size() && i < 5; i++) {
			T s = list.get(i);
			try {
				HashMap<String, JsonPrimitive<?>> json = apiLeeway.getJSON(x -> x.asMap(),
						"general/tradingperiod/" + s.getId().toString());
				s.setEarliest(DateUtil.calFromDateStr(json.get("start").asStr()));
				s.setLatest(DateUtil.calFromDateStr(json.get("end").asStr()));
			} catch (Exception e) {
				// We assume tht if an error occured, that we don't have access to the given
				// symbol
				// This might be a wrong assumption
				System.out.println("Remove element");
				list.remove(i);
				i--;
			}
		}
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
					|| s.getId().symbol.toLowerCase().startsWith(prefix.toLowerCase())) {
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
			if (x.getId().symbol == symbol)
				return x;
		}
		return null;
	}

	public static List<Price> getPrices(SonifiableID s, Calendar start, Calendar end, IntervalLength interval) {
		return testPrices();

		// try {
		// String is = interval.toString(API.TWELVEDATA);
		// return apiTwelvedata.getJSON(x -> x.asMap().get("values"), "time_series",
		// "interval", is, "start_date",
		// Util.formatDate(start), "end_date", Util.formatDate(end), "timezone",
		// "UTC").applyList(x -> {
		// try {
		// HashMap<String, JsonPrimitive<?>> m = x.asMap();
		// Calendar startDay = Util.calFromDateStr(m.get("datetime").asStr());
		// Instant startTime =
		// Util.fmtDatetime.parse(m.get("datetime").asStr()).toInstant();
		// Instant endTime = interval.addToInstant(startTime);

		// return new Price(startDay, startTime, endTime, m.get("open").asDouble(),
		// m.get("close").asDouble(), m.get("low").asDouble(),
		// m.get("high").asDouble());
		// } catch (Exception e) {
		// return null;
		// }
		// }, true);

		// } catch (Exception e) {
		// // @Checkin Make sure we want to indicate errors like this
		// return null;
		// }
	}
}
