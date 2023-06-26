package dhbw.si.audio.playback;

import dhbw.si.app.communication.EventQueues;
import dhbw.si.util.Dev;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * @author B. Frahm
 * @reviewer V. Richter
 * @reviewer L. Wellhausen
 */
public class Playback implements Runnable {
    // This value is written to by this class and read by the PlaybackController
    // Race-Conditions are ignored, because it is ok if we are a couple percentage points behind as that wouldn't be visible in the UI anyways
    // Thus, we simply use a thread-unsafe global variable to reduce performance overheads of thread-safe alternatives
    public static double playedPercentage = 0;

    private static final int PLAYBACK_SAMPLE_SIZE = 4410;
    private final SourceDataLine s;
    private final short[] data;

    public Playback(SourceDataLine s, short[] data) {
        this.s = s;
        this.data = data;
    }

    @Override
    public void run() {
        int positionPointer = 0;
        boolean paused = false;
        boolean running = true;

        try {
            s.open();
            s.start();
        } catch (LineUnavailableException e) {
            // TODO
            throw new RuntimeException(e);
        }

        byte[] outBuffer = new byte[PLAYBACK_SAMPLE_SIZE * 2];
        while (running) {
            if (!paused && positionPointer < data.length / PLAYBACK_SAMPLE_SIZE) {
                for (int i = 0; i < PLAYBACK_SAMPLE_SIZE; i++) {
                    outBuffer[2 * i] = (byte) ((data[positionPointer * PLAYBACK_SAMPLE_SIZE + i] >> 8) & 0xFF);
                    outBuffer[2 * i + 1] = (byte) (data[positionPointer * PLAYBACK_SAMPLE_SIZE + i] & 0xFF);
                }
                s.write(outBuffer, 0, outBuffer.length);
                positionPointer++;
            }

            while (!EventQueues.toPlayback.isEmpty()) {
                PlayControlEvent nextEvent = new PlayControlEvent();
                try {
                    nextEvent = EventQueues.toPlayback.take();
                } catch (InterruptedException e) {
                    if (Dev.DEBUG) e.printStackTrace();
                }
                switch (nextEvent.getType()) {
                    case PAUSE -> paused = true;
                    case PLAY -> paused = false;
                    case SKIP_FORWARD -> positionPointer += nextEvent.getDuration();
                    case SKIP_BACKWARD -> positionPointer = Math.max(positionPointer - nextEvent.getDuration(), 0);
                    case GOTO -> positionPointer = (int) (nextEvent.getGoToRelative() * data.length / PLAYBACK_SAMPLE_SIZE);
                    case RESET -> positionPointer = 0;
                    case STOP -> {
                        positionPointer = 0;
                        paused = true;
                    }
                    case KILL -> running = false;
                }
            }

            playedPercentage = ((double) positionPointer) * PLAYBACK_SAMPLE_SIZE / data.length;
        }
        s.drain();
        s.close();
    }
}