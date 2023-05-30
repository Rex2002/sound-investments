package dataRepo.api;

import java.util.Objects;

import dataRepo.json.JsonPrimitive;

public class HandledPagination {
	private JsonPrimitive<?> restJson;
	private boolean done;

	public HandledPagination(JsonPrimitive<?> restJson, boolean done) {
		this.restJson = restJson;
		this.done = done;
	}

	public JsonPrimitive<?> getRestJson() {
		return this.restJson;
	}

	public void setRestJson(JsonPrimitive<?> restJson) {
		this.restJson = restJson;
	}

	public boolean isDone() {
		return this.done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof HandledPagination)) {
			return false;
		}
		HandledPagination handledPagination = (HandledPagination) o;
		return Objects.equals(restJson, handledPagination.restJson) && done == handledPagination.done;
	}
}
