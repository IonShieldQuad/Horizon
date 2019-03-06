package core;

import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class HorizonDisplay extends JPanel {
    private static final int MARGIN_X = 50;
    private static final int MARGIN_Y = 50;
    private static final double EXTRA_AMOUNT = 0.2;
    private static final Color GRID_COLOR = Color.GRAY;
    private static final Color COLOR_START = Color.RED;
    private static final Color COLOR_END = Color.BLUE;
    private static final Color COLOR_START_BOT = Color.RED;
    private static final Color COLOR_END_BOT = Color.GREEN;
    private static final Color GRAPH_COLOR = new Color(0x5bcefa);
    private static final Color POINT_COLOR = Color.YELLOW;
    private static final int POINT_SIZE = 5;
    
    private Function function;
    
    private double lowerX ;
    private double upperX;
    private double lowerY;
    private double upperY;
    private double lowerZ;
    private double upperZ;
    private int stepsZ;
    private int offsetX;
    private int offsetY;
    private double shiftX;
    private double shiftY;
    private float hiddenAlpha;
    private boolean custom = false;
    private boolean displayingHidden = false;
    
    public HorizonDisplay() {
        super();
    }
    
    public void setFunction(Function function) {
        this.function = function;
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //drawGrid(g);
        if (function != null) {
            drawGraph(g, function::calculate);
        }
    }
    
    
    private void drawGraph(Graphics g, DoubleBinaryOperator op) {
        java.util.List<Integer> lowerHorizon = new ArrayList<>(getWidth() + 1);
        java.util.List<Integer> upperHorizon = new ArrayList<>(getWidth() + 1);
        
        for (int j = 0; j < getWidth() + 1; j++) {
            lowerHorizon.add(null);
            upperHorizon.add(null);
        }
        
        double oX = getOffsetX() - getShiftX() * (upperZ - lowerZ) / 2;
        double oY = getOffsetY() - getShiftY() * (upperZ - lowerZ) / 2;
        
        for (int k = 0; k < getStepsZ(); k++) {
            double z = interpolate(lowerZ, upperZ, (double)k / getStepsZ());

            oX += getShiftX() * (upperZ - lowerZ) / getStepsZ();
            oY += getShiftY() * (upperZ - lowerZ) / getStepsZ();
            
            //g.setColor(new Color(Color.HSBtoRGB((float) Math.random(), 1.0f, 1.0f)));
            int prev = 0;
            for (int i = 0; i < graphWidth(); i++) {
                PointDouble val = graphToValue(new PointDouble(i + MARGIN_X, 0));
                if (custom) {
                    val = new PointDouble(val.getX(), op.applyAsDouble(val.getX(), z));
                }
                else {
                    val = new PointDouble(val.getX(), f(val.getX(), z));
                }
                if (k == 1) {
                    System.out.println("X: " + val.getX() + " Y: " + val.getY());
                }
                val = valueToGraph(val);
                if (i != 0) {
                    try {
                        int x = (int) Math.round(val.getX()) + (int)Math.round(oX);
                        int y = (int) Math.round(val.getY()) - (int)Math.round(oY);
                        if (upperHorizon.get(x) == null || y > upperHorizon.get(x)) {
                            g.setColor(interpolate(COLOR_START_BOT, COLOR_END_BOT, (double)k / getStepsZ()));
                            g.drawRect(x, y, 0, 0);
                            upperHorizon.set(x, y);
                        }
                        if (lowerHorizon.get(x) == null || y < lowerHorizon.get(x)) {
                            g.setColor(interpolate(COLOR_START, COLOR_END, (double)k / getStepsZ()));
                            g.drawRect(x, y, 0, 0);
                            lowerHorizon.set(x, y);
                        }
                        if (isDisplayingHidden() && upperHorizon.get(x) != null && lowerHorizon.get(x) != null && y < upperHorizon.get(x) && y > lowerHorizon.get(x)) {
                            g.setColor(new Color(1.0f, 1.0f, 1.0f, getHiddenAlpha()));
                            g.drawRect(x, y, 0, 0);
                        }
                            //g.drawLine(MARGIN_X + i - 1, prev, (int) Math.round(val.getX()), (int) Math.round(val.getY()));
                    }
                    catch (IndexOutOfBoundsException e) {
                        //e.printStackTrace();
                    }
                }
                prev = (int) Math.round(val.getY());
            }
        }
    }
    
    private void drawGrid(Graphics g) {
        g.setColor(GRID_COLOR);
        g.drawLine(MARGIN_X, getHeight() - MARGIN_Y, getWidth() - MARGIN_X, getHeight() - MARGIN_Y);
        g.drawLine(MARGIN_X, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)), getWidth() - MARGIN_X, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)));
        g.drawLine(MARGIN_X, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT), getWidth() - MARGIN_X, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT));
        
        g.drawLine(MARGIN_X, getHeight() - MARGIN_Y, MARGIN_X, MARGIN_Y);
        g.drawLine(MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), getHeight() - MARGIN_Y, MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), MARGIN_Y);
        g.drawLine(MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), getHeight() - MARGIN_Y, MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), MARGIN_Y);
        
        g.drawString(Double.toString(lowerX()), MARGIN_X + (int)(graphWidth() * EXTRA_AMOUNT), getHeight() - MARGIN_Y / 2);
        g.drawString(Double.toString(upperX()), MARGIN_X + (int)(graphWidth() * (1 - EXTRA_AMOUNT)), getHeight() - MARGIN_Y / 2);
        g.drawString(Double.toString(lowerY()), MARGIN_X / 4, MARGIN_Y + (int)(graphHeight() * (1 - EXTRA_AMOUNT)));
        g.drawString(Double.toString(upperY()), MARGIN_X / 4, MARGIN_Y + (int)(graphHeight() * EXTRA_AMOUNT));
    }
    
    private int graphWidth() {
        return getWidth() - 2 * MARGIN_X;
    }
    
    private int graphHeight() {
        return getHeight() - 2 * MARGIN_Y;
    }
    
    private double lowerX() {
        return lowerX;
    }
    
    private double upperX() {
        return upperX;
    }
    
    private double lowerY() {
        return lowerY;
    }
    
    private double upperY() {
        return upperY;
    }
    
    public void setLowerX(double lowerX) {
        this.lowerX = lowerX;
    }
    
    public void setUpperX(double upperX) {
        this.upperX = upperX;
    }
    
    public void setLowerY(double lowerY) {
        this.lowerY = lowerY;
    }
    
    public void setUpperY(double upperY) {
        this.upperY = upperY;
    }
    
    public double lowerZ() {
        return lowerZ;
    }
    
    public void setLowerZ(double lowerZ) {
        this.lowerZ = lowerZ;
    }
    
    public double upperZ() {
        return upperZ;
    }
    
    public void setUpperZ(double upperZ) {
        this.upperZ = upperZ;
    }
    
    public int getStepsZ() {
        return stepsZ;
    }
    
    public void setStepsZ(int stepsZ) {
        this.stepsZ = stepsZ;
    }
    
    public int getOffsetX() {
        return offsetX;
    }
    
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    
    public int getOffsetY() {
        return offsetY;
    }
    
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
    
    public double getShiftX() {
        return shiftX;
    }
    
    public void setShiftX(double shiftX) {
        this.shiftX = shiftX;
    }
    
    public double getShiftY() {
        return shiftY;
    }
    
    public void setShiftY(double shiftY) {
        this.shiftY = shiftY;
    }
    
    private PointDouble valueToGraph(PointDouble point) {
        double valX = (point.getX() - lowerX()) / (upperX() - lowerX());
        double valY = (point.getY() - lowerY()) / (upperY() - lowerY());
        return new PointDouble(MARGIN_X + (int)((graphWidth() * EXTRA_AMOUNT) * (1 - valX) + (graphWidth() * (1 - EXTRA_AMOUNT)) * valX), getHeight() - MARGIN_Y - (int)((graphHeight() * EXTRA_AMOUNT) * (1 - valY) + (graphHeight() * (1 - EXTRA_AMOUNT)) * valY));
    }
    
    private PointDouble graphToValue(PointDouble point) {
        double valX = (point.getX() - (MARGIN_X + (graphWidth() * EXTRA_AMOUNT))) / ((MARGIN_X + (graphWidth() * (1 - EXTRA_AMOUNT))) - (MARGIN_X + (graphWidth() * EXTRA_AMOUNT)));
        double valY = (point.getY() - (MARGIN_Y + (graphHeight() * (1 - EXTRA_AMOUNT)))) / ((MARGIN_Y + (graphHeight() * EXTRA_AMOUNT)) - (MARGIN_Y + (graphHeight() * (1 - EXTRA_AMOUNT))));
        return new PointDouble(lowerX() * (1 - valX) + upperX() * valX, lowerY() * (1 - valY) + upperY() * valY);
    }
    
    private double interpolate(double a, double b, double alpha) {
        return b * alpha + a * (1 - alpha);
    }
    
    private Color interpolate(Color c1, Color c2, double alpha) {
        double gamma = 2.2;
        int r = (int)Math.round(255 * Math.pow(Math.pow(c2.getRed() / 255.0, gamma) * alpha + Math.pow(c1.getRed() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        int g = (int)Math.round(255 * Math.pow(Math.pow(c2.getGreen() / 255.0, gamma) * alpha + Math.pow(c1.getGreen() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        int b = (int)Math.round(255 * Math.pow(Math.pow(c2.getBlue() / 255.0, gamma) * alpha + Math.pow(c1.getBlue() / 255.0, gamma) * (1 - alpha), 1 / gamma));
        
        return new Color(r, g, b);
    }
    
    private double f(double x, double z) {
        return (1/5.0) * Math.sin(x) * Math.cos(z) - (3/2.0) * Math.cos(7 * (Math.pow(x - Math.PI, 2) + Math.pow(z - Math.PI, 2))/4) * Math.exp(-(Math.pow(x - Math.PI, 2) + Math.pow(z - Math.PI, 2)));
    }
    
    public boolean isCustom() {
        return custom;
    }
    
    public void setCustom(boolean custom) {
        this.custom = custom;
    }
    
    public boolean isDisplayingHidden() {
        return displayingHidden;
    }
    
    public void setDisplayingHidden(boolean displayingHidden) {
        this.displayingHidden = displayingHidden;
    }
    
    public float getHiddenAlpha() {
        return hiddenAlpha;
    }
    
    public void setHiddenAlpha(float hiddenAlpha) {
        this.hiddenAlpha = hiddenAlpha;
    }
}

