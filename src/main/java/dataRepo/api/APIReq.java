package dataRepo.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import dataRepo.json.JsonPrimitive;
import dataRepo.json.Parser;

public class APIReq {
	// Conditions based on the debug-variable will be evaluated at compile time
	// See: https://stackoverflow.com/a/1813873/13764271
	private static final boolean debug = true;
	private static int reqCounter = 0;

	private static final List<StrTuple> defaultHeaders = new ArrayList<>(
			List.of(new StrTuple("Content-Type", "application/json")));
	private static final HttpClient client = HttpClient.newHttpClient();

	public static ErrHandler defaultErrHandler() {
		return new ErrHandler() {
			@Override
			public HttpResponse<String> handle(HttpResponse<String> res) throws APIErr {
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
	private Function<JsonPrimitive<?>, HandledPagination> paginationHandler;
	private Consumer<Integer> setQueryPage;
	private ErrHandler errorHandler;

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey,
			Function<JsonPrimitive<?>, HandledPagination> handler,
			Consumer<Integer> setQueryPage, ErrHandler errorHandler) {
		this.base = base.endsWith("/") ? base : base + "/";
		this.authPolicy = authPolicy;
		this.apiTokQueryKey = apiTokQueryKey;
		this.apiToks = apiToks;
		this.curTokIdx = 0;
		this.paginationHandler = handler;
		this.setQueryPage = setQueryPage;
		this.errorHandler = errorHandler;
		reset();
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey,
			Function<JsonPrimitive<?>, HandledPagination> handler,
			Consumer<Integer> setQueryPage) {
		this(base, apiToks, authPolicy, apiTokQueryKey, handler, setQueryPage, defaultErrHandler());
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey) {
		this(base, apiToks, authPolicy, apiTokQueryKey, null, null, defaultErrHandler());
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy) {
		this(base, apiToks, authPolicy, null, null, null, defaultErrHandler());
	}

	public void setPaginationHandler(Function<JsonPrimitive<?>, HandledPagination> handler,
			Consumer<Integer> setQueryPage) {
		this.paginationHandler = handler;
		this.setQueryPage = setQueryPage;
	}

	public void setErrorHandler(ErrHandler errorHandler) {
		this.errorHandler = errorHandler;
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

	// Return `this` to allow chaining
	public APIReq setQuery(String key, String val) {
		for (StrTuple h : this.queries) {
			if (h.getFirst() == key) {
				h.setSecond(val);
				return this;
			}
		}
		this.queries.add(new StrTuple(key, val));
		return this;
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
		curTokIdx++;
		if (curTokIdx == apiToks.length)
			curTokIdx = 0;
		return res;
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

		if (queries.length > 0 || this.queries.size() > 0) {
			if (endPoint.endsWith("/")) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append('?');

			for (int i = 0; i < this.queries.size(); i++) {
				StrTuple q = this.queries.get(i);
				sb.append(q.getFirst());
				sb.append('=');
				sb.append(q.getSecond());
				sb.append('&');
			}

			for (int i = 0; i < queries.length; i += 2) {
				sb.append(queries[i + 0]);
				sb.append('=');
				sb.append(queries[i + 1]);
				sb.append('&');
			}

			// Last '&' is unnecessary & can be removed
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

	public HttpResponse<String> makeReq(String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		HttpResponse<String> res = client.send(prepReq(endPoint, queries), HttpResponse.BodyHandlers.ofString());
		if (res.statusCode() >= 300 || res.statusCode() < 200) {
			errorHandler.handle(res);
		}
		return res;
	}

	public <T> T getJSON(Function<JsonPrimitive<?>, T> func, String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);
		HttpResponse<String> res = makeReq(endPoint, queries);
		String body = res.body();

		return new Parser().parse(body, func);
	}

	public <T> List<T> getJSONList(Function<JsonPrimitive<?>, T> func, String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);
		HttpResponse<String> res = makeReq(endPoint, queries);
		String body = res.body();

		return new Parser().parse(body).applyList(func);
	}

	public <T> List<T> getPaginatedList(Function<JsonPrimitive<?>, T> func, String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException, APIErr {
		if (endPoint.startsWith("/"))
			endPoint = endPoint.substring(1);

		Parser parser = new Parser();
		List<T> list = new ArrayList<>();
		Integer pageCounter = 0;
		HandledPagination page = null;
		do {
			this.setQueryPage.accept(pageCounter);
			HttpResponse<String> res = makeReq(endPoint, queries);
			String body = res.body();

			page = this.paginationHandler.apply(parser.parse(body));
			list.addAll(page.getRestJson().applyList(func));
			pageCounter++;
		} while (!page.isDone());

		return list;
	}
}
