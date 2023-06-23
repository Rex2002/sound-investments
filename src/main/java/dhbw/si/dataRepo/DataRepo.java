package dhbw.si.dataRepo;

import dhbw.si.app.AppError;
import dhbw.si.dataRepo.api.APIReq;
import dhbw.si.dataRepo.api.AuthPolicy;
import dhbw.si.dataRepo.json.JsonPrimitive;
import dhbw.si.dataRepo.json.Parser;
import dhbw.si.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author V. Richter
 * @reviewer L. Lehmann
 *
 * The DataRepo offers an interface for retrieving any data from the API connection.
 * The main functionalities are provided via the {@code getPrices} and {@code findByPrefix} functions.
 * Before those can be used, however, one should call the {@code init} function. This function should not be called more than once.
 *
 * Much of the implementation of this class assumes that only one component is interacting with the DataRepo without chance of race conditions. This assumption helps us reduce unnecessary overhead.
 * The first implementation detail based on this assumption is the fact, that this class is static throughout, as it is not assumed to be needed twice.
 *
 * Because waiting for API-calls can take quite long, any function making API-requests will return a {@code Future}.
 * Furthermore, the DataRepo might update its list of sonifiables (i.e. stocks, etfs and indices) at any moment. When interacting with the DataRepo, one should thus periodically check whether this data has been updated (indicated via the atomic boolean {@code updatedData}).
 * As it is implemented at the moment, updates wll only happen on startup of the app and only if the last update was 5 days ago.
 *
 * @ImplNote
 * There are several variables that may be set to effect the functioning of the DataRepo. They are mainly useful for enabling different testing cases
 * As the user is not meant to tweak these values, they are compile-time values.
 */
public class DataRepo {
	private static final boolean GET_EXCHANGES_DYNAMICALLY = false;
	private static final boolean UPDATE_SONIFIABLES        = true;
	private static final int UPDATE_PERIOD_IN_DAYS         = 5;
	private static final List<String> DEFAULT_EXCHANGES    = List.of("NYSE", "NASDAQ", "XETRA", "F", "BE", "MU");

	private static final String[] apiToksLeeway = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm", "r7splaijduabfpcu2z2l14",
			"o5npdx6elm2pcpp395uaun", "2ja5gszii8g63hzjd41x78", "ftd5l4hscm9biueu5ptptr", "42dreongzzo5yqkyg2r3cr",
			"axt2o7xkx1cxd9nsinqfu6", "rumn8riwt3oo18z1cluvz1", "9rjaye34gvir42j113qf7r", "6gqkwy7si5lv1tgicp28le", "6yylrshzzhlsdhf26ysmq9",
			"xaqiip44tivsl5a963zvft", "gyvss5g25tfcc5jt9oniob", "amcx442becc3lp9ffm4vav", "dknrw7xj2fozz383i8hlwk", "9ajr7myfoyaowast33fmas",
			"1a7byxsvpyleppn373eel2", "hgffb6jusiy2yb3121wxik", "oogf9s7g8oqg9tg4fxc2yr", "nj3mvk564sayhot99mxkff", "7fmgnkm5pdq6h6dv1wjqki",
			"ejiuuknoyhos5814t8izqd", "npcef8lh836zl3hbwtfli8", "4ym7tja8p2dwlp2bih2fje", "wm56996eeq62c8ocz71i6o", "z827zmvlpu8xizn2f8kxx4",
			"68ybhlije2mbpludm7th58", "qjzj3z3c2r9xznkvhaitee", "qjzj3z3c2r9xznkvhaitee", "ivgccl4d9j6wcsd4b6ir9e", "9uvojumn4cfucw678vysxv",
			"z24dmsuvivce2hjf7e38i5", "msdo37wboxs5oeunq8gji2"
	};

	// File-path is relative from the resources directory
	private static final String lastUpdateFilePath = "/Data/last-updated.txt";

	// to prevent race conditions when making requests via the same APIReq object in different threads, we create
	// a new APIReq object in each thread. This function simplifies creating new objects with the same configuration
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
	public static final AtomicBoolean updatedData      = new AtomicBoolean(false);
	private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(16, 64, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	/* All three arrays below are assumed to be parallel. This assumption is enforced in the {@code updateSonifiablesList} function */
	private static Sonifiable[] sonifiables;
	private static String[] lowercaseSymbols;
	private static String[] lowercaseNames;

	// Note: You shouldn't call this function twice
	public static void init() throws AppError {
		try {
			// Read cached sonifiables from disk
			loadCached();

			// Potentially update list of sonifiables to be synced with the API
			if (UPDATE_SONIFIABLES) {
				Instant lastUpdate = getLastUpdateDay();
				if (lastUpdate == null || lastUpdate.plus(UPDATE_PERIOD_IN_DAYS, ChronoUnit.DAYS).isBefore(Instant.now()))
					updateSonifiablesList();
			}
		} catch (Exception e) {
			if (Dev.DEBUG) e.printStackTrace();
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
			if (lowercaseNames[i].equals(prefix))
				l = bestFinds;
			else if (lowercaseNames[i].contains(prefix))
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
					if (startEnd[1].before(startEnd[0])) break;
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
				if (Dev.DEBUG) e.printStackTrace();
				if (Dev.DEBUG) System.out.println("Fehler beim Einholen der Preisdaten von " + s + " vom " + DateUtil.formatDateGerman(start) + " bis zum " + DateUtil.formatDateGerman(end));
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
				if (Dev.DEBUG) System.out.println("FutureList is done");
				for (Object sonifiables : allSonifiables) {
					for (Sonifiable s : (List<Sonifiable>) sonifiables) {
						newSonifiables.add(s);
						newSymbols.add(s.getId().symbol.toLowerCase());
						newNames.add(s.getName().toLowerCase());
					}
				}
				if (Dev.DEBUG) System.out.println("newSonifiable lists created");

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
				if (Dev.DEBUG) System.out.println("Duplicates removed");


				// The intermediate arrays are used to prevent the user trying to search for sonifiables, while they are being updated
				// this way, the actual updating is a simple pointer-swap which is instantaneous
				if (Dev.DEBUG) System.out.println("new Sonifiables amount: " + newSonifiables.size());
				if (Dev.DEBUG) System.out.println("new Symbols amount: " + newSymbols.size());
				if (Dev.DEBUG) System.out.println("new Names amount: " + newNames.size());
				assert newSonifiables.size() == newSymbols.size() && newSonifiables.size() == newNames.size();
				int len = newSonifiables.size();
				Sonifiable[] newSon = new Sonifiable[len];
				String[]     newSym = new String[len];
				String[]     newNam = new String[len];
				newSon = newSonifiables.toArray(newSon);
				newSym = newSymbols    .toArray(newSym);
				newNam = newNames      .toArray(newNam);
				if (Dev.DEBUG) System.out.println("intermediate arrays created");


				sonifiables      = newSon;
				lowercaseSymbols = newSym;
				lowercaseNames   = newNam;
				if (Dev.DEBUG) System.out.println("updated");
				updatedData.set(true);
				cacheData();
				Files.write(Path.of("./src/main/resources/" + lastUpdateFilePath), Long.toString(Instant.now().toEpochMilli()).getBytes(), StandardOpenOption.CREATE);
			} catch (Throwable e) {
				// we intentionally ignore this error, as it's not effecting the user at this time
				if (Dev.DEBUG) e.printStackTrace();
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
			if (Dev.DEBUG) e.printStackTrace();
			throw new AppError("Die gespeicherten Börsendaten konnten nicht gelesen werden. Stelle sicher, dass keine App-internen Dateien verschoben oder gelöscht wurden.");
		}
	}

	private static void cacheData() throws AppError {
		try {
			Path path = Path.of("./src/main/resources/Data/sonifiables.json");
			String data = ArrayFunctions.toStringArr(sonifiables,  s -> s.toJSON(), true);
			Files.write(path, data.getBytes(), StandardOpenOption.WRITE);
		} catch (Exception e) {
			if (Dev.DEBUG) e.printStackTrace();
			throw new AppError("Die neuen Börsendaten konnten nicht gespeichert werden.");
		}
	}
}