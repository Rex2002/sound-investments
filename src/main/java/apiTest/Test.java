package apiTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import json.*;

public class Test {
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		String[] leewayAPIToks = { "pgz64a5qiuvw4qhkoullnx" };
		APIReq leewayAPI = new APIReq("https://api.leeway.tech/api/v1", leewayAPIToks, AuthPolicy.QUERY, "apitoken");

		List<T> l = leewayAPI.getJSONList(x -> {
			HashMap<String, JsonPrimitive<?>> obj = x.asMap();
			Double start = obj.get("open").asDouble();
			Double end = obj.get("close").asDouble();
			return new T(start, end);
		}, "public/historicalquotes/SAP.XETRA", "from", "2023-01-01");

		for (Integer i = 0; i < l.size(); i++) {
			System.out.println(i.toString() + ": " + l.get(i).toString());
		}
	}
}
