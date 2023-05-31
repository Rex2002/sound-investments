package dataRepo;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import app.AppError;
import dataRepo.api.APIErr;
import dataRepo.api.APIReq;
import dataRepo.api.AuthPolicy;
import dataRepo.api.PaginationHandler;
import dataRepo.json.JsonPrimitive;
import dataRepo.json.Parser;
import util.ArrayFunctions;
import util.FutureList;
import util.UnorderedList;

public class DataRepo {
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

	// TODO: Add specific error handling for the different APIs
	private static APIReq apiTwelvedata = new APIReq("https://api.twelvedata.com/", apiToksTwelvedata, AuthPolicy.QUERY,
			"apikey");
	private static APIReq apiLeeway = new APIReq("https://api.leeway.tech/api/v1/public/", apiToksLeeway,
			AuthPolicy.QUERY,
			"apitoken");
	private static APIReq apiMarketstack = new APIReq("http://api.marketstack.com/v1/", apiToksMarketstack,
			AuthPolicy.QUERY, "access_key", new PaginationHandler(json -> {
				// @Cleanup Remove hardcoded limit
				return 100;
			}, json -> json.asMap().get("data")),
			(page) -> {
				// @Cleanup Remove hardcoded offset
				// The function signature might have to change too, bc we need the current limit
				// for calculating the offset and I don't know how to get the limit without
				// having access to the APIReq object
				String[] res = { "offset", Integer.toString(10 * page) };
				return res;
			}, null);

	// @Scalability If more than one component would need to react to updated data,
	// a single boolean flag would not be sufficient of course. Since we know,
	// however, that only the StateManager reacts to this information, having a
	// single boolean flag is completely sufficient
	public static AtomicBoolean updatedData = new AtomicBoolean(false);
	private static BlockingQueue<Runnable> tpQueue = new LinkedBlockingQueue<>();
	private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(16, 64, 60, TimeUnit.SECONDS,
			tpQueue);

	private static UnorderedList<Stock> stocks = new UnorderedList<>(128);
	private static UnorderedList<ETF> etfs = new UnorderedList<>(128);
	private static UnorderedList<Index> indices = new UnorderedList<>(128);

	// When updatedStocksTradingPeriods == stocks.size(), all stocks are updated
	// set by setTradingPeriods()
	private static AtomicInteger updatedStocksTradingPeriods = new AtomicInteger(stocks.size());
	private static AtomicInteger updatedETFsTradingPeriods = new AtomicInteger(etfs.size());
	private static AtomicInteger updatedIndicesTradingPeriods = new AtomicInteger(indices.size());
	private static int updatedTradingPeriodsCounter = 0; // Amount of sonifiables lists, whose trading periods have been
															// updated

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

	public static void updateSonifiablesList() throws AppError {
		try {
			// @Cleanup increase limit to the maximum (1000) for production
			apiMarketstack.setQueries("limit", "10");
			FutureList<UnorderedList<Stock>> fl = apiMarketstack.getPaginatedJSONList("tickers", threadPool, 8,
					(Function<Integer, UnorderedList<Stock>>) (size -> {
						UnorderedList<Stock> l = new UnorderedList<>(size);
						for (int i = 0; i < size; i++)
							l.add(i, null);
						return l;
					}),
					x -> x, (Function<JsonPrimitive<?>, Stock>) (x -> {
						try {
							return new Stock(x.asMap().get("name").asStr(),
									new SonifiableID(x.asMap().get("symbol").asStr(),
											x.asMap().get("stock_exchange").asMap().get("acronym").asStr()));
						} catch (Exception e) {
							return null;
						}
					}));
			Timer checkFLTimer = new Timer();
			checkFLTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					try {
						if (fl.isDone()) {
							stocks = fl.get();
							for (int i = 0; i < stocks.size(); i++) {
								if (stocks.get(i) == null)
									stocks.remove(i);
							}
							writeToJSON("stocks.json",
									ArrayFunctions.toStringArr(stocks.getArray(), x -> ((Sonifiable) x).toJSON(),
											true));

							setTradingPeriods(stocks, updatedStocksTradingPeriods);

							// TODO: Update ETFs and Stocks too

							Timer timer = new Timer();
							timer.scheduleAtFixedRate(new TimerTask() {
								public void run() {
									// TODO: Refactor to reduce code duplication
									boolean updatedTradingPeriods = false;
									// @Cleanup exchange 32 with stocks.size()
									if (updatedStocksTradingPeriods.compareAndSet(stocks.size(), 0)) {
										updatedTradingPeriods = true;
										updatedTradingPeriodsCounter++;
										stocks.applyRemoves();
									}
									// @Cleanup uncomment once we actually get data for etfs/indices
									// needs to be commented out for now, bc the code doesn't work when list.size()
									// == 0
									// if (updatedETFsTradingPeriods.compareAndSet(etfs.size(), 0)) {
									// updatedTradingPeriods = true;
									// updatedTradingPeriodsCounter++;
									// etfs.applyRemoves();
									// }
									// if (updatedIndicesTradingPeriods.compareAndSet(indices.size(), 0)) {
									// updatedTradingPeriods = true;
									// updatedTradingPeriodsCounter++;
									// indices.applyRemoves();
									// }

									System.out.println(
											"Timer check: ThreadPool = " + threadPool.toString());

									if (updatedTradingPeriods)
										updatedData.set(true);
									// @Cleanup exchange 1 with 3, once we get data for etfs/indices too
									if (updatedTradingPeriodsCounter >= 1) {
										updatedTradingPeriodsCounter = 0;
										System.out.println("Setting Trading Periods done");
										timer.cancel();
									}
								}
							}, 500, 200);

							checkFLTimer.cancel();
						}
					} catch (Throwable e) {
						e.printStackTrace();
						checkFLTimer.cancel();
					}
				}
			}, 1000, 200);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError(e.getMessage());
		}
	}

	// Note: Don't call this function twice
	public static void init() throws AppError {
		try {
			// Read cached stocks
			Parser parser = new Parser();
			String stocksRes = Files.readString(Path.of("./src/main/resources/stocks.json"));
			JsonPrimitive<?> json = parser.parse(stocksRes);
			stocks = json
					.applyList(x -> {
						Calendar earliest = null, latest = null;
						try {
							earliest = DateUtil.calFromDateStr(x.asMap().get("earliest").asStr());
							latest = DateUtil.calFromDateStr(x.asMap().get("latest").asStr());
						} catch (Exception e) {
						}
						return new Stock(x.asMap().get("name").asStr(),
								new SonifiableID(x.asMap().get("id").asMap().get("symbol").asStr(),
										x.asMap().get("id").asMap().get("exchange").asStr()),
								earliest,
								latest);
					}, new UnorderedList<>(json.asList().size()), true);

			// TODO: Read cached ETFs/Indices

			// Start updating stocks in background
			updateSonifiablesList();

			System.out.println("Init done");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppError(e.getMessage());
		}
	}

	private static <T> void writeToJSON(String filename, String data) throws AppError {
		try {
			PrintWriter out = new PrintWriter("./src/main/resources/" + filename);
			out.write(data);
			out.close();
		} catch (Exception e) {
			throw new AppError(e.getMessage());
		}
	}

	private static <T extends Sonifiable> void setTradingPeriods(UnorderedList<T> list,
			AtomicInteger updatedTradingPeriods) {
		updatedTradingPeriods.set(0);
		for (int i = 0; i < list.size(); i++) {
			T s = list.get(i);
			final int idx = i;
			getTradingPeriod(s, dates -> {
				if (dates == null) {
					list.removeLater(idx);
				} else {
					s.setEarliest(dates[0]);
					s.setLatest(dates[1]);
				}
				int x = updatedTradingPeriods.incrementAndGet();
				System.out.println("Set trading periods. idx = " + idx + ", updatedCount = " + x + ", tpQueue.size() = "
						+ tpQueue.size());
				return s;
			});
		}
	}

	// Returns list of two Calendar objects. The first element is the starting date,
	// the second the ending date. If an error happened, `null` is returned
	public static <T> Future<T> getTradingPeriod(Sonifiable s, Function<Calendar[], T> callback) {
		return threadPool.submit(() -> {
			Calendar[] res = { null, null };
			T out = null;
			try {
				HashMap<String, JsonPrimitive<?>> json = apiLeeway
						.getJSON("general/tradingperiod/" + s.getId().toString(), x -> x.asMap());
				res[0] = DateUtil.calFromDateStr(json.get("start").asStr());
				res[1] = DateUtil.calFromDateStr(json.get("end").asStr());
				out = callback.apply(res);
			} catch (APIErr e) {
				// Try again using Twelvedata's API instead
				try {
					apiTwelvedata.setQueries("symbol", s.getId().getSymbol(), "exchange",
							s.getId().getExchange(),
							"outputsize", "10", "interval", IntervalLength.DAY.toString(API.TWELVEDATA));

					List<JsonPrimitive<?>> vals = apiTwelvedata.getJSON("/time_series",
							x -> x.asMap().get("values").asList(), "order", "ASC", "start_date", "1990-01-01");
					res[0] = DateUtil.calFromDateStr(vals.get(0).asMap().get("datetime").asStr());

					vals = apiTwelvedata.getJSON("/time_series", x -> x.asMap().get("values").asList(), "order", "DESC",
							"end_date",
							DateUtil.formatDate(Calendar.getInstance()));
					res[1] = DateUtil.calFromDateStr(vals.get(0).asMap().get("datetime").asStr());

					apiTwelvedata.resetQueries();
					out = callback.apply(res);
				} catch (Exception e2) {
					out = callback.apply(null);
				}
			} catch (Exception e) {
				out = callback.apply(null);
			}
			return out;
		});
		// Alternative implementation:
		/*
		 * apiTwelvedata.setQueries("symbol", s.getId().getSymbol(), "exchange",
		 * s.getId().getExchange(),
		 * "outputsize", "10", "interval", IntervalLength.DAY.toString(API.TWELVEDATA));
		 *
		 * List<JsonPrimitive<?>> vals = apiTwelvedata.getJSON(x ->
		 * x.asMap().get("values").asList(),
		 * "/time_series", "order", "ASC", "start_date", "1990-01-01");
		 * if (vals.isEmpty())
		 * throw new Exception(); // throw exception to fall into catch-branch
		 * s.setEarliest(DateUtil.calFromDateStr(vals.get(0).asMap().get("datetime").
		 * asStr()));
		 *
		 * vals = apiTwelvedata.getJSON(x -> x.asMap().get("values").asList(),
		 * "/time_series", "order", "DESC", "end_date",
		 * DateUtil.formatDate(Calendar.getInstance()));
		 * if (vals.isEmpty())
		 * throw new Exception(); // throw exception to fall into catch-branch
		 * s.setEarliest(DateUtil.calFromDateStr(vals.get(0).asMap().get("datetime").
		 * asStr()));
		 */
	}

	public static List<Sonifiable> findByPrefix(String prefix, FilterFlag... filters) {
		int flag = FilterFlag.getFilterVal(filters);
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

	public static List<Price> getPrices(Sonifiable s, Calendar start, Calendar end, IntervalLength interval) {
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
