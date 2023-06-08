package dataRepo.api;

import dataRepo.json.JsonPrimitive;
import dataRepo.json.Parser;
import util.ArrayFunctions;
import util.FutureList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

public class APIReq {
	// Conditions based on the debug-variable will be evaluated at compile time
	// See: https://stackoverflow.com/a/1813873/13764271
	private static final boolean debug = true;
	private static int reqCounter = 0;
	private static Random rand = new Random();

	private static final List<StrTuple> defaultHeaders = new ArrayList<>(
			List.of(new StrTuple("Content-Type", "application/json")));
	private static final HttpClient client = HttpClient.newHttpClient();

	public static ErrHandler rateLimitErrHandler(Predicate<HttpResponse<String>> isRateLimited) {
		// The Rate-Limit is handled by making switching the API-key and making a new request
		// Switching the API-key is done implicitly via the getApiTok() method
		return new ErrHandler() {
			@Override
			public HttpResponse<String> handle(HttpResponse<String> res, APIReq api) throws APIErr {
				int apiToksAmount = api.getAllApiToks().length;
				int i = 0;
				while (isRateLimited.test(res)) {
					try {
						res = api.makeReq(null, false);
					} catch (Exception e) {
					}
					i++;
					if (i == apiToksAmount) throw new APIErr(res.statusCode(), res.body());
				}
				return res;
			};
		};
	}

	public static ErrHandler defaultErrHandler() {
		return new ErrHandler() {
			@Override
			public HttpResponse<String> handle(HttpResponse<String> res, APIReq api) throws APIErr {
				throw new APIErr(res.statusCode(), res.body());
			}
		};
	}

	private final String base;
	private List<StrTuple> headers;
	private List<StrTuple> queries;
	private final AuthPolicy authPolicy;
	private final String apiTokQueryKey;
	private final String[] apiToks;
	private int curTokIdx;
	private final PaginationHandler paginationHandler;
	private final Function<Integer, String[]> getQueryForPage;
	private final ErrHandler errorHandler;
	private String lastReqEndpoint;
	private StrTuple[] lastReqQueries;

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey,
			PaginationHandler handler, Function<Integer, String[]> getQueryForPage, ErrHandler errorHandler) {
		this.base = base.endsWith("/") ? base : base + "/";
		this.authPolicy = authPolicy;
		this.apiTokQueryKey = apiTokQueryKey;
		this.apiToks = apiToks;
		this.curTokIdx = rand.nextInt(apiToks.length);
		this.paginationHandler = handler;
		this.getQueryForPage = getQueryForPage;
		this.errorHandler = errorHandler;
		reset();
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey,
			PaginationHandler handler, Function<Integer, String[]> getQueryForPage) {
		this(base, apiToks, authPolicy, apiTokQueryKey, handler, getQueryForPage, defaultErrHandler());
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey) {
		this(base, apiToks, authPolicy, apiTokQueryKey, null, null, defaultErrHandler());
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy) {
		this(base, apiToks, authPolicy, null, null, null, defaultErrHandler());
	}

	public void reset() {
		resetHeaders();
		resetQueries();
	}

	public void resetHeaders() {
		this.headers = defaultHeaders;
	}

	public void resetQueries() {
		this.queries = new ArrayList<>();
	}

	// Return `this` to allow chaining
	public APIReq setHeader(String key, String val) {
		for (StrTuple h : this.headers) {
			if (h.getFirst() == key) {
				h.setSecond(val);
				return this;
			}
		}
		this.headers.add(new StrTuple(key, val));
		return this;
	}

	// Convenience method for setting several headers at once.
	// Return `this` to allow chaining
	public APIReq setHeaders(String... headers) {
		assert headers.length % 2 == 0
				: "setHeaders expects a flattened list of key-value pairs as input. The amount of arguments must therefore be even";

		for (int i = 0; i < headers.length; i += 2) {
			setHeader(headers[i], headers[i + 1]);
		}
		return this;
	}

	public String getHeader(String key) {
		for (StrTuple h : headers) {
			if (h.getFirst() == key)
				return h.getSecond();
		}
		return null;
	}

	// Return `this` to allow chaining
	public APIReq setQuery(String key, String val) {
		for (StrTuple h : queries) {
			if (h.getFirst() == key) {
				h.setSecond(val);
				return this;
			}
		}
		queries.add(new StrTuple(key, val));
		return this;
	}

	public String getQuery(String key) {
		for (StrTuple q : queries) {
			if (q.getFirst() == key)
				return q.getSecond();
		}
		return null;
	}

	// Convenience method for setting several headers at once.
	// Return `this` to allow chaining
	public APIReq setQueries(String... queries) {
		assert queries.length % 2 == 0
				: "setQueries expects a flattened list of key-value pairs as input. The amount of arguments must therefore be even";

		for (int i = 0; i < queries.length; i += 2) {
			setQuery(queries[i], queries[i + 1]);
		}
		return this;
	}

	public String getApiTok() {
		String res = apiToks[curTokIdx];
		curTokIdx = (curTokIdx + 1) % apiToks.length;
		return res;
	}

	public String[] getAllApiToks() {
		return apiToks;
	}

	public HttpRequest prepReq(String endPoint, StrTuple... queries)
			throws URISyntaxException, IOException, InterruptedException {
		String[] queryStrs = new String[2 * queries.length];
		for (int i = 0; i < queries.length; i++) {
			queryStrs[2*i+0] = queries[i].getFirst();
			queryStrs[2*i+1] = queries[i].getSecond();
		}
		return prepReq(endPoint, queryStrs);
	}

	public HttpRequest prepReq(String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException {
		assert queries.length % 2 == 0
				: "req expects a flattened list of key-value pairs as input for the queries. The amount of query-arguments must therefore be even";

		switch (authPolicy) {
			case BEARER:
				setHeader("Authorization", "Bearer " + getApiTok());
				break;

			case QUERY:
				setQuery(apiTokQueryKey, getApiTok());

			default:
				assert false : "unreachable";
				break;
		}

		StringBuilder sb = new StringBuilder(base);
		sb.append(endPoint);
		int internalQueriesAmount = this.queries.size();
		int queriesNoApiTokAmount = internalQueriesAmount - ((authPolicy == AuthPolicy.QUERY) ? 1 : 0);
		lastReqEndpoint = endPoint;
		lastReqQueries = new StrTuple[queriesNoApiTokAmount + queries.length / 2];

		if (queries.length > 0 || internalQueriesAmount > 0) {
			if (endPoint.endsWith("/")) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append('?');

			int k = 0;
			for (int i = 0; i < internalQueriesAmount; i++, k++) {
				StrTuple q = this.queries.get(i);
				sb.append(q.getFirst());
				sb.append('=');
				sb.append(q.getSecond());
				sb.append('&');
				if (authPolicy == AuthPolicy.QUERY && q.getFirst().equals(apiTokQueryKey))
					k--;
				else
					lastReqQueries[k] = q;
			}

			for (int i = 0; i < queries.length; i += 2) {
				sb.append(queries[i + 0]);
				sb.append('=');
				sb.append(queries[i + 1]);
				sb.append('&');
				lastReqQueries[k++] = new StrTuple(queries[i + 0], queries[i + 1]);
			}

			// Last '&' is unnecessary and can be removed
			sb.deleteCharAt(sb.length() - 1);
		}

		String url = sb.toString().replace(" ", "%20");
		if (debug)
			System.out.println((reqCounter++) + ". URL: " + url);
		URI uri = new URI(url);

		HttpRequest.Builder rb = HttpRequest.newBuilder(uri);
		for (StrTuple header : headers) {
			rb.setHeader(header.getFirst(), header.getSecond());
		}
		return rb.build();
	}

	// To repeat the last request again, call this function with req == null
	public HttpResponse<String> makeReq(HttpRequest req, boolean handleErr)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (req == null) req = prepReq(lastReqEndpoint, lastReqQueries);
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		if (handleErr && (res.statusCode() >= 300 || res.statusCode() < 200)) {
			return errorHandler.handle(res, this);
		} else {
			return res;
		}
	}

	public HttpResponse<String> makeReq(String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		HttpRequest req = prepReq(endPoint, queries);
		return makeReq(req, true);
	}

	public <T> T getJSON(String endPoint, Function<JsonPrimitive<?>, T> func, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);
		HttpResponse<String> res = makeReq(endPoint, queries);
		String body = res.body();

		return new Parser().parse(body, func);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, T> forEach, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		return getJSONList(endPoint, json -> json, forEach, false, queries);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, T> forEach, boolean rmNulls, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		return getJSONList(endPoint, json -> json, forEach, rmNulls, queries);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList, Function<JsonPrimitive<?>, T> forEach, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		return getJSONList(endPoint, jsonToList, forEach, false, queries);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList, Function<JsonPrimitive<?>, T> forEach, boolean rmNulls, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);
		HttpResponse<String> res = makeReq(endPoint, queries);
		String body = res.body();

		return new Parser().parse(body, jsonToList).applyList(forEach, rmNulls);
	}

	@SuppressWarnings("unchecked")
	public <T, L extends List<T>> FutureList<L> getPaginatedJSONList(String endPoint, ExecutorService execService,
			int parallelAmount, Function<Integer, L> makeOutList,
			Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList, Function<JsonPrimitive<?>, T> forEach,
			String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);

		// Initial request
		String[] allQueries = ArrayFunctions.add(queries, getQueryForPage.apply(0));
		JsonPrimitive<?> res = new Parser().parse(makeReq(endPoint, allQueries).body());
		int total = paginationHandler.getTotal.apply(res);
		L totalOutList = makeOutList.apply(total);
		JsonPrimitive<?> dataOfRes = jsonToList.apply(paginationHandler.getJsonData.apply(res));
		int firstListSize = dataOfRes.asList().size();
		dataOfRes.applyList(forEach, totalOutList, false);

		// Further requests (in parallel)
		parallelAmount = Math.min(total / firstListSize, parallelAmount);
		Future<?>[] futures = new Future<?>[parallelAmount];
		for (int i = 0; i < parallelAmount; i++) {
			futures[i] = execService
					.submit(new PaginatedReq<T, L>(this, endPoint, queries, i, parallelAmount, firstListSize,
							total, totalOutList, jsonToList, forEach));
		}

		return new FutureList<>((Future<L>[]) futures);
	}

	private class PaginatedReq<T, L extends List<T>> implements Callable<L> {
		private final APIReq api;
		private final String endpoint;
		private final String[] queries;
		private final int threadIndex;
		private final int threadAmount;
		private int singleResSize;
		private final int total;
		private final L list;
		private final Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList;
		private final Function<JsonPrimitive<?>, T> forEach;
		private final Parser parser;

		public PaginatedReq(APIReq api, String endpoint, String[] queries, int threadIndex, int threadAmount,
				int singleResSize, int total, L list, Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList,
				Function<JsonPrimitive<?>, T> forEach) {
			this.api = api;
			this.endpoint = endpoint;
			this.queries = queries;
			this.threadIndex = threadIndex;
			this.threadAmount = threadAmount;
			this.singleResSize = singleResSize;
			this.total = total;
			this.list = list;
			this.jsonToList = jsonToList;
			this.forEach = forEach;
			this.parser = new Parser();
		}

		public L call() throws Exception {
			// The first request with offset == 0 was already executed before, so we set
			// reqAmounts = 1 in that case
			int reqAmounts = threadIndex == 0 ? 1 : 0;
			int pageIdx = (reqAmounts * threadAmount + threadIndex);
			int listStartIdx = pageIdx * singleResSize;
			int received;
			while (listStartIdx < total) {
				String[] allQueries = ArrayFunctions.add(queries, getQueryForPage.apply(pageIdx));
				received = 0;
				try {
					String res = api.makeReq(endpoint, allQueries).body();
					JsonPrimitive<?> data = jsonToList
							.apply(api.paginationHandler.getJsonData.apply(parser.parse(res)));
					List<T> l = data.applyList(forEach, new ArrayList<>(singleResSize), false);
					received = l.size();
					for (int i = 0; i < received; i++) {
						list.set(listStartIdx + i, l.get(i));
					}
				} catch (Exception e) {
					// Request failed -> Log Error
					// @Decide Should user be informed about this failure?
					e.printStackTrace();
				} finally {
					// In case we didn't receive as many elements as we expected,
					// we set the remaining elements in the list-range to null,
					// to prevent undefined behaviour because of uninitialized values
					int len = Math.min(listStartIdx + singleResSize, total);
					for (int i = listStartIdx + received; i < len; i++) {
						list.set(i, null);
					}
				}
				reqAmounts++;
				pageIdx = (reqAmounts * threadAmount + threadIndex);
				listStartIdx = pageIdx * singleResSize;
			}
			return list;
		}
	}
}