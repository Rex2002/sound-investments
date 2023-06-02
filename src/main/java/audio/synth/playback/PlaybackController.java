package audio.synth.playback;

import app.communication.EventQueues;
import audio.Util;

import javax.sound.sampled.SourceDataLine;

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

    public void kill() {
        PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.KILL);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}