package audio.synth.fx;

import lombok.Data;

@Data
public class FilterData {
    double[] cutoff;
    double[] bandwidth;
    boolean highPass;
}