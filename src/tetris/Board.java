/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

/**
 *
 * @author Mirai
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.Shape.Tetrominoes;

//Klasa koja sadrzi celu logiku
public class Board extends JPanel implements ActionListener { //ActionListener koristimo za upravljanje oblicima

    //setujemo visinu i sirinu board-a
    final int BoardWidth = 10;
    final int BoardHeight = 22;
    
    //koristimo Timer da bi merili ciklus odvijenih dogadjaja
    Timer timer;
    boolean isFallingFinished = false; //proveravamo da li oblik pada
    boolean isStarted = false; //proveravamo da li je igra pocela
    boolean isPaused = false; //proveravamo da li je igra pauzirana
    int numLinesRemoved = 0; //brojimo broj linija koje smo do sada sklonili
    int curX = 0; //odredjuje X poziciju oblika koji pada
    int curY = 0; //odredjuje Y poziciju oblika koji pada
    JLabel statusbar; //status bar
    Shape curPiece; //oblik koji pada
    Tetrominoes[] board; //niz oblika na tabli



    public Board(Tetris parent) {
       //fokus je na tabli
       setFocusable(true); 
       curPiece = new Shape(); //kreiramo oblik na tabli
       timer = new Timer(400, this); //pozivamo timer i na svakih 400ms oblik ce se kretati ka dole
       timer.start(); //startujemo timer

       statusbar =  parent.getStatusBar(); //uzimamo status bar
       board = new Tetrominoes[BoardWidth * BoardHeight]; //ako je sirina table popunjena oblicima
       addKeyListener(new TAdapter());
       clearBoard();  //cistimo red na tabli
    }

    //metoda koja proverava da li je pad oblika zavrsen. Ako jeste, kreira se novi oblik, ako nije, spusta se za jedan red dole
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    //metode za kreiranje oblika na lokaciji na tabli
    int squareWidth() { return (int) getSize().getWidth() / BoardWidth; } //sirina kockice
    int squareHeight() { return (int) getSize().getHeight() / BoardHeight; } //duzina kockice
    Tetrominoes shapeAt(int x, int y) { return board[(y * BoardWidth) + x]; } //kockice

    //startujemo igru
    public void start()
    {
        if (isPaused)
            return;

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        clearBoard();

        newPiece();
        timer.start();
    }

    //metoda za pauziranje igre
    private void pause()
    {
        if (!isStarted)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            statusbar.setText("paused");
        } else {
            timer.start();
            statusbar.setText(String.valueOf(numLinesRemoved));
        }
        repaint();
    }

    //metoda za iscrtavanje oblika
    public void paint(Graphics g)
    { 
        //pozivamo Graphics instancu unutar paint metode, sa njom iscrtavamo
        super.paint(g);
        
        //crtamo sve oblike na tabli
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BoardHeight * squareHeight();

        //Prvo iscrtavamo sve oblike, ili ostale oblike koje su vec na dnu table.
        //Sve kockice kojima crtamo su u board nizu, uzimamo ih putem metode shapeAt()
        for (int i = 0; i < BoardHeight; ++i) {
            for (int j = 0; j < BoardWidth; ++j) {
                Tetrominoes shape = shapeAt(j, BoardHeight - i - 1);
                if (shape != Tetrominoes.NoShape)
                    drawSquare(g, 0 + j * squareWidth(),
                               boardTop + i * squareHeight(), shape);
            }
        }

        //iscrtavamo oblik koji pada
        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                           boardTop + (BoardHeight - y - 1) * squareHeight(),
                           curPiece.getShape());
            }
        }
    }

    //ako pretisnemo space, oblik ce pasti na dno.
    //Spustamo oblike jednu liniju na dole dok ne dostigne dno ili neki drugi oblik.
    private void dropDown()
    {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    //spustanje oblika za samo jednu liniju
    private void oneLineDown()
    {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    //ispunjava tablu sa NoShape oblikom koji koristimo za koliziju
    private void clearBoard()
    {
        for (int i = 0; i < BoardHeight * BoardWidth; ++i)
            board[i] = Tetrominoes.NoShape;
    }

    //metoda koja smesta padajuci oblik u board niz.
    //Tabla sadrzi sve kockice svih oblika i oblike koji su vec na dnu table.
    //Kada oblik zavrsi pad, proveravamo da li mozemo da uklonimo linije koje su popunjene sa dna table
    //to radimo metodom removeFullLines(), i zatim kreiramo novi oblik
    private void pieceDropped()
    {
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BoardWidth) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished)
            newPiece();
    }

    //Metoda koja kreira novi oblik. Oblik dobija random izgled.
    //Proveravamo koordinate, ako smo na vrhu i oblik ne moze da pada ili da se pomera, igra je gotova.
    //Timer se zaustavlja
    private void newPiece()
    {
        curPiece.setRandomShape();
        curX = BoardWidth / 2 + 1;
        curY = BoardHeight - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;
            statusbar.setText("Game Over");
        }
    }

    //metoda koja pomera oblike. Vraca false ukoliko je oblik dostigao ivice table ili drugi oblik na tabli.
    private boolean tryMove(Shape newPiece, int newX, int newY)
    {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BoardWidth || y < 0 || y >= BoardHeight)
                return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    //proveravamo da li ima punih redova izmedju svih redova na tabli
    //ako postoji barem jedna puna linija, uklanjamo je
    //nakon uklanjanja linije, povecavamo brojac
    //pomeramo sve linija iznad ka dole i tako unistavamo punu liniju
    private void removeFullLines()
    {
        int numFullLines = 0;

        for (int i = BoardHeight - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BoardWidth; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BoardHeight - 1; ++k) {
                    for (int j = 0; j < BoardWidth; ++j)
                         board[(k * BoardWidth) + j] = shapeAt(j, k + 1);
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusbar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoes.NoShape);
            repaint();
        }
     }

    //svaki tetris oblik se sastoji od 4 kocice i ovom metodom ih crtamo
    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape)
    {
        //oblici imaju 4 boje
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), 
            new Color(102, 204, 102), new Color(102, 102, 204), 
            new Color(204, 204, 102), new Color(204, 102, 204), 
            new Color(102, 204, 204), new Color(218, 170, 0)
        };


        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                         x + squareWidth() - 1, y + 1);
    }

    //kontrolisanje oblika pomocu tastature
    class TAdapter extends KeyAdapter {
         public void keyPressed(KeyEvent e) {

             //ukoliko igra nije startovana ili nemamo oblike, nista se ne desava
             if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape) {  
                 return;
             }

             //uzimamo input sa tastature
             int keycode = e.getKeyCode();

             //ako pretisnemo P, igra se pauzira
             if (keycode == 'p' || keycode == 'P') {
                 pause();
                 return;
             }

             //ako je igra pauzirana, ne mozemo da konrolisemo oblike
             if (isPaused)
                 return;

             //kontrola oblika
             switch (keycode) {
             case KeyEvent.VK_LEFT: //strelica u levo
                 tryMove(curPiece, curX - 1, curY);
                 break;
             case KeyEvent.VK_RIGHT: //strelica u desno
                 tryMove(curPiece, curX + 1, curY);
                 break;
             case KeyEvent.VK_DOWN: //rotiranje oblika u desno sa strelicom na dole
                 tryMove(curPiece.rotateRight(), curX, curY);
                 break;
             case KeyEvent.VK_UP: //rotiranje oblika u levo sa strelicom na gore
                 tryMove(curPiece.rotateLeft(), curX, curY);
                 break;
             case KeyEvent.VK_SPACE: //space spusta oblik na dno
                 dropDown();
                 break;
             case 'd': //slovo D spusta oblik za jednu liniju
                 oneLineDown();
                 break;
             case 'D':
                 oneLineDown();
                 break;
             }

         }
     }
}
