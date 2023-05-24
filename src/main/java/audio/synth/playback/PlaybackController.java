package audio.synth.playback;

import state.EventQueues;

import javax.sound.sampled.SourceDataLine;

public class PlaybackController{

    public final int SKIP_LENGTH = 40;
    private final SourceDataLine s;
    private final short[] data;

    public PlaybackController(SourceDataLine s, short[] data) {
        this.s = s;
        this.data = data;
    }
    public void startPlayback(){
        Thread playController = new Thread(new Playback(s, data));
        playController.start();
    }

    public void play() {
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.PLAY);
        try{
            EventQueues.toPlayback.put(p);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    public void pause(){
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.PAUSE);
        try{
            EventQueues.toPlayback.put(p);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void reset(){
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.RESET);
        try{
            EventQueues.toPlayback.put(p);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void skipForward(){
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.SKIP_FORWARD);
        p.setDuration(SKIP_LENGTH);
        try{
            EventQueues.toPlayback.put(p);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void skipBackward(){
        PlayControlEvent p = new PlayControlEvent();
        p.setType(PlayControlEventsEnum.SKIP_BACKWARD);
        p.setDuration(SKIP_LENGTH);
        try{
            EventQueues.toPlayback.put(p);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}