package apiTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		String[] leewayAPIToks = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm" };
		APIReq leewayAPI = new APIReq("https://api.leeway.tech/api/v1/public/", leewayAPIToks, AuthPolicy.QUERY,
				"apitoken");

		List<Exchange> exchanges = leewayAPI.getJSONList(
				x -> new Exchange(x.asMap().get("Name").asStr(), x.asMap().get("Code").asStr()),
				"general/exchanges");

		List<Stock> stocks = new ArrayList<>();
		for (Exchange exchange : exchanges) {
			stocks.addAll(leewayAPI.getJSONList(
					x -> new Stock(x.asMap().get("name").asStr(), x.asMap().get("code").asStr(),
							x.asMap().get("exchange").asStr(), x.asMap().get("type").asStr()),
					"general/symbols/" + exchange.code));
		}

		System.out.println("Amount of available stocks: " + stocks.size());
	}
}
