package app.communication;

import dataRepo.FilterFlag;

public class SonifiableFilter {
	public final String prefix;
	public final FilterFlag categoryFilter;

	public SonifiableFilter(String prefix, FilterFlag categoryFilter) {
		this.prefix = prefix;
		this.categoryFilter = categoryFilter;
	}
}
