package apiTest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import json.JsonPrimitive;
import json.Parser;

public class APIReq {
	// Conditions based on the debug-variable will be evaluated at compile time
	// See: https://stackoverflow.com/a/1813873/13764271
	private static final boolean debug = false;

	private static final List<StrTuple> defaultHeaders = new ArrayList<>(
			List.of(new StrTuple("Content-Type", "application/json")));
	private static final HttpClient client = HttpClient.newHttpClient();

	private final String base;
	private List<StrTuple> headers;
	private List<StrTuple> queries;
	private final AuthPolicy authPolicy;
	private final String apiTokQueryKey;
	private final String[] apiToks;
	private int curTokIdx;

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy, String apiTokQueryKey) {
		this.base = base.endsWith("/") ? base : base + "/";
		this.authPolicy = authPolicy;
		this.apiTokQueryKey = apiTokQueryKey;
		this.apiToks = apiToks;
		this.curTokIdx = 0;
		reset();
	}

	public APIReq(String base, String[] apiToks, AuthPolicy authPolicy) {
		this.base = base.endsWith("/") ? base : base + "/";
		this.authPolicy = authPolicy;
		this.apiTokQueryKey = null;
		this.apiToks = apiToks;
		this.curTokIdx = 0;
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
	public APIReq setHeaders(String... headers) {
		assert headers.length % 2 == 0
				: "setHeaders expects a flattened list of key-value pairs as input. The amount of arguments must therefore be even";

		for (int i = 0; i < headers.length; i += 2) {
			setHeader(headers[i], headers[i + 1]);
		}
		return this;
	}

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
		sb.append(endPoint.startsWith("/") ? endPoint.substring(1) : endPoint);

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
				sb.append(queries[0]);
				sb.append('=');
				sb.append(queries[1]);
				sb.append('&');
			}

			// Last '&' is unnecessary & can be removed
			sb.deleteCharAt(sb.length() - 1);
		}
		URI uri = new URI(sb.toString());

		HttpRequest.Builder rb = HttpRequest.newBuilder(uri);
		for (StrTuple header : headers) {
			rb.setHeader(header.getFirst(), header.getSecond());
		}
		return rb.build();
	}

	public T getJSON(Function<JsonPrimitive<?>, T> func, String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException {
		HttpRequest req = prepReq(endPoint, queries);
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		String body = res.body();

		if (debug) {
			String filename = "resources/test.json";
			PrintWriter out = new PrintWriter(filename);
			out.print(body);
			out.close();
			return new Parser().parse(filename, body, func);
		} else {
			return new Parser().parse(body, func);
		}
	}

	public List<T> getJSONList(Function<JsonPrimitive<?>, T> func, String endPoint, String... queries)
			throws URISyntaxException, IOException, InterruptedException {
		HttpRequest req = prepReq(endPoint, queries);
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		String body = res.body();

		if (debug) {
			String filename = "resources/test.json";
			PrintWriter out = new PrintWriter(filename);
			out.print(body);
			out.close();
			return new Parser().parseList(filename, body, func);
		} else {
			return new Parser().parseList(body, func);
		}
	}
}
