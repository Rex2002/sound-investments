package audio.synth;

public class EvInstrData {
    EvInstrEnum type;
    boolean[] values;

    public EvInstrData(EvInstrEnum type, boolean[] values){
        this.type = type;
        this.values = values;
    }
}