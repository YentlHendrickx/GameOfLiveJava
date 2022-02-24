import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;


/*
    // Now
    TODO: START/ STOP simulation button
    TODO: Step button

    // Later
    TODO: Don't loop over every cell to check for next gen, maybe keep array of active squares and only check their neighbors?
    TODO: Optimize Grid drawing (don't redraw square unless necessary, maybe drawing queue?)
    TODO: Grid / window size controls in JFFrame
    TODO: Icon
    TODO: Rendering (JFrame) in separate file
 */


public class Main {
    // Grid properties
    public static int gridSize = 50;

    // Grid pixels size
    public static int gridPixels = 10;

    // Window size
    public static int windowWidthPixels = 850;
    public static int windowHeightPixels = 900;
    public static boolean forceWindowSquare = false;
    public static int gridXOffset = 20;
    public static int gridYOffset = 20;

    public static int windowXActualDiff = 0;
    public static int windowYActualDiff = 0;

    public static Cell[][] cells;
    public static Cell[][] nextGenCells;

    public static JFrame frame;
    public static JButton simulateButton;
    public static Grid grid;

    public static Thread simulationThread;
    public static boolean repainted = true;

    public static boolean simRunning = false;

    public static void main(String[] args) {
        /// WINDOW MANAGING
        // Force window dimensions
        if (forceWindowSquare) {
            windowWidthPixels = Math.max(windowWidthPixels, windowHeightPixels);
            windowHeightPixels = windowWidthPixels;
        }

        // Calculate how big the grid has to be to fit in the window (we use the smallest window value to calculate)
        gridPixels = (Math.min(windowWidthPixels, windowHeightPixels) / gridSize);

        // Calculate new window size based on grid size and offset
        windowWidthPixels += (gridXOffset * 2);
        windowHeightPixels += (gridYOffset * 2);

        // Start window setup
        frame = new JFrame();
        grid = new Grid(gridSize, gridPixels, gridXOffset, gridYOffset);
        frame.setBounds(0, 0, (windowWidthPixels), (windowHeightPixels));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        grid.setBackground(Color.GRAY);
        buttonPanel.setBackground(Color.GRAY);
        simulateButton = new JButton("Resume simulation");
        JButton stepButton = new JButton("Step");
        JButton clearButton = new JButton("Clear");
        buttonPanel.add(simulateButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(clearButton);

        grid.setPreferredSize(new Dimension(gridSize*gridPixels, (gridSize*gridPixels) + gridYOffset));
        buttonPanel.setPreferredSize(new Dimension(35,10));
        container.add(grid);
        container.add(buttonPanel);
        container.setBackground(Color.GRAY);

        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(container);

        // Calculate exact bounds
        int frameX = frame.getContentPane().getSize().width;
        int frameY = frame.getContentPane().getSize().height;
        int windowX = frame.getWidth();
        int windowY = frame.getHeight();

        windowXActualDiff = windowX - frameX;
        windowYActualDiff = windowY - frameY;

        int newFrameX = windowX + windowXActualDiff;
        int newFrameY = windowY + windowYActualDiff;

        frame.setBounds(0, 0, newFrameX, newFrameY);

//        System.out.println(frame.getContentPane().getSize());
//        System.out.println(frame.getWidth());
        // Add mouse listener to frame
        frame.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!simRunning) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        handleClick(e.getX(), e.getY());
                    }
                }
            }
        });

        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simRunning = !simRunning;
                if (simRunning) {
                    simulateButton.setText("Pause simulation");
                    simulationThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (simRunning) {
                                    if (repainted) {
                                        repainted = false;
                                        updateCells();
                                    }
                                    Thread.sleep(250);
//                                    System.out.println("FALSE");

                                }
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    simulationThread.start();
                } else {
                    simulateButton.setText("Resume simulation");
                }
            }
        });

        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!simRunning) {
                    updateCells();
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simRunning = false;
                simulateButton.setText("Resume simulation");

                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        cells[i][j].setState(false);
                        nextGenCells[i][j].setState(false);
                    }
                }
                updateCells();
            }
        });

        /// ARRAY SETUPS
        // Fill arrays with empty cells
        cells = new Cell[gridSize][gridSize];
        nextGenCells = new Cell[gridSize][gridSize];
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                int startX = gridXOffset + (gridPixels * x);
                int startY = gridYOffset + (gridPixels * y);

                cells[x][y] = new Cell(startX, startY, x, y);
                nextGenCells[x][y] = new Cell(startX, startY, x, y);
            }
        }

        redraw(0, 0, true);
    }

    // Calculate cell state in next frame
    // <2 neighbors = dead
    // >3 neighbors = dead
    // 2-3 neighbors = stays alive
    // 3 neighbors AND currently dead = alive
    public static void updateCells() {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y< gridSize; y++) {
                int liveCells = cells[x][y].getLiveCellCount(cells, gridSize);

                if (cells[x][y].getState()) {
                    nextGenCells[x][y].setState(false);

                    if (liveCells >= 2 && liveCells <= 3) { nextGenCells[x][y].setState(true);}
                } else {
                    if (liveCells == 3) { nextGenCells[x][y].setState(true);}
                }
            }
        }

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                cells[x][y].setState(nextGenCells[x][y].getState());
            }
        }

        redraw(0,0,true);

    }

    public static void redraw(int cellXX, int cellYY, boolean all) {
        if (!all) {
            grid.drawNewCell(cellXX, cellYY, windowXActualDiff / 2, windowYActualDiff - (windowXActualDiff / 2), frame.getGraphics());
        } else {
            grid.setCells(cells);
            grid.repaint();
            simulateButton.repaint();
        }
        repainted = true;
    }

    // Handle mouse clicks (toggling)
    // If white square clicked => black
    // If black square clicked => white
    public static void handleClick(int x, int y) {
        // Offsets
        x -= (windowXActualDiff / 2);
        // Y offset is top bar + little bar at the bottom (same as x)
        y -= windowYActualDiff - (windowXActualDiff / 2);

//        System.out.println(x);

        int arrayX = -1;
        int arrayY = -1;

        for (int xT = x; xT > gridXOffset; xT -= gridPixels) {
            arrayX++;
        }
        for (int yT = y; yT > gridYOffset; yT -= gridPixels) {
            arrayY++;
        }

        if (arrayX >= 0 && arrayX < gridSize && arrayY >= 0 && arrayY < gridSize) {
            cells[arrayX][arrayY].toggle();

            redraw(arrayX, arrayY, false);
        }
    }
}