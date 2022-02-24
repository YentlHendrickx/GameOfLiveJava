import javax.swing.*;
import java.awt.*;

public class Grid extends JPanel {

    public int gridSize;
    public int gridPixels;
    public int xOffset;
    public int yOffset;
    public Cell[][] cells;

    public Grid(int gridSize, int gridPixels, int xOffset, int yOffset) {
        this.gridSize = gridSize;
        this.gridPixels = gridPixels;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.cells = new Cell[gridSize][gridSize];
    }

    public void setCells(Cell[][] cells) {
//        this.cells = new Cell[gridSize][gridSize];
        this.cells = cells;
    }

    public void paint(Graphics g) {

        // Draw background
        if (this.cells[0][0] != null) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(xOffset, yOffset, (gridPixels * gridSize) + 1, (gridPixels * gridSize) + 1);

            // Draw white rects
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    if (cells[x][y].getState()) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect((x * gridPixels) + xOffset + 1, (y * gridPixels) + yOffset + 1, gridPixels-1, gridPixels-1);
                }
            }
        }
    }

    public void drawNewCell(int celIndexX, int celIndexY, int offsetX, int offsetY, Graphics g) {
        Cell target = cells[celIndexX][celIndexY];

        // Determine to color black or white
        if (target.getState()) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.WHITE);
        }

        g.fillRect((target.getX()) + offsetX + 1, (target.getY()) + offsetY + 1 , gridPixels -1, gridPixels-1);
    }

}
