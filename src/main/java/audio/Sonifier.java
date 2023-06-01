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
import audio.synth.playback.PlaybackController;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static audio.Util.concatArrays;

public class Sonifier {
    public static PlaybackController sonify(InstrumentDataRaw[] instrumentDataRaw, EvInstrData[] evInstrData, int lenghtInBeats) throws AppError{
        double lengthInSeconds = lenghtInBeats / (Constants.TEMPO / 60f);

        double[][] synthLines = new double[instrumentDataRaw.length + 1][];
        for(int i = 0; i < instrumentDataRaw.length; i++){
            InstrumentData instrData = new Harmonizer(instrumentDataRaw[i], lenghtInBeats).harmonize();

            synthLines[i] = new SynthLine(instrData, lengthInSeconds).synthesize();
        }

        double[][] evInstrs = new double[evInstrData.length][];
        for(int i = 0; i < evInstrData.length; i++){
            evInstrs[i] = new EvInstrLine(evInstrData[i], lengthInSeconds).synthesize();
        }

        double[] backing = new Backing(lenghtInBeats).getBacking();
        synthLines[synthLines.length - 1] = backing;

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

        return new PlaybackController(sdl, out);
    }


}