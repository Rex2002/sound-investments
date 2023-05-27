package app.communication;

import dataRepo.FilterFlag;

public class SonifiableFilter {
	public String prefix;
	public FilterFlag categoryFilter;

	public SonifiableFilter(String prefix, FilterFlag categoryFilter) {
		this.prefix = prefix;
		this.categoryFilter = categoryFilter;
	}
}
