package dhbw.si.audio;

import dhbw.si.app.AppError;
import dhbw.si.app.mapping.GlobalFxDataRaw;
import dhbw.si.app.mapping.InstrumentDataRaw;
import dhbw.si.audio.events.EvInstrData;
import dhbw.si.audio.events.EvInstrLine;
import dhbw.si.audio.mixer.Backing;
import dhbw.si.audio.mixer.Mixer;
import dhbw.si.audio.synth.*;
import dhbw.si.audio.synth.fx.Effect;
import dhbw.si.audio.playback.PlaybackController;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static dhbw.si.audio.Util.concatArrays;

public class Sonifier {

    /**
     * orchestrates the sonification based on the passed data <br/>
     * @param instrumentDataRaw contains one element for each instrument that is meant to be sonified
     * @param evInstrData contains one element for each event instrument that is meant to be sonified
     * @param globalFxDataRaw contains the global fx-data (filter & reverb)
     * @param lengthInSecondsRaw holds the target length of the sonification
     * @return a playbackController object that contains the finished sonification
     * @throws AppError if an error, about which the user should be informed, occurs an AppError is thrown (which is subsequently shown in the UI)
     */
    public static PlaybackController sonify(InstrumentDataRaw[] instrumentDataRaw, EvInstrData[] evInstrData, GlobalFxDataRaw globalFxDataRaw, int lengthInSecondsRaw) throws AppError {
        Backing backing = new Backing();
        Constants.TEMPO = backing.setSamplesAndGetTempo();

        double numberBeatsRaw = (Constants.TEMPO / 60f) * lengthInSecondsRaw;
        // get number of beats to nearest multiple of 16 so that dhbw.si.audio always lasts for
        // a full multiple of 4 bars
        int lengthInBeats = (int) Math.round(numberBeatsRaw / 16) * 16;
        double lengthInSeconds = lengthInBeats / (Constants.TEMPO / 60f);

        double[][] synthLines = new double[instrumentDataRaw.length + 1][];
        for(int i = 0; i < instrumentDataRaw.length; i++){
            InstrumentData instrData = new Harmonizer(instrumentDataRaw[i], lengthInBeats).harmonizeInstrumentData();

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

        GlobalFxData globalFxData = new Harmonizer(globalFxDataRaw, lengthInBeats).harmonizeGlobalData();
        if(globalFxData.getDelayReverb() != null && globalFxData.getFeedbackReverb() != null)
            out = Effect.echoWithFeedback(out, globalFxData.getFeedbackReverb(), globalFxData.getDelayReverb());
        if(globalFxData.getFilterData() != null)
            out = Effect.IIR(out, globalFxData.getFilterData());

        out = Effect.antiAliasing(out);
        AudioFormat af = new AudioFormat(Constants.SAMPLE_RATE, 16, Constants.CHANNEL_NO, true, true);
        SourceDataLine sdl;
        try {
            sdl = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            // TODO exception handling
            throw new AppError("Zugriff auf Lautsprecher verweigert");
        }

        return new PlaybackController(sdl, out, lengthInSeconds);
    }
}