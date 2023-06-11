package dhbw.si.dataRepo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.util.Calendar;

@Data
@AllArgsConstructor
public class Price {
	public Calendar day;
	public Instant start;
	public Instant end;
	public Double open;
	public Double close;
	public Double low;
	public Double high;
}
