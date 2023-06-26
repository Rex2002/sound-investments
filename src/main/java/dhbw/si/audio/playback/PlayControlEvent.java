package dhbw.si.audio.playback;

import lombok.Data;

/**
 * @author B. Frahm
 * @reviewer V. Richter
 * @reviewer L. Lehmann
 */
@Data
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
}