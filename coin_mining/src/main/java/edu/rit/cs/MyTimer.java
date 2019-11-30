package edu.rit.cs;

public class MyTimer {
    private String timerName;
    private long startTime;
    private long endTime;
    private long elapsedTime;

    public MyTimer(String timerName){
        this.timerName = timerName;
    }

    public void start_timer(){
        this.startTime = System.nanoTime();
    }

    public void stop_timer(){
        this.endTime = System.nanoTime();
    }

    public void print_elapsed_time(){
        this.elapsedTime = this.endTime - this.startTime;
        System.out.println("ElapsedTime (" + this.timerName + "): "+ elapsedTime + " nano sec");
    }

    public long get_elapsed_time_in_sec() {
        return this.elapsedTime/1_000_000_000;
    }

}
