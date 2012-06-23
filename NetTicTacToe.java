import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.regex.*;

class MyButton extends JButton {
	private Integer row;
	private Integer column;
	private boolean pressed = false;			//уже нажата
	public MyButton() {}
	public MyButton(Integer row, Integer column) {
		this.row = row;
		this.column = column;
	} 

	public Integer getRow() {return row;}
	public Integer getColumn() {return column;}
	public void mark(String sign) {
		pressed = true;
		setText(sign);
	}
	public boolean isPressed() {return pressed;}
}

public class NetTicTacToe extends JPanel implements ActionListener, Runnable {
	static JFrame frame = new JFrame("NetTicTacToe");
	TextField rowsField = new TextField("3");
	TextField columnsField = new TextField("3");
	TextField ipAddress = new TextField("127.0.0.1");
	JPanel panelTicTacToeField = new JPanel();
	JButton changeSizeButton = new JButton("Change Size");
	int rows = 3;
	int columns = 3;
	MyButton[][] buttonArray;
	boolean myCourse;
	boolean hasNotStarted ;
	String sign;

	public NetTicTacToe() throws Exception {
		setLayout(new BorderLayout());
		createPanelTicTacToeField();
		add(ipAddress, BorderLayout.NORTH);
		add(panelTicTacToeField, BorderLayout.CENTER);
		JPanel panelChangeSize = new JPanel();
		panelChangeSize.setLayout(new GridLayout(1,3));
		panelChangeSize.add(rowsField); 
		panelChangeSize.add(columnsField);
		panelChangeSize.add(changeSizeButton);
		changeSizeButton.addActionListener(this);
		add(panelChangeSize, BorderLayout.SOUTH);
		Thread t = new Thread(this);
		t.start();
	}

	private void sendMessage(String message) {
		try {
			Socket s = new Socket(ipAddress.getText(), 8179);
			PrintWriter out1 = new PrintWriter(s.getOutputStream(), true );
			out1.println(message);
			s.close();
		} catch (Exception e ) {  
    		e.printStackTrace();
    	}
	}

	public void actionPerformed(ActionEvent event) {
		try {
			if (event.getSource() == changeSizeButton) {
				deletePanelTicTacToeField();
				rows = Integer.parseInt(rowsField.getText());
				columns = Integer.parseInt(columnsField.getText());
				createPanelTicTacToeField();
				sendMessage(rowsField.getText() + ":" + columnsField.getText() + ":size");
				return;
			}
			MyButton buttonPressed = (MyButton)event.getSource();
			if (hasNotStarted) {
				sign = "x";
				hasNotStarted = false;
			}
			if (!buttonPressed.isPressed() && myCourse) {
        		buttonPressed.mark(sign);
        		myCourse = false;
        		sendMessage(buttonPressed.getRow().toString() + ":" + buttonPressed.getColumn().toString() + ":" + sign);
			}
		} catch (Exception e ) {  
    		e.printStackTrace();
    	}

	}

	private void createPanelTicTacToeField() {
		buttonArray = new MyButton[rows][columns];
		panelTicTacToeField.setLayout(new GridLayout(rows,columns));
		for (Integer i = 0; i < rows; i++)
			for (Integer j = 0; j < columns; j++) {
				buttonArray[i][j] = new MyButton(i, j);
				panelTicTacToeField.add(buttonArray[i][j]);
				buttonArray[i][j].addActionListener(this);
			}
		buttonArray[0][0].setText(" ");
		myCourse = true;
		hasNotStarted = true;
	}

	private void deletePanelTicTacToeField() {
		for (Integer x = 0; x < rows; x++)
			for (Integer y = 0; y < columns; y++) {
				panelTicTacToeField.remove(buttonArray[x][y]);
			}
	}


	public void run() {
		Pattern pattern = Pattern.compile(":");
		try { 
			ServerSocket s = new ServerSocket(8179);
	        for (;;) {  
	            Socket incoming = s.accept( );
	            BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
	            String message = in.readLine();
	            String outsideAddress = incoming.getInetAddress().toString();
	            String insideAddres = incoming.getLocalAddress().toString();

	            String[] splitedMessage = pattern.split(message);
				int i = Integer.parseInt(splitedMessage[0]);
				int j = Integer.parseInt(splitedMessage[1]);
				String obtainedSign = splitedMessage[2];
		        ipAddress.setText(outsideAddress.substring(1));
		        myCourse = true;
	            if (!outsideAddress.equals(insideAddres)) {		//работаем с другим компъютером
	            	if (obtainedSign.equals("size")) {
						deletePanelTicTacToeField();	            		
	            		rows = i;
						columns = j;
						createPanelTicTacToeField();
					} else {
		            	if (hasNotStarted) {
							sign = "o";
							hasNotStarted = false;
						}
		            	buttonArray[i][j].mark(obtainedSign);     	
	            	}
	            } else {				//локальный компьютер
	            	if (sign.equals("x")) {
	            		sign = "o";
	            	} else {
	            		sign = "x";
	            	}
	            }
	            incoming.close();
        	} 		
	    } catch (Exception e ) {  
	    	e.printStackTrace();
	    }
	}

	public static void main(String[] args) throws Exception{
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setContentPane(new NetTicTacToe());
	    frame.setSize(350, 350);
	    frame.setVisible(true);
	}
}