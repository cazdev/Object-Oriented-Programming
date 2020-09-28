import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.util.*;

public class Stage {
    Grid grid;
    ArrayList<Actor> actors;
    List<Cell> cellOverlay;
    List<MenuItem> menuOverlay;
    Optional<Actor> actorInAction;

    //enum State {ChoosingActor, SelectingNewLocation, CPUMoving, SelectingMenuItem, SelectingTarget}
    GameState currentState = new ChoosingActor();
    
    public Stage(){
        grid = new Grid();
        actors = new ArrayList<Actor>();
        cellOverlay = new ArrayList<Cell>();
        menuOverlay = new ArrayList<MenuItem>();
        currentState =new ChoosingActor();  // TODO: do we need this and line 14??
    }

    public void paint(Graphics g, Point mouseLoc){

        currentState.paint(g, this);

        for(Actor a: actors){
            if(!a.isTeamRed() && !a.isTeamBlue()) {
                a.setLocation(grid.randomCell());
            }
        }

        grid.paint(g,mouseLoc);
        grid.paintOverlay(g, cellOverlay, new Color(0f, 0f, 1f, 0.5f));

        for(Actor a: actors){
            a.paint(g);   
        }
        
        // state display
        g.setColor(Color.DARK_GRAY);
        g.drawString(currentState.toString(),grid.maxCols*grid.cellSize + 30,20);

        // display cell
        Optional<Cell> cap = grid.cellAtPoint(mouseLoc);
        if (cap.isPresent()){
            Cell capc = cap.get();
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.valueOf(capc.col) + String.valueOf(capc.row), grid.maxCols*grid.cellSize + 30, 50);
            g.drawString(capc.description, grid.maxCols*grid.cellSize + 120, 50);
            g.drawString("movement cost", grid.maxCols*grid.cellSize + 30, 65);
            g.drawString(String.valueOf(capc.movementCost()), grid.maxCols*grid.cellSize + 120, 65);
        } 

        // agent display
        int yloc = 138;
        int i = 0;
        for(int j = 0; j < actors.size(); j++){
            Actor a = actors.get(j);
            if (!(a instanceof ImpAdapter)){
                g.drawString(a.getClass().toString(),grid.maxCols*grid.cellSize + 20, yloc + 70*i);
                g.drawString("location:", grid.maxCols*grid.cellSize + 30, yloc + 13 + 70 * i);
                g.drawString(Character.toString(a.getLocation().col) + Integer.toString(a.getLocation().row), grid.maxCols*grid.cellSize + 120, yloc + 13 + 70 * i);
                g.drawString("redness:", grid.maxCols*grid.cellSize + 30, yloc + 26 + 70*i);
                g.drawString(Float.toString(a.redness), grid.maxCols*grid.cellSize + 120, yloc + 26 + 70*i);
                g.drawString("strat:", grid.maxCols*grid.cellSize + 30, yloc + 39 + 70*i);
                g.drawString(a.strat.toString(), grid.maxCols*grid.cellSize + 120, yloc + 39 + 70*i);
                
                i++;
            }
        }

        // menu overlay (on top of everything else)
        for(MenuItem mi: menuOverlay){
            mi.paint(g);
        }


    }

    public List<Cell> getClearRadius(Cell from, int size, boolean considerCost){
        CPUMoving.lock.lock();
        List<Cell> init = grid.getRadius(from, size, considerCost);
        CPUMoving.lock.unlock();
        for(Actor a: actors){
            CPUMoving.lock.lock();
            init.remove(a.loc);
            CPUMoving.lock.unlock();
        }
        return init;
    }

    public void mouseClicked(int x, int y){
        currentState.mouseClick(x, y, this);
    }

    public Optional<Actor> actorAt(Cell c){
        for(Actor a: actors){
            if (a.loc == c){
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public Set<Cell> occupiedCells(){
        Set<Cell> ret = new HashSet<Cell>();
        for(Actor a: actors){
            if (a instanceof ImpAdapter) {
                ret.add(a.getLocation());
            }
        }
        return ret;
    }
}