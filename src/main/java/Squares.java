import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Squares extends JFrame {
    private JButton startB = new JButton("Start");
    private JButton endB = new JButton("End");
    private JButton pauseB = new JButton("Pause");
    private JButton resumeB = new JButton("Resume");

    private JPanel buttonsPanel = new JPanel();
    private SquaresPanel squaresPanel = new SquaresPanel();

    private SquareDrawerThread drawerThread;

    public Squares() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(startB);
        buttonsPanel.add(endB);
        buttonsPanel.add(pauseB);
        buttonsPanel.add(resumeB);

        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.add(squaresPanel, BorderLayout.CENTER);

        this.setBounds(100, 100, 1000, 500);
        this.setVisible(true);


        startB.addActionListener(e -> {
            if (drawerThread == null || !drawerThread.isAlive()) {
                drawerThread = new SquareDrawerThread(squaresPanel);
                drawerThread.start();
            }
        });

        pauseB.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.pauseDrawing();
            }
        });

        resumeB.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.resumeDrawing();
            }
        });

        endB.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.terminate();
                drawerThread = null;
            }
            squaresPanel.clearSquares();
        });
    }


    static class SquaresPanel extends JPanel {
        private final List<Rectangle> squares = new ArrayList<>();

        public void addSquare(Rectangle square) {
            squares.add(square);
            repaint();
        }

        public void clearSquares() {
            squares.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            for (Rectangle square : squares) {
                g.fillRect(square.x, square.y, square.width, square.height);
            }
        }
    }


    static class SquareDrawerThread extends Thread {
        private final SquaresPanel panel;
        private volatile boolean running = true;
        private volatile boolean paused = false;

        private final Object lock = new Object();
        private int x = 10;

        public SquareDrawerThread(SquaresPanel panel) {
            this.panel = panel;
        }

        public void pauseDrawing() {
            paused = true;
        }

        public void resumeDrawing() {
            synchronized (lock) {
                paused = false;
                lock.notify();
            }
        }

        public void terminate() {
            running = false;
            resumeDrawing();
        }

        @Override
        public void run() {
            while (running) {
                synchronized (lock) {
                    if (paused) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }


                Rectangle square = new Rectangle(x, 50, 50, 50);
                panel.addSquare(square);
                x += 60;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Squares::new);
    }
}
