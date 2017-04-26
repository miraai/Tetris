/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Mirai
 */

//Glavna, pokretacka, klasa
public class Tetris extends JFrame{

    JLabel statusbar;

    public Tetris() throws HeadlessException {
        statusbar = new JLabel(" 0"); //kreiramo status bar
        add(statusbar, BorderLayout.SOUTH); //pozicija status bara
        Board board = new Board(this); //kreiramo board
        add(board); //dodajemo board
        board.start(); //pokrece igru
        
        setSize(200, 400); //podesavamo velicinu ekrana
        setTitle("Tetris"); //dodajemo naziv
        setDefaultCloseOperation(EXIT_ON_CLOSE); //zatvaranje ige na X
    }
    
    //metoda za getovanje, uzimanje, status bara
    public JLabel getStatusBar() { 
        return statusbar;
    }
    
    //main metoda, glavna metoda koja pokrece program
    public static void main(String[] args) {
        Tetris game = new Tetris(); //instanca tetris klase
        game.setLocationRelativeTo(null); //lokacija na sredini ekrana
        game.setVisible(true); //vidljivost
    }
    
}
