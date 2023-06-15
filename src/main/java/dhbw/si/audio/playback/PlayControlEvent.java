package dhbw.si.audio.playback;

import lombok.Data;

/**
 * @author V. Richter
 * @reviewer B. Frahm
 * @reviewer L. Lehman
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