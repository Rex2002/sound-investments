package audio.synth.playback;

public class PlayControlEvent {
    private PlayControlEventsEnum type;
    private int duration;
    private double goToRelative;

    public PlayControlEvent(PlayControlEventsEnum type, double goToRelative) {
        this.type = type;
        this.goToRelative = goToRelative;
    }

    public PlayControlEvent(PlayControlEventsEnum type, int duration) {
        this.type = type;
        this.duration = duration;
    }

    public PlayControlEvent(PlayControlEventsEnum type) {
        this.type = type;
    }

    public PlayControlEvent() {
    }

    public void setType(PlayControlEventsEnum type) {
        this.type = type;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setGoToRelative(double goToRelative) {
        this.goToRelative = goToRelative;
    }

    public PlayControlEventsEnum getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public double getGoToRelative() {
        return goToRelative;
    }

    @Override
    public String toString() {
        return "PlayControlEvent{" +
                "type=" + type +
                ", duration=" + duration +
                ", goToRelative=" + goToRelative +
                '}';
    }
}