package Main;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AppTimer
{
    private Timer timer;
    private int elapsedTime = 0; // in seconds
    private boolean running = false;
    private final List<Runnable> listeners = new ArrayList<>();

    public AppTimer()
    {
        timer = new Timer(1000, e ->
        {
            if (running)
            {
                elapsedTime++;
                notifyListeners();
            }
        });
        timer.start();
    }

    public void start()
    {
        running = true;
    }

    public void pause()
    {
        running = false;
    }

    public void resume()
    {
        running = true;
    }

    public void reset()
    {
        running = false;
        elapsedTime = 0;
        notifyListeners();
    }

    public void addListener(Runnable listener)
    {
        listeners.add(listener);
    }

    private void notifyListeners()
    {
        for (Runnable listener : listeners)
        {
            listener.run();
        }
    }

    public String getFormattedTime()
    {
        int hours = elapsedTime / 3600;
        int minutes = (elapsedTime % 3600) / 60;
        int seconds = elapsedTime % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

