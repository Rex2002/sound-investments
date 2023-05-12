package dataRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import apiTest.APIReq;
import apiTest.AuthPolicy;
import json.JsonPrimitive;

public class DataRepo {
	public static enum FilterFlag {
		STOCK(1 << 0),
		ETF(1 << 1),
		INDEX(1 << 2);

		private final int x;

		FilterFlag(int x) {
			this.x = x;
		}

		public int getVal() {
			return x;
		}
	}

	public static int getFilterVal(FilterFlag... flags) {
		int val = 0;
		for (FilterFlag flag : flags) {
			val |= flag.getVal();
		}
		return val;
	}

	private static final String[] apiToksLeeway = { "pgz64a5qiuvw4qhkoullnx", "9pe3xyaplenvfvbnyxtomm",
			"r7splaijduabfpcu2z2l14", "o5npdx6elm2pcpp395uaun", "2ja5gszii8g63hzjd41x78" };
	private static final String[] apiToksMarketstack = { "4b6a78c092537f07bbdedff8f134372d",
			"0c2e8a9c96f2a74c0049f4b662f47b40",
			"621fc5e0add038cc7d9697bcb7f15caa", "4312dfd8788579ec14ee9e9c9bec4557",
			"0a99047c49080d975013978d3609ca9e" };
	private static final String[] apiToksTwelvedata = { "04ed9e666cbb4873ac6d29651e2b4d7e" };

	private static APIReq apiTwelvedata = new APIReq("https://api.twelvedata.com/", apiToksTwelvedata, AuthPolicy.QUERY,
			"apikey");

	private static List<Stock> stocks = new ArrayList<>(128);
	private static List<ETF> etfs = new ArrayList<>(128);
	private static List<Index> indices = new ArrayList<>(128);

	public static void init() {
		// For debugging only:
		apiTwelvedata.setQuery("exchange", "XNYS");

		try {
			stocks = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "stocks")
					.applyList(x -> new Stock(x.asMap().get("name").asStr(),
							x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));

			etfs = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "etf")
					.applyList(x -> new ETF(x.asMap().get("name").asStr(),
							x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));

			indices = apiTwelvedata.getJSON(x -> x.asMap().get("data"), "indices")
					.applyList(x -> new Index(x.asMap().get("name").asStr(),
							x.asMap().get("symbol").asStr(), x.asMap().get("exchange").asStr()));

			System.out.println("Amount of stocks: " + stocks.size());
			System.out.println("Amount of etfs: " + etfs.size());
			System.out.println("Amount of indices: " + indices.size());
			System.out.println("Init done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Sonifiable> findByPrefix(String prefix, FilterFlag... flags) {
		// TODO: Implement actual logic here

		int flag = getFilterVal(flags);
		List<Sonifiable> l = new ArrayList<>(128);
		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			l.addAll(stocks);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			l.addAll(etfs);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			l.addAll(indices);
		return l;
	}

	public static List<Sonifiable> getSlice(int offset, int amount, FilterFlag... flags) {
		// TODO: Implement actual logic here

		int flag = getFilterVal(flags);
		List<Sonifiable> l = new ArrayList<>(128);
		if ((flag & FilterFlag.STOCK.getVal()) > 0)
			l.addAll(stocks);
		if ((flag & FilterFlag.ETF.getVal()) > 0)
			l.addAll(etfs);
		if ((flag & FilterFlag.INDEX.getVal()) > 0)
			l.addAll(indices);
		return l.subList(offset, Math.min(l.size(), offset + amount));
	}
}
