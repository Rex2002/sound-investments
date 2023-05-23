package audio.synth;

import state.EventQueues;

import java.util.Scanner;

public class PlaybackController implements Runnable{

    String controlAction;
    @Override
    public void run() {
        while(true){
            System.out.println("Please enter your next control action: ");
            Scanner s = new Scanner(System.in);
            controlAction = s.next();
            try {
                switch (controlAction) {
                    //resume
                    case "r" -> EventQueues.toPlayback.put(-2);
                    //pause
                    case "p" -> EventQueues.toPlayback.put(-1);
                    // jump forward 1s
                    case "jf" -> EventQueues.toPlayback.put(1);
                    // jump backward 1s
                    case "jb" -> EventQueues.toPlayback.put(2);
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}