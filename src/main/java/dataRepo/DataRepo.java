package dataRepo;

import app.AppError;
import dataRepo.api.APIReq;
import dataRepo.api.AuthPolicy;
import dataRepo.api.PaginationHandler;
import dataRepo.json.JsonPrimitive;
import dataRepo.json.Parser;
import util.ArrayFunctions;
import util.DateUtil;
import util.FutureList;
import util.UnorderedList;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataRepo {
	private static final boolean GET_PRICES_DYNAMICALLY    = true;
	private static final boolean GET_EXCHANGES_DYNAMICALLY = false;
	private static final boolean UPDATE_SONIFIABLES        = true;

	private static final List<String> DEFAULT_EXCHANGES = List.of("NYSE", "NASDAQ", "XETRA", "F", "BE", "MU");

	private static final String[] apiToksLeeway = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm", "r7splaijduabfpcu2z2l14",
			"o5npdx6elm2pcpp395uaun", "2ja5gszii8g63hzjd41x78", "ftd5l4hscm9biueu5ptptr", "42dreongzzo5yqkyg2r3cr",
			"axt2o7xkx1cxd9nsinqfu6", "rumn8riwt3oo18z1cluvz1", "9rjaye34gvir42j113qf7r", "6gqkwy7si5lv1tgicp28le", "6yylrshzzhlsdhf26ysmq9",
			"xaqiip44tivsl5a963zvft", "gyvss5g25tfcc5jt9oniob", "amcx442becc3lp9ffm4vav", "dknrw7xj2fozz383i8hlwk", "9ajr7myfoyaowast33fmas",
			"1a7byxsvpyleppn373eel2", "hgffb6jusiy2yb3121wxik", "oogf9s7g8oqg9tg4fxc2yr"
	};

	// to prevent race conditions when making requests via the same APIReq object in different threads,
	// we create a new APIReq object for each sequential task
	private static APIReq getApiLeeway() {
		return new APIReq("https://api.leeway.tech/api/v1/public/", apiToksLeeway, AuthPolicy.QUERY, "apitoken",
			null, null, APIReq.rateLimitErrHandler(res -> {
				if (res.statusCode() == 429) return true;
				if (res.body().startsWith("Your limit of")) return true;
				return false;
			}));
	}

	// @Scalability If more than one component would need to react to updated data,
	// a single boolean flag would not be sufficient of course. Since we know,
	// however, that only the StateManager reacts to this information, having a
	// single boolean flag is completely sufficient
	public static AtomicBoolean updatedData        = new AtomicBoolean(false);
	private static ThreadPoolExecutor threadPool   = new ThreadPoolExecutor(16, 64, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	private static UnorderedList<Sonifiable> stocks  = new UnorderedList<>(128);
	private static UnorderedList<Sonifiable> etfs    = new UnorderedList<>(128);
	private static UnorderedList<Sonifiable> indices = new UnorderedList<>(128);

	private static List<Price> testPrices() {
		try {
			String fname  = "./src/main/resources/TestData/TestPrices.json";
			String json   = Files.readString(Path.of(fname));
			Parser parser = new Parser();
			List<Price> prices = parser.parse(fname, json).applyList(x -> {
				try {
					HashMap<String, JsonPrimitive<?>> m = x.asMap();
					Calendar startDay = DateUtil.calFromDateStr(m.get("datetime").asStr());
					Instant startTime = DateUtil.fmtDatetime.parse(m.get("datetime").asStr()).toInstant();
					Instant endTime   = IntervalLength.HOUR.addToInstant(startTime);

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

	// Note: You shouldn't call this function twice
	public static void init() throws AppError {
		try {
			// Read cached stocks
			stocks  = readFromJSON("stocks");
			etfs    = readFromJSON("etfs");
			indices = readFromJSON("indices");

			if (UPDATE_SONIFIABLES)
				updateSonifiablesList();
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError(e.getMessage());
		}
	}

	public static Sonifiable[] findByPrefix(int startIdx, int length, String prefix, FilterFlag... filters) {
		int filledLen = 0;
		int flag = FilterFlag.getFilterVal(filters);
		Sonifiable[] l = new Sonifiable[length];

		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			filledLen = findByPrefix(prefix, stocks,  startIdx,             l, filledLen);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			filledLen = findByPrefix(prefix, etfs,    startIdx - filledLen, l, filledLen);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			filledLen = findByPrefix(prefix, indices, startIdx - filledLen, l, filledLen);

		if (filledLen < length) {
			Sonifiable[] newL = new Sonifiable[filledLen];
			System.arraycopy(l, 0, newL, 0, filledLen);
			return newL;
		} else {
			return l;
		}
	}

	private static int findByPrefix(String prefix, List<? extends Sonifiable> src, int srcOffset, Sonifiable[] dst, int dstOffset) {
		if (dstOffset == dst.length) return dstOffset;
		int i = 0;
		for (Sonifiable s : src) {
			if (s != null && (s.name.toLowerCase().startsWith(prefix.toLowerCase())
					           || s.getId().symbol.toLowerCase().startsWith(prefix.toLowerCase()))
					      && i++ >= srcOffset) {
				dst[dstOffset++] = s;
				if (dstOffset == dst.length) break;
			}
		}
		return dstOffset;
	}

	public static List<Sonifiable> getAll(FilterFlag... filters) {
		int flag = FilterFlag.getFilterVal(filters);
		List<Sonifiable> l = new ArrayList<>(128);
		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			l.addAll(stocks);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			l.addAll(etfs);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			l.addAll(indices);
		return l;
	}

	public static String getSonifiableName(SonifiableID id) {
		Sonifiable s = getSonifable(id, stocks);
		if (s == null) s = getSonifable(id, etfs);
		if (s == null) s = getSonifable(id, indices);
		if (s == null) return null;
		else return s.getName();
	}

	private static Sonifiable getSonifable(SonifiableID id, List<Sonifiable> list) {
		for (Sonifiable x : list) {
			if (x.getId() == id)
				return x;
		}
		return null;
	}

	public static Future<List<Price>> getPrices(SonifiableID s, Calendar start, Calendar end, IntervalLength interval) {
		return threadPool.submit(() -> {
			if (!GET_PRICES_DYNAMICALLY) return testPrices();

			APIReq apiLeeway = getApiLeeway();
			Calendar[] startEnd = {start, end};

			// TODO: Add de-facto pagination to price-requests to allow parallelization
			// Idea for doing this: See how long the date-range in the first response was
			// then split the rest of the range for start->end into chunks of that range
			// To achieve this, the signatures for pagination handlers most certainly have to be changed again
			try {
				List<Price> out = new ArrayList<>(1024);
				Calendar earliestDay = startEnd[1];
				// Because leeway's range is exclusive, we need to decrease the startdate that we put in the request and keep the same for comparison of the loop
				Calendar startCmp = (Calendar) startEnd[0].clone();
				startEnd[0].roll(Calendar.DATE, false);
				// Leeway throws error if range is more than 600 days -> is handled in StateManager determining the Interval

				boolean isEoD = interval == IntervalLength.DAY;
				String endpoint = (isEoD ? "historicalquotes/" : "intraday/") + s;
				String[] intervalQueries = {"interval", interval.toString(API.LEEWAY)};
				do {
					// We increase end by 1, because Leeway's range is exclusive
					// and after each iteration end = earliestDay and it might be that we missed some data from that day
					startEnd[1].roll(Calendar.DATE, true);
					String[] queries = {"from", DateUtil.formatDate(startEnd[0]), "to", DateUtil.formatDate(startEnd[1])};
					if (!isEoD) queries = ArrayFunctions.add(queries, intervalQueries);

					List<Price> prices = apiLeeway.getJSONList(endpoint,
							json -> {
								try {
									HashMap<String, JsonPrimitive<?>> m = json.asMap();
									Calendar day;
									Instant startTime;
									Instant endTime;
									if (isEoD) {
										day = DateUtil.calFromDateStr(m.get("date").asStr());
										day.set(Calendar.HOUR_OF_DAY, 0);
										startTime = day.toInstant();
										day.set(Calendar.HOUR_OF_DAY, 23);
										endTime = day.toInstant();
									} else {
										day = DateUtil.calFromDateTimeStr(m.get("datetime").asStr());
										startTime = new Timestamp(m.get("timestamp").asLong()).toInstant();
										endTime = interval.addToInstant(startTime);
									}
									return new Price(day, startTime, endTime, m.get("open").asDouble(), m.get("close").asDouble(), m.get("low").asDouble(), m.get("high").asDouble());
								} catch (Exception e) {
									return null;
								}
							}, true, queries);

					if (prices.isEmpty()) break;
					Calendar nextEarliestDay = prices.get(0).getDay();
					if (nextEarliestDay.equals(earliestDay)) // we would get trapped in an infinite loop now
						break;
					earliestDay = nextEarliestDay;
					startEnd[1] = (Calendar) earliestDay.clone();
					out.addAll(0, prices);
				} while (startCmp.before(earliestDay));

				ArrayFunctions.rmDuplicates(out, 300, (x, y) -> x.getStart().equals(y.getStart()));
				return out;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in getting Price-Data for " + s + " from " + DateUtil.formatDate(start) + " to " + DateUtil.formatDate(end));
				return null;
			}
		});
	}

	public static void updateSonifiablesList() {
		threadPool.submit(() -> {
			try {
				List<String> exchanges = DEFAULT_EXCHANGES;
				if (GET_EXCHANGES_DYNAMICALLY) {
					APIReq apiLeeway = getApiLeeway();
					exchanges = apiLeeway.getJSONList("general/exchanges", json -> json.asMap().get("Code").asStr());
				}

				FutureList<List<ExtendedSonifiable>> fl = new FutureList<>(exchanges.size());
				for (int i = 0; i < exchanges.size(); i++) {
					String exchange = exchanges.get(i);
					fl.add(threadPool.submit(() -> {
						try {
							return getApiLeeway().getJSONList("general/symbols/" + exchange, json -> {
								HashMap<String, JsonPrimitive<?>> m = json.asMap();
								JsonPrimitive<?> typeJson = m.get("Type");
								SonifiableType type = SonifiableType.fromString(typeJson.isNull() ? "" : typeJson.asStr());
								if (type == SonifiableType.NONE) return null;
								return new ExtendedSonifiable(type, new Sonifiable(
									m.get("Name").asStr(), new SonifiableID(m.get("Code").asStr(), exchange)
								));
							}, true);
						} catch (Exception e) {
							return null;
						}
					}));
				}

				UnorderedList<Sonifiable> newStocks  = new UnorderedList<>(4096);
				UnorderedList<Sonifiable> newEtfs    = new UnorderedList<>(2048);
				UnorderedList<Sonifiable> newIndices = new UnorderedList<>(2048);
				Object[] allSonifiables = fl.getAll();
				for (Object sonifiables : allSonifiables) {
					for (ExtendedSonifiable s : (List<ExtendedSonifiable>) sonifiables) {
						switch (s.type) {
							// @Performance it's absurd to create new objects here
							// solution would be to have stocks/etfs/indices be lists of sonifiables
							case STOCK -> newStocks.add(s.sonifiable);
							case ETF -> newEtfs.add(s.sonifiable);
							case INDEX -> newIndices.add(s.sonifiable);
							case NONE -> {
								System.out.println("Unreachable: We shouldn't be able to still get unidentified Sonifiables after awaiting all futures");
							}
						}
					}
				}

				// Cache data for future use
				stocks = newStocks;
				etfs = newEtfs;
				indices = newIndices;
				writeToJSON("stocks",  ArrayFunctions.toStringArr(stocks.getArray(),  s -> ((Sonifiable) s).toJSON(), true));
				writeToJSON("etfs",    ArrayFunctions.toStringArr(etfs.getArray(),    s -> ((Sonifiable) s).toJSON(), true));
				writeToJSON("indices", ArrayFunctions.toStringArr(indices.getArray(), s -> ((Sonifiable) s).toJSON(), true));
				System.out.println("Wrote new data into cached files");

				System.out.println("New stocks length: " + stocks.size());
				System.out.println("New etfs length: " + etfs.size());
				System.out.println("New indices length: " + indices.size());
				updatedData.set(true);
			} catch (Throwable e) {
				// we intentionally ignore this error, as it's not effecting the user in this moment
				e.printStackTrace();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static<T extends Sonifiable> UnorderedList<T> readFromJSON(String filename) throws AppError {
		try {
			Parser parser = new Parser();
			String str = Files.readString(Paths.get(DataRepo.class.getResource("/Data/" + filename + ".json").toURI()));
			JsonPrimitive<?> json = parser.parse(str);
			return json.applyList(x -> {
				return (T) new Sonifiable(x.asMap().get("name").asStr(), new SonifiableID(
					x.asMap().get("id").asMap().get("symbol").asStr(),
					x.asMap().get("id").asMap().get("exchange").asStr()));
			}, new UnorderedList<>(json.asList().size()), true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError("Die gespeicherten Daten für '" + filename + "' konnten nicht gelesen werden. Stelle sicher, dass keine App-internen Dateien verschoben oder gelöscht wurden.");
		}
	}

	private static <T> void writeToJSON(String filename, String data) throws AppError {
		try {
			URL url = DataRepo.class.getResource("/Data/" + filename + ".json");
			Path path = Paths.get(url.toURI());
			// System.out.println("data: " + data);
			Files.write(path, data.getBytes(), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError("Die neuen Daten für '" + filename + "' konnten nicht gespeichert werden.");
		}
	}
}