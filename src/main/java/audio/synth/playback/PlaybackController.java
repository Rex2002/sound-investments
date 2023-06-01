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
    public void startPlayback(){
        EventQueues.toPlayback.clear();
        Thread playController = new Thread(new Playback(s, data));
        playController.start();
    }

    public void play() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.PLAY);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.PAUSE);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.RESET);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void skipForward() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.SKIP_FORWARD);
        p.setDuration(SKIP_LENGTH);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void skipBackward() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.SKIP_BACKWARD);
        p.setDuration(SKIP_LENGTH);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.STOP);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.KILL);
        try {
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}