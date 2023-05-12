package synth.generators;

import static synth.Test.SAMPLE_RATE;

public class PhaseAdvancers {


    public static double advancePhaseSine(double phase, double freq){
        phase += 2 * Math.PI * freq/SAMPLE_RATE;
        while (phase > 2 * Math.PI){
            phase -= 2* Math.PI;
        }
        while (phase < 0){
            phase += 2 * Math.PI;
        }
        return phase;
    }

    public static void advancePhaseSquare(PhaseContainer phases, double freq){
        phases.phase += freq/SAMPLE_RATE;

        while (phases.phase > 1.0f){
            phases.phase -= 1.0f;
        }
        while (phases.phase < 0.0f){
            phases.phase += 1.0f;
        }
        if (phases.phase >= 0.5f){
            phases.ret = 1.0f;
        }
        else{
            phases.ret = -1.0f;
        }
    }
    public static void advancePhaseSawtooth(PhaseContainer phases, double freq){
        phases.phase += freq/SAMPLE_RATE;
        while(phases.phase > 1.0f)
            phases.phase -= 1.0f;

        while(phases.phase < 0.0f)
            phases.phase += 1.0f;
        phases.ret = (phases.phase * 2) - 1;
    }


}