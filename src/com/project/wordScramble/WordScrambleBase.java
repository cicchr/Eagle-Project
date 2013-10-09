package com.project.wordScramble;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class WordScrambleBase {

	private ScrambleUI ui;
	private ArrayList<Scramble> scrambles;
	private FutureAction endGameTimer, hintTimer;
	private Random rand;
	private Scramble currentScramble;
	private boolean scrambleLoaded;
	
	private static final int HINT_WAIT_TIME = 5000;

	@SuppressWarnings("serial")
	public WordScrambleBase() {
		scrambles = new ArrayList<Scramble>();
		ui = new ScrambleUI() {

			@Override
			public void complete() {
				scrambleLoaded = false;
				ui.displayCorrect();
				showDescriptionDialog(currentScramble.getDescription(), new ImageIcon(currentScramble.getPicture()), currentScramble.getWord());
				nextScramble();
			}

		};
		
		hintTimer = new FutureAction() {
			
			@Override
			public void performAction() {
				ui.hint();
				hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
			}
			
			@Override
			public void actionCancelled() {
				
			}
		};
		
		ui.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
			}
		});
		
		rand = new Random();
		currentScramble = null;

		endGameTimer = new FutureAction() {

			@Override
			public void performAction() {
				ui.gameOver();
				Main.infoMsg("Word scramble game timed out");
			}

			@Override
			public void actionCancelled() {
				
			}
		};

		ui.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}
		});

	}

	public void playGame(ArrayList<Scramble> scrambles) {
		this.scrambles = scrambles;
		scrambleLoaded = false;
		nextScramble();
		endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
		hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
		synchronized (ui) {
			while (!ui.exit()) {
				try {
					ui.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		endGameTimer.cancel();
		hintTimer.cancel();
	}

	private void nextScramble() {
		if (scrambles.size() > 0) {
			if (scrambleLoaded)
				Main.errMsg("Scramble already loaded proceding anyway", false);
			int i = rand.nextInt(scrambles.size());
			currentScramble = scrambles.get(i);
			scrambleLoaded = true;
			scrambles.remove(i);
			ui.newScramble(currentScramble);
		} else {
			ui.gameOver();
		}
	}

	// public void playGame(ArrayList<Scramble> scrambles) {
	// this.scrambles = scrambles;
	// exit = false;
	// endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
	// boolean scrambleLoaded = false;
	// Scramble currentScramble = null;
	// ArrayList<Scramble> unusedScrambles = scrambles;
	// Random rand = new Random();
	// while (!exit) {
	// if (scrambleLoaded && currentScramble != null) {
	// ui.newScramble(currentScramble);
	// while (scrambleLoaded) {
	// if (ui.inputWasCaptured()) {
	// if (ui.getCurrentText().equalsIgnoreCase(currentScramble.getWord())) {
	// scrambleLoaded = false;
	// ui.displayCorrect();
	// ImageIcon ico = new ImageIcon();
	// ico.setImage(currentScramble.getPicture());
	// showDescriptionDialog(currentScramble.getDescription(), ico,
	// currentScramble.getWord());
	// } else if (ui.getCurrentText().length() ==
	// currentScramble.getWord().length())
	// ui.displayWrong();
	// } else if (ui.exit()) {
	// scrambleLoaded = false;
	// exit = true;
	// }
	// try {
	// Thread.sleep(125);
	// } catch (InterruptedException e) {
	// Main.saveStackTrace(e);
	// }
	// }
	// currentScramble = null;
	// } else if (scrambleLoaded && currentScramble == null) {
	// Main.errMsg("Word Scramble thinks Scramble is loaded but currentScramble is null",
	// false);
	// throw new NullPointerException("currentScramble is null");
	// } else if (unusedScrambles.size() != 0) {
	// int i = rand.nextInt(unusedScrambles.size());
	// currentScramble = unusedScrambles.get(i);
	// unusedScrambles.remove(i);
	// scrambleLoaded = true;
	// ui.newScramble(currentScramble);
	// } else {
	// exit = true;
	// }
	// }
	// ui.gameOver();
	// }

	public void showDescriptionDialog(String str, ImageIcon ico, String title) {
		if (ico.getIconHeight() > 500 || ico.getIconWidth() > 500) {
			int h = 1;
			int w = 1;
			if (ico.getIconHeight() < ico.getIconWidth()) {
				h = 500;
				w = (h * ico.getIconWidth()) / ico.getIconWidth();
			} else {
				w = 500;
				h = (w * ico.getIconHeight()) / ico.getIconWidth();
			}
			if (h == 1 || w == 1) {
				Main.errMsg("Unable to resize image - com.project.trivia.WordScrambleBase.showDescriptionDialog()", false);
			} else {
				Image img = ico.getImage().getScaledInstance(h, w, Image.SCALE_FAST);
				ico = new ImageIcon(img);
			}
		}
		ArrayList<String> descriptionLines = new ArrayList<String>();
		int currentCharNum = 0;
		int lastCharNum = 0;
		while (currentCharNum < str.length()) {
			lastCharNum = currentCharNum;
			currentCharNum += 90;
			if (currentCharNum > str.length()) {
				descriptionLines.add(str.substring(lastCharNum));
			} else {
				while (str.charAt(currentCharNum) != ' ')
					currentCharNum -= 1;
				descriptionLines.add(str.substring(lastCharNum, currentCharNum));
			}
		}
		String formatted = " ";
		for (int i = 0; i < descriptionLines.size(); i++) {
			formatted += descriptionLines.get(i) + (i + 1 != descriptionLines.size() ? "\n" : "");
		}
		UIManager.put("OptionPane.background", Color.BLACK);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		UIManager.put("Panel.background", Color.BLACK);
		UIManager.put("OptionPane.messageFont", new Font("Serif", Font.PLAIN, 30));
		JOptionPane.showOptionDialog(null, formatted, title, JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, ico, new String[] { "Next" }, "Next");
	}

	public static ArrayList<Scramble> loadScrambles() {
		ArrayList<Scramble> scrambles = new ArrayList<Scramble>();
		File dir = new File("gameFiles/scrambles/");
		if (!dir.isDirectory())
			dir.mkdirs();
		File[] files = dir.listFiles();
		for (File f : files) {
			String word = "";
			String text = "";
			String picLoc = "";
			String description = "";
			try {
				BufferedReader read = new BufferedReader(new FileReader(f));
				word = read.readLine();
				text = read.readLine();
				picLoc = read.readLine();
				description = read.readLine();
				read.close();
			} catch (FileNotFoundException e) {
				Main.errMsg("File not found after getting list of files - file: " + f.getName() + " (project.wordScramble.WordScrambleBase.loadScrambles())", true);
				Main.saveStackTrace(e);
			} catch (IOException e) {
				Main.errMsg("IOException unable to read file: " + f.getName() + " (project.wordScramble.WordScrambleBase.loadScrambles())", true);
				Main.saveStackTrace(e);
			}

			if (word.equals("") || text.equals("") || picLoc.equals("") || description.equals(""))
				Main.errMsg("Scramble File not read properly - file: " + f.getName(), true);
			else {
				Image pic = null;
				try {
					pic = ImageIO.read(new File(picLoc));
				} catch (IOException e) {
					Main.saveStackTrace(e);
				}
				scrambles.add(new Scramble(word, text, pic, description));
			}
		}
		return scrambles;
	}

	public static String scrambleWord(String word) {
		Main.infoMsg("scrambleWord called with word: " + word);
		char[] arr = word.toCharArray();
		ArrayList<Character> list = new ArrayList<Character>();
		for (char c : arr)
			list.add(c);
		Collections.shuffle(list);
		word = "";
		for (char c : list)
			word = word + c;
		Main.infoMsg("scrambleWord complete with scramble: " + word);
		return word;
	}

}
