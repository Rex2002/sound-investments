package dhbw.si.app.mapping;

import dhbw.si.dataRepo.SonifiableID;
import lombok.Data;

/**
 * @author V. Richter
 */
@Data
public class ExchangeData<T extends ExchangeParam> {
	private SonifiableID id;
	private T data;

	public ExchangeData(SonifiableID id, T data) {
		this.id = id;
		this.data = data;
	}
}
