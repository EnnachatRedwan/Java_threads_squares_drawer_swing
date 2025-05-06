

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Squares extends JFrame {

    private static final Color BACKGROUND_COLOR = new Color(240, 240, 245);
    private static final Color BUTTON_COLOR = new Color(100, 120, 200);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color PANEL_BACKGROUND = new Color(255, 255, 255);


    private final JButton startButton = new JButton("Start");
    private final JButton endButton = new JButton("Stop");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton resumeButton = new JButton("Resume");
    private final JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 1000, 500);
    private final JComboBox<String> squareColorCombo = new JComboBox<>(new String[]{"Red", "Blue", "Green", "Random"});

    private final JPanel controlPanel = new JPanel();
    private final EnhancedSquaresPanel squaresPanel = new EnhancedSquaresPanel();

    private SquareDrawerThread drawerThread;

    public Squares() {
        super("Animated Squares");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        this.setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);


        setupControlPanel();


        squaresPanel.setBackground(PANEL_BACKGROUND);
        squaresPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        this.add(new JScrollPane(squaresPanel), BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);


        setupEventHandlers();


        this.setSize(1000, 600);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void setupControlPanel() {
        controlPanel.setLayout(new BorderLayout(5, 0));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(endButton);


        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        optionsPanel.setBackground(BACKGROUND_COLOR);


        JPanel speedPanel = new JPanel(new BorderLayout(5, 0));
        speedPanel.setBackground(BACKGROUND_COLOR);
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        speedSlider.setBackground(BACKGROUND_COLOR);
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setInverted(true);
        speedPanel.add(speedLabel, BorderLayout.WEST);
        speedPanel.add(speedSlider, BorderLayout.CENTER);


        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        colorPanel.setBackground(BACKGROUND_COLOR);
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        squareColorCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        colorPanel.add(colorLabel);
        colorPanel.add(squareColorCombo);

        optionsPanel.add(speedPanel);
        optionsPanel.add(colorPanel);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(optionsPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        ActionListener resetThread = e -> {
            if (drawerThread != null && drawerThread.isAlive()) {
                drawerThread.terminate();
            }
            drawerThread = null;
        };

        startButton.addActionListener(e -> {
            resetThread.actionPerformed(null);
            drawerThread = new SquareDrawerThread(
                    squaresPanel,
                    speedSlider.getValue(),
                    squareColorCombo.getSelectedIndex()
            );
            drawerThread.start();
            updateButtonStates(true);
        });

        pauseButton.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.pauseDrawing();
                updateButtonStates(false);
            }
        });

        resumeButton.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.resumeDrawing();
                updateButtonStates(true);
            }
        });

        endButton.addActionListener(e -> {
            resetThread.actionPerformed(null);
            squaresPanel.clearSquares();
            updateButtonStates(false);
        });

        speedSlider.addChangeListener(e -> {
            if (drawerThread != null && !speedSlider.getValueIsAdjusting()) {
                drawerThread.setSpeed(speedSlider.getValue());
            }
        });

        squareColorCombo.addActionListener(e -> {
            if (drawerThread != null) {
                drawerThread.setColorMode(squareColorCombo.getSelectedIndex());
            }
        });
    }

    private void updateButtonStates(boolean running) {
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        resumeButton.setEnabled(!running && drawerThread != null);
        endButton.setEnabled(drawerThread != null);
    }

    static class EnhancedSquaresPanel extends JPanel {
        private final List<SquareInfo> squares = new ArrayList<>();

        public EnhancedSquaresPanel() {
            setBackground(PANEL_BACKGROUND);
        }

        public void addSquare(int x, int y, int size, Color color) {
            squares.add(new SquareInfo(x, y, size, color));
            repaint();
        }

        public void clearSquares() {
            squares.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;


            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (SquareInfo square : squares) {
                g2d.setColor(square.color);


                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRect(square.x + 3, square.y + 3, square.size, square.size);


                g2d.setColor(square.color);
                g2d.fillRect(square.x, square.y, square.size, square.size);


                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawLine(square.x, square.y, square.x + square.size, square.y);
                g2d.drawLine(square.x, square.y, square.x, square.y + square.size);
            }
        }

        static class SquareInfo {
            final int x, y, size;
            final Color color;

            SquareInfo(int x, int y, int size, Color color) {
                this.x = x;
                this.y = y;
                this.size = size;
                this.color = color;
            }
        }
    }

    static class SquareDrawerThread extends Thread {
        private final EnhancedSquaresPanel panel;
        private volatile boolean running = true;
        private volatile boolean paused = false;
        private volatile int speed;
        private volatile int colorMode;

        private final Object lock = new Object();
        private int x = 10;
        private final Random random = new Random();

        public SquareDrawerThread(EnhancedSquaresPanel panel, int speed, int colorMode) {
            this.panel = panel;
            this.speed = speed;
            this.colorMode = colorMode;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public void setColorMode(int colorMode) {
            this.colorMode = colorMode;
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

        private Color getSquareColor() {
            switch (colorMode) {
                case 0: return Color.RED;
                case 1: return Color.BLUE;
                case 2: return Color.GREEN;
                case 3: default:
                    return new Color(
                            random.nextInt(256),
                            random.nextInt(256),
                            random.nextInt(256)
                    );
            }
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

                if (!running) break;


                int size = 30 + random.nextInt(40);


                int y = 50 + random.nextInt(200);


                panel.addSquare(x, y, size, getSquareColor());


                x += size + 10;


                if (x > panel.getWidth() - 100) {
                    x = 10;
                }

                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(Squares::new);
    }
}