package state;

public enum PointData {
	TRENDBREAK, // Beim Durchbrechen einer Trend-Formation
	EQMOVINGAVG, // Wenn absoluter Preis = gleitender Durchschnitt gilt
	EQSUPPORT, // Wenn absoluter Preis = Unterstützungs-Linie gilt
	EQRESIST; // Wenn absoluter Preis = Widerstands-Linie gilt
}
