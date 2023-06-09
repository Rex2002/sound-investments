package audio;

import app.AppError;
import app.mapping.InstrumentDataRaw;
import audio.harmonizer.Harmonizer;
import audio.mixer.Backing;
import audio.mixer.Mixer;
import audio.synth.EvInstrData;
import audio.synth.EvInstrLine;
import audio.synth.InstrumentData;
import audio.synth.SynthLine;
import audio.synth.fx.Effect;
import audio.synth.playback.PlaybackController;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static audio.Util.concatArrays;

public class Sonifier {
    public static PlaybackController sonify(InstrumentDataRaw[] instrumentDataRaw, EvInstrData[] evInstrData, int lengthInSecondsRaw) throws AppError {
        Backing backing = new Backing();
        Constants.TEMPO = backing.setSamplesAndGetTempo();

        double numberBeatsRaw = (Constants.TEMPO / 60f) * lengthInSecondsRaw;
        // get number of beats to nearest multiple of 16 so that audio always lasts for
        // a full multiple of 4 bars
        int lengthInBeats = (int) Math.round(numberBeatsRaw / 16) * 16;
        double lengthInSeconds = lengthInBeats / (Constants.TEMPO / 60f);

        double[][] synthLines = new double[instrumentDataRaw.length + 1][];
        for(int i = 0; i < instrumentDataRaw.length; i++){
            InstrumentData instrData = new Harmonizer(instrumentDataRaw[i], lengthInBeats).harmonize();

            synthLines[i] = new SynthLine(instrData, lengthInSeconds).synthesize();
        }

        double[][] evInstrs = new double[evInstrData.length][];
        for(int i = 0; i < evInstrData.length; i++){
            evInstrs[i] = new EvInstrLine(evInstrData[i], lengthInSeconds).synthesize();
        }

        double[] backingLine = backing.getBacking(lengthInBeats / 4);
        synthLines[synthLines.length - 1] = backingLine;

        double[][] outArrays = concatArrays(evInstrs, synthLines);

        double[] out = Mixer.mixAudioStreams(outArrays, evInstrs.length, synthLines.length - 1);
        out = Effect.antiAliasing(out);
        AudioFormat af = new AudioFormat(Constants.SAMPLE_RATE, 16, Constants.CHANNEL_NO, true, true);
        SourceDataLine sdl;
        try {
            sdl = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            // TODO exception handling
            throw new RuntimeException(e);
        }

        return new PlaybackController(sdl, out, lengthInSeconds);
    }
}
