package synth.fx;

import lombok.Data;

@Data
public class FilterData {
    double[] cutoff;
    double[] order;
    boolean highPass;
}