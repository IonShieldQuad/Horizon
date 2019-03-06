package core;

import org.mariuszgromada.math.mxparser.Function;

import javax.swing.*;
import java.util.function.DoubleBinaryOperator;

public class MainWindow {
    private JPanel rootPanel;
    private JTextArea log;
    private JTextField lowerX;
    private JTextField upperX;
    private JButton calculateButton;
    private HorizonDisplay graph;
    private JTextField functionField;
    private JTextField lowerY;
    private JTextField upperY;
    private JTextField lowerZ;
    private JTextField upperZ;
    private JTextField Zsteps;
    private JTextField Xoffset;
    private JTextField Yoffset;
    private JTextField shiftX;
    private JTextField shiftY;
    private JCheckBox useCustomFunctionCheckBox;
    private JCheckBox displayHiddenCheckBox;
    private JTextField hiddenAlpha;
    
    private Function function;
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
    }
    
    
    
    private void calculate() {
        try {
            log.setText("");
            function = new Function(functionField.getText());
            if (!function.checkSyntax()) {
                log.append("\nInvalid function input");
            }
            else {
                new Thread(this::updateGraph).start();
                log.append("\nGenerated");
            }
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
    }
    
    private void updateGraph() {
        graph.setFunction(function);
        graph.setLowerX(Double.parseDouble(lowerX.getText()));
        graph.setUpperX(Double.parseDouble(upperX.getText()));
        graph.setLowerY(Double.parseDouble(lowerY.getText()));
        graph.setUpperY(Double.parseDouble(upperY.getText()));
        graph.setLowerZ(Double.parseDouble(lowerZ.getText()));
        graph.setUpperZ(Double.parseDouble(upperZ.getText()));
        graph.setStepsZ(Integer.parseInt(Zsteps.getText()));
        graph.setOffsetX(Integer.parseInt(Xoffset.getText()));
        graph.setOffsetY(Integer.parseInt(Yoffset.getText()));
        graph.setShiftX(Double.parseDouble(shiftX.getText()));
        graph.setShiftY(Double.parseDouble(shiftY.getText()));
        graph.setHiddenAlpha(Math.max(Math.min(Float.parseFloat(hiddenAlpha.getText()), 1.0f), 0.0f));
        graph.setCustom(useCustomFunctionCheckBox.isSelected());
        graph.setDisplayingHidden(displayHiddenCheckBox.isSelected());
        graph.repaint();
    }
    
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Horizon");
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
}
