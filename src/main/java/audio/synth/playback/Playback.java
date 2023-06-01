package audio.synth.playback;

import app.communication.EventQueues;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Playback implements Runnable {
    private static final int PLAYBACK_SAMPLE_SIZE = 4410;
    private final SourceDataLine s;
    private final short[] data;
    private int positionPointer;
    private boolean paused;
    private boolean running;

    public Playback(SourceDataLine s, short[] data) {
        this.s = s;
        this.data = data;
    }

    @Override
    public void run() {
        // TODO test / implement edge behaviour
        System.out.println("Entering playWithControls.");
        System.out.println(
                "The audio playback can currently be controlled with the following commands: \n p: pause \n r: resume \n jf: jump forward 2s \n jb: jump backwards 2s \n rs: reset to start \n s: kill  \n ");
        System.out.println(
                "Please do not try to test edge-case behaviour. It is untested and may result in outOfBoundsExceptions");
        positionPointer = 0;
        paused = false;
        running = true;
        System.out.println("started thread for playback controller.");
        System.out.println("playBackSampleSize/data.length: " + data.length / PLAYBACK_SAMPLE_SIZE);

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
                System.out.println("getting a value from the event queue:" + EventQueues.toPlayback.peek());
                PlayControlEvent nextEvent = new PlayControlEvent();
                try {
                    nextEvent = EventQueues.toPlayback.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switch (nextEvent.getType()) {
                    case PAUSE -> paused = true;
                    case PLAY -> paused = false;
                    case SKIP_FORWARD -> positionPointer += nextEvent.getDuration();
                    case SKIP_BACKWARD -> positionPointer = Math.max(positionPointer - nextEvent.getDuration(), 0);
                    case RESET -> positionPointer = 0;
                    case STOP -> {
                        positionPointer = 0;
                        paused = true;
                    }
                    case KILL -> running = false;
                }
            }
        }
        s.drain();
        s.close();
        System.out.println("finished loop");
    }
}