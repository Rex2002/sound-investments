package state;

public enum LineData {
	PRICE, // Absoluter Preis des Kurses
	MOVINGAVG, // Gleitender Durchschnitt wie in 2.3 beschrieben
	RELCHANGE; // Steigung zwischen zwei Preispunkten
}
