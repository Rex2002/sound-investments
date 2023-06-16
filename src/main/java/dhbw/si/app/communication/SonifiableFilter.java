package dhbw.si.app.communication;

import dhbw.si.dataRepo.FilterFlag;

/**
 * @author V. Richter
 */
public class SonifiableFilter {
	public final String prefix;
	public final FilterFlag categoryFilter;

	public SonifiableFilter(String prefix, FilterFlag categoryFilter) {
		this.prefix = prefix;
		this.categoryFilter = categoryFilter;
	}
}
