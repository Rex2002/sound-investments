package dataRepo;

public class ExtendedSonifiable {
	public final SonifiableType type;
	public final Sonifiable sonifiable;

	public ExtendedSonifiable(SonifiableType type, Sonifiable sonifiable) {
		this.type = type;
		this.sonifiable = sonifiable;
	}
}
