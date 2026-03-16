package Main.service;

import java.awt.AWTEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class InactivityMonitor
{
    private final int inactivityLimitMs;
    private Timer checkTimer;
    private long lastActivityTime;
    private boolean isRunning = false;
    private final List<InactivityListener> listeners = new ArrayList<>();

    public interface InactivityListener
    {
        void onInactivityDetected();
        void onActivityResumed();
    }

    public InactivityMonitor(int inactivityMinutes)
    {
        this.inactivityLimitMs = inactivityMinutes * 10000;
        this.lastActivityTime = System.currentTimeMillis();
        this.checkTimer = new Timer(10000, e -> checkInactivity());

        //for testing
        //this.inactivityLimitMs = 5000;
        //this.lastActivityTime = System.currentTimeMillis();
        //this.checkTimer = new Timer(1000, e -> checkInactivity());

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            lastActivityTime = System.currentTimeMillis();
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    public void addListener(InactivityListener listener)
    {
        listeners.add(listener);
    }

    public void start()
    {
        isRunning = true;
        checkTimer.start();
    }

    public void stop()
    {
        isRunning = false;
        checkTimer.stop();
    }

    private void checkInactivity()
    {
        if (!isRunning) return;

        long timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime;
        if (timeSinceLastActivity > inactivityLimitMs)
        {
            SwingUtilities.invokeLater(() -> {
                listeners.forEach(InactivityListener::onInactivityDetected);
            });
        }
    }

    public void resetTimer()
    {
        lastActivityTime = System.currentTimeMillis();
    }
}