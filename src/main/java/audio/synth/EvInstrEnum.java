package audio.synth;

public enum EvInstrEnum {

    GONG,
    GING,
    GANG;

    public String toFileName(){
        switch (this){
            case GONG -> {
                return "CPA_Free_Impact_30.wav";
            }
            case GANG -> {
                // TODO
                return "";
            }
            case GING -> {
                //TODO
                return "";
            }
            default -> {
                // TODO
                return "defaultSample";
            }
        }
    }

}