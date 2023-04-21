package guipanels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class NumericTextField extends JTextField {

    public NumericTextField() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                }
            }
        });
        setBorder(BorderFactory.createLineBorder(Color.RED));
    }
}