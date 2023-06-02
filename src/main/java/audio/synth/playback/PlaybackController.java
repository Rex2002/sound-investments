package audio.synth.playback;

import app.communication.EventQueues;
import audio.Util;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class PlaybackController {
    public final int SKIP_LENGTH = 10;
    private final SourceDataLine s;
    private final short[] data;

    public PlaybackController(SourceDataLine s, short[] data) {
        this.s = s;
        this.data = data;
    }

    public PlaybackController(SourceDataLine s, double[] data){
        this.s = s;
        this.data = Util.scaleToShort(data);
    }

    public double getPlayedPercentage() {
        return Playback.playedPercentage;
    }

    public void startPlayback() {
        EventQueues.toPlayback.clear();
        Thread playController = new Thread(new Playback(s, data));
        playController.start();
    }

    public void play() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.PLAY);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.PAUSE);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.RESET);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void skipForward() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.SKIP_FORWARD, SKIP_LENGTH);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void skipBackward() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.SKIP_BACKWARD, SKIP_LENGTH);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Goes to a specific time in the audio stream
    // The input is the percentage of the entire audio stream
    // that should be skipped to
    public void goToRelative(double percentage) {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.GOTO, percentage);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.STOP);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void save(String filename) {
        byte[] byteData = Util.convertShortToByte(data);
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(byteData), s.getFormat(), byteData.length);
        File outFile = new File(filename);
        try {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void kill() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.KILL);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}