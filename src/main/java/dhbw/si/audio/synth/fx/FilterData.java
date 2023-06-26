package dhbw.si.audio.synth.fx;

import lombok.Data;
/**
 * @author B. Frahm
 */
@Data
public class FilterData {
    double[] cutoff;
    double[] bandwidth;
    boolean highPass;
}