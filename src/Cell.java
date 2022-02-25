public class Cell {
    public boolean state;
    public int x, y, xArray, yArray;

    public Cell(int x, int y, int xArray, int yArray) {
        this.x = x; this.y = y; this.state = false; this.xArray = xArray; this.yArray = yArray;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return this.state;
    }

    public int getX() {return this.x;}
    public int getY() {return this.y;}

    public int getLiveCellCount(Cell[][] cells, int gridSize) {
        int liveNeighbours = 0;

        for (int i = this.xArray -1; i < this.xArray + 2; i++) {
            for (int j = this.yArray -1; j < this.yArray + 2; j++) {
                if (!(i == this.xArray && j == this.yArray)) {
                    if (i >= 0 && i < gridSize && j >= 0 && j < gridSize)
                        if (cells[i][j].getState()) liveNeighbours++;
                }
            }
        }

        return liveNeighbours;
    }

    public void toggle() {
        this.state = !this.state;
    }
}
