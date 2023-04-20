package apiTest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.List;
import json.*;

public class Test {
	public static void main(String[] args) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
				"https://api.leeway.tech/api/v1/public/historicalquotes/SAP.XETRA?apitoken=pgz64a5qiuvw4qhkoullnx&from=2023-04-01"))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String body = response.body();

		PrintWriter out = new PrintWriter("resources/test.json");
		out.print(body);
		out.close();

		Parser parser = new Parser();
		List<T> l = parser.parseList("resources/test.json", body, x -> {
			HashMap<String, JsonPrimitive<?>> obj = x.asMap();
			Double start = obj.get("open").asDouble();
			Double end = obj.get("close").asDouble();
			return new T(start, end);
		});

		for (Integer i = 0; i < l.size(); i++) {
			System.out.println(i.toString() + ": " + l.get(i).toString());
		}
	}
}
