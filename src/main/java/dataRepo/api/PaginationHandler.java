package dataRepo.api;

import java.util.function.Function;

import dataRepo.json.JsonPrimitive;

public class PaginationHandler {
	public Function<JsonPrimitive<?>, Integer> getTotal;
	public Function<JsonPrimitive<?>, JsonPrimitive<?>> getJsonData;

	public PaginationHandler(Function<JsonPrimitive<?>, Integer> getTotal,
			Function<JsonPrimitive<?>, JsonPrimitive<?>> getJsonData) {
		this.getTotal = getTotal;
		this.getJsonData = getJsonData;
	}
}
