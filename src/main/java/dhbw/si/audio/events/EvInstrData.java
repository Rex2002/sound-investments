package dhbw.si.audio.events;
/**
 * @author B. Frahm
 * @reviewer M. Richert
 */
public class EvInstrData {
    final EvInstrEnum type;
    final boolean[] values;

    public EvInstrData(EvInstrEnum type, boolean[] values){
        this.type = type;
        this.values = values;
    }
}