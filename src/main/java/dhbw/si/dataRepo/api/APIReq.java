package dhbw.si.dataRepo.api;

import dhbw.si.dataRepo.json.JsonPrimitive;
import dhbw.si.dataRepo.json.Parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class APIReq {
	private static final Random rand = new Random();

	private static final List<StrTuple> defaultHeaders = new ArrayList<>(
			List.of(new StrTuple("Content-Type", "application/json")));
	private static final HttpClient client = HttpClient.newHttpClient();

	public static ErrHandler rateLimitErrHandler(Predicate<HttpResponse<String>> isRateLimited) {
		// The Rate-Limit is handled by making switching the API-key and making a new request
		// Switching the API-key is done implicitly via the getApiTok() method
		return (res, api) -> {
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
	}

	private final String base;
	private List<StrTuple> headers;
	private List<StrTuple> queries;
	private final AuthPolicy authPolicy;
	private final String apiTokQueryKey;
	private final String[] apiToks;
	private int curTokIdx;
	private final ErrHandler errorHandler;
	private String lastReqEndpoint;
	private StrTuple[] lastReqQueries;

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey,
				  ErrHandler errorHandler) {
		this.base = base.endsWith("/") ? base : base + "/";
		this.authPolicy = authPolicy;
		this.apiTokQueryKey = apiTokQueryKey;
		this.apiToks = apiToks;
		this.curTokIdx = rand.nextInt(apiToks.length);
		this.errorHandler = errorHandler;
		reset();
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
			if (h.getFirst().equals(key)) {
				h.setSecond(val);
				return this;
			}
		}
		this.headers.add(new StrTuple(key, val));
		return this;
	}

	// Return `this` to allow chaining
	public APIReq setQuery(String key, String val) {
		for (StrTuple h : queries) {
			if (h.getFirst().equals(key)) {
				h.setSecond(val);
				return this;
			}
		}
		queries.add(new StrTuple(key, val));
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
			throws URISyntaxException {
		String[] queryStrs = new String[2 * queries.length];
		for (int i = 0; i < queries.length; i++) {
			queryStrs[2 * i] = queries[i].getFirst();
			queryStrs[2 * i + 1] = queries[i].getSecond();
		}
		return prepReq(endPoint, queryStrs);
	}

	public HttpRequest prepReq(String endPoint, String... queries)
			throws URISyntaxException {
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
				sb.append(queries[i]);
				sb.append('=');
				sb.append(queries[i + 1]);
				sb.append('&');
				lastReqQueries[k++] = new StrTuple(queries[i], queries[i + 1]);
			}

			// Last '&' is unnecessary and can be removed
			sb.deleteCharAt(sb.length() - 1);
		}

		String url = sb.toString().replace(" ", "%20");
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

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, T> forEach, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		return getJSONList(endPoint, json -> json, forEach, false, queries);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, T> forEach, boolean rmNulls, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		return getJSONList(endPoint, json -> json, forEach, rmNulls, queries);
	}

	public <T> List<T> getJSONList(String endPoint, Function<JsonPrimitive<?>, JsonPrimitive<?>> jsonToList, Function<JsonPrimitive<?>, T> forEach, boolean rmNulls, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);
		HttpResponse<String> res = makeReq(endPoint, queries);
		String body = res.body();

		return new Parser().parse(body, jsonToList).applyList(forEach, rmNulls);
	}

}