package audio.synth.playback;

public class PlayControlEvent {
    PlayControlEventsEnum type;
    int duration;

    public void setType(PlayControlEventsEnum type) {
        this.type = type;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public PlayControlEventsEnum getType() {
        return type;
    }

    public int getDuration(){
        return duration;
    }

    @Override
    public String toString() {
        return "PlayControlEvent{" +
                "type=" + type +
                ", duration=" + duration +
                '}';
    }
}