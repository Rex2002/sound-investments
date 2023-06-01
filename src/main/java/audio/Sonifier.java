package audio;

import app.AppError;
import app.mapping.InstrumentDataRaw;
import audio.harmonizer.Harmonizer;
import audio.mixer.Mixer;
import audio.synth.EvInstrData;
import audio.synth.EvInstrLine;
import audio.synth.InstrumentData;
import audio.synth.SynthLine;
import audio.synth.playback.PlaybackController;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static audio.Util.concatArrays;

public class Sonifier {
    public static PlaybackController sonify(InstrumentDataRaw[] instrumentDataRaw, EvInstrData[] evInstrData, int length) throws AppError{
        InstrumentData[] instrumentData = new InstrumentData[instrumentDataRaw.length];
        double[][] synthLines = new double[instrumentDataRaw.length][];
        for(int i = 0; i < instrumentDataRaw.length; i++){
            InstrumentData instrData = new Harmonizer(instrumentDataRaw[i], length).harmonize();

            synthLines[i] = new SynthLine(instrData, length).synthesize();
        }

        double[][] evInstrs = new double[evInstrData.length][];
        for(int i = 0; i < evInstrData.length; i++){
            evInstrs[i] = new EvInstrLine(evInstrData[i], length).synthesize();
        }

        double[][] outArrays = concatArrays(synthLines, evInstrs);

        double[] out = Mixer.mixAudioStreams(outArrays);
        AudioFormat af = new AudioFormat(Constants.SAMPLE_RATE, 16, Constants.CHANNEL_NO, true, true);
        SourceDataLine sdl;
        try {
            sdl = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            // TODO exception handling
            throw new RuntimeException(e);
        }
        PlaybackController controller = new PlaybackController(sdl, out);

        return controller;
    }


}