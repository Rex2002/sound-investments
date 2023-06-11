package dhbw.si.dataRepo;

import dhbw.si.app.AppError;
import dhbw.si.dataRepo.api.APIReq;
import dhbw.si.dataRepo.api.AuthPolicy;
import dhbw.si.dataRepo.json.JsonPrimitive;
import dhbw.si.dataRepo.json.Parser;
import dhbw.si.util.ArrayFunctions;
import dhbw.si.util.DateUtil;
import dhbw.si.util.FutureList;
import dhbw.si.util.UnorderedList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataRepo {
	private static final boolean GET_PRICES_DYNAMICALLY    = true;
	private static final boolean GET_EXCHANGES_DYNAMICALLY = false;
	private static final boolean UPDATE_SONIFIABLES        = true;

	private static final int          UPDATE_PERIOD_IN_DAYS = 5;
	private static final List<String> DEFAULT_EXCHANGES     = List.of("NYSE", "NASDAQ", "XETRA", "F", "BE", "MU");

	private static final String[] apiToksLeeway = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm", "r7splaijduabfpcu2z2l14",
			"o5npdx6elm2pcpp395uaun", "2ja5gszii8g63hzjd41x78", "ftd5l4hscm9biueu5ptptr", "42dreongzzo5yqkyg2r3cr",
			"axt2o7xkx1cxd9nsinqfu6", "rumn8riwt3oo18z1cluvz1", "9rjaye34gvir42j113qf7r", "6gqkwy7si5lv1tgicp28le", "6yylrshzzhlsdhf26ysmq9",
			"xaqiip44tivsl5a963zvft", "gyvss5g25tfcc5jt9oniob", "amcx442becc3lp9ffm4vav", "dknrw7xj2fozz383i8hlwk", "9ajr7myfoyaowast33fmas",
			"1a7byxsvpyleppn373eel2", "hgffb6jusiy2yb3121wxik", "oogf9s7g8oqg9tg4fxc2yr"
	};

	private static final String lastUpdateFilePath = "/Data/last-updated.txt";

	// to prevent race conditions when making requests via the same APIReq object in different threads,
	// we create a new APIReq object for each sequential task
	private static APIReq getApiLeeway() {
		return new APIReq("https://api.leeway.tech/api/v1/public/", apiToksLeeway, AuthPolicy.QUERY, "apitoken",
				APIReq.rateLimitErrHandler(res -> {
				if (res.statusCode() == 429) return true;
			return res.body().startsWith("Your limit of");
		}));
	}

	// @Scalability If more than one component would need to react to updated data,
	// a single boolean flag would not be sufficient of course. Since we know,
	// however, that only the StateManager reacts to this information, having a
	// single boolean flag is completely sufficient
	public static final AtomicBoolean updatedData        = new AtomicBoolean(false);
	private static final ThreadPoolExecutor threadPool   = new ThreadPoolExecutor(16, 64, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	// All three arrays are assumed to be parallel
	private static Sonifiable[] sonifiables;
	private static String[] lowercaseSymbols;
	private static String[] lowercaseNames;

	private static List<Price> testPrices() {
		try {
			String fname  = "./src/main/resources/TestData/TestPrices.json";
			String json   = Files.readString(Path.of(fname));
			Parser parser = new Parser();
			return parser.parse(fname, json).applyList(x -> {
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
		} catch (Exception e) {
			return List.of();
		}
	}

	// Note: You shouldn't call this function twice
	public static void init() throws AppError {
		try {
			// Read cached sonifiables
			loadCached();

			if (UPDATE_SONIFIABLES) {
				Instant lastUpdate = getLastUpdateDay();
				if (lastUpdate == null || lastUpdate.plus(UPDATE_PERIOD_IN_DAYS, ChronoUnit.DAYS).isBefore(Instant.now()))
					updateSonifiablesList();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError(e.getMessage());
		}
	}

	public static Sonifiable[] findByPrefix(int length, String prefix, FilterFlag... filters) {
		if (prefix == "") {
			Sonifiable[] out = new Sonifiable[length];
			System.arraycopy(sonifiables, 0, out, 0, length);
			return out;
		}

		prefix = prefix.toLowerCase();
		List<Sonifiable> bestFinds  = new ArrayList<>(length);
		List<Sonifiable> otherFinds = new ArrayList<>(length);
		int flag = FilterFlag.getFilterVal(filters);

		for (int i = 0; i < lowercaseSymbols.length; i++) {
			List<Sonifiable> l = null;
			if (lowercaseSymbols[i].equals(prefix))
				l = bestFinds;
			else if (lowercaseSymbols[i].startsWith(prefix))
				l = otherFinds;
			if (l != null && (flag & sonifiables[i].type.getVal()) > 0)
				l.add(sonifiables[i]);
		}
		for (int i = 0; i < lowercaseNames.length; i++) {
			List<Sonifiable> l = null;
			if (lowercaseSymbols[i].equals(prefix))
				l = bestFinds;
			else if (lowercaseSymbols[i].contains(prefix))
				l = otherFinds;
			if (l != null && (flag & sonifiables[i].type.getVal()) > 0)
				l.add(sonifiables[i]);
		}

		if (bestFinds.size() >= length) {
			bestFinds = bestFinds.subList(0, length);
		} else {
			bestFinds.addAll(otherFinds.subList(0, Math.min(otherFinds.size(), length - bestFinds.size())));
		}
		Sonifiable[] out = new Sonifiable[bestFinds.size()];
		return bestFinds.toArray(out);
	}

	public static String getSonifiableName(SonifiableID id) {
		for (Sonifiable x : sonifiables) {
			if (x.getId().equals(id))
				return x.getCompositeName();
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
				String[] intervalQueries = {"interval", interval.toString()};
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

	public static Instant getLastUpdateDay() {
		try {
			String str = Files.readString(Path.of("./src/main/resources/"+ lastUpdateFilePath));
			return Instant.ofEpochMilli(Long.parseLong(str));
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void updateSonifiablesList() {
		threadPool.submit(() -> {
			try {
				List<String> exchanges = DEFAULT_EXCHANGES;
				if (GET_EXCHANGES_DYNAMICALLY) {
					APIReq apiLeeway = getApiLeeway();
					exchanges = apiLeeway.getJSONList("general/exchanges", json -> json.asMap().get("Code").asStr());
				}

				FutureList<List<Sonifiable>> fl = new FutureList<>(exchanges.size());
				for (String exchange : exchanges) {
					fl.add(threadPool.submit(() -> {
						try {
							return getApiLeeway().getJSONList("general/symbols/" + exchange, json -> {
								HashMap<String, JsonPrimitive<?>> m = json.asMap();
								JsonPrimitive<?> typeJson = m.get("Type");
								FilterFlag type = FilterFlag.fromLeewayString(typeJson.isNull() ? "" : typeJson.asStr());
								if (type == FilterFlag.NONE) return null;
								return new Sonifiable(m.get("Name").asStr(), new SonifiableID(m.get("Code").asStr(), exchange), type);
							}, true);
						} catch (Exception e) {
							return null;
						}
					}));
				}

				UnorderedList<Sonifiable> newSonifiables  = new UnorderedList<>(4096);
				List<String> newSymbols = new UnorderedList<>(4096);
				List<String> newNames = new UnorderedList<>(4096);
				Object[] allSonifiables = fl.getAll();
				System.out.println("FutureList is done");
				for (Object sonifiables : allSonifiables) {
					for (Sonifiable s : (List<Sonifiable>) sonifiables) {
						newSonifiables.add(s);
						newSymbols.add(s.getId().symbol.toLowerCase());
						newNames.add(s.getName().toLowerCase());
					}
				}
				System.out.println("newSonifiable lists created");

				// Remove duplicates
				// @Performance O(n^2) for a very big n :eyes:
				for (int i = 0; i < newSymbols.size(); i++) {
					for (int j = i+1; j < newSymbols.size(); j++) {
						if (newSymbols.get(i).equals(newSymbols.get(j))) {
							newSymbols.remove(j);
							newNames.remove(j);
							newSonifiables.remove(j);
							j--;
						}
					}
				}
				System.out.println("Duplicates removed");


				// The intermediate arrays are used to prevent the user trying to search for sonifiables, while they are being updated
				// this way, the actual updating is a simple pointer-swap which is instantaneous
				System.out.println("new Sonifiables amount: " + newSonifiables.size());
				System.out.println("new Symbols amount: " + newSymbols.size());
				System.out.println("new Names amount: " + newNames.size());
				assert newSonifiables.size() == newSymbols.size() && newSonifiables.size() == newNames.size();
				int len = newSonifiables.size();
				Sonifiable[] newSon = new Sonifiable[len];
				String[]     newSym = new String[len];
				String[]     newNam = new String[len];
				newSon = newSonifiables.toArray(newSon);
				newSym = newSymbols    .toArray(newSym);
				newNam = newNames      .toArray(newNam);
				System.out.println("intermediate arrays created");


				sonifiables      = newSon;
				lowercaseSymbols = newSym;
				lowercaseNames   = newNam;
				System.out.println("updated");
				updatedData.set(true);
				cacheData();
				Files.write(Path.of("./src/main/resources/" + lastUpdateFilePath), Long.toString(Instant.now().toEpochMilli()).getBytes(), StandardOpenOption.CREATE);
			} catch (Throwable e) {
				// we intentionally ignore this error, as it's not effecting the user at this time
				e.printStackTrace();
			}
		});
	}

	public static void loadCached() throws AppError {
		try {
			Parser parser = new Parser();
			String stri = Files.readString(Path.of("./src/main/resources/Data/sonifiables.json"));
			JsonPrimitive<?> json = parser.parse(stri);
			List<Sonifiable> sl = json.applyList(x -> new Sonifiable(
						x.asMap().get("name").asStr(),
						new SonifiableID(
							x.asMap().get("id").asMap().get("symbol").asStr(),
							x.asMap().get("id").asMap().get("exchange").asStr()),
						FilterFlag.fromString(x.asMap().get("type").asStr())),
					true);
			sonifiables      = new Sonifiable[sl.size()];
			sonifiables      = sl.toArray(sonifiables);
			lowercaseSymbols = new String[sonifiables.length];
			lowercaseNames   = new String[sonifiables.length];
			for (int i = 0; i < sonifiables.length; i++) {
				lowercaseSymbols[i] = sonifiables[i].getId().symbol.toLowerCase();
				lowercaseNames  [i] = sonifiables[i].getName().toLowerCase();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError("Die gespeicherten Börsendaten konnten nicht gelesen werden. Stelle sicher, dass keine App-internen Dateien verschoben oder gelöscht wurden.");
		}
	}

	private static void cacheData() throws AppError {
		try {
			Path path = Path.of("./src/main/resources/Data/sonifiables.json");
			String data = ArrayFunctions.toStringArr(sonifiables,  s -> s.toJSON(), true);
			Files.write(path, data.getBytes(), StandardOpenOption.WRITE);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError("Die neuen Börsendaten konnten nicht gespeichert werden.");
		}
	}
}