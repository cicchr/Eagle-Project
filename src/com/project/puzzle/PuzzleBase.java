package com.project.puzzle;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class PuzzleBase {

	private ArrayList<Puzzle> puzzles;
	private ArrayList<BufferedImage> templates;
	private PuzzleUI ui;
	private Puzzle currentPuzzle;
	private boolean puzzleLoaded;
	private FutureAction endGameTimer;

	public PuzzleBase() {
		puzzles = new ArrayList<Puzzle>();
		templates = new ArrayList<BufferedImage>();
		ui = new PuzzleUI();
		puzzleLoaded = false;
		endGameTimer = new FutureAction() {

			@Override
			public void performAction() {
				ui.gameOver();
				Main.infoMsg("Puzzle Game Timed out");
			}

			@Override
			public void actionCancelled() {
				ui.gameOver();
				Main.errMsg("Puzzle end game timer canceled", false);
			}
		};
		
		endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
		
		ui.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent arg0) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}
		});
	}

	public void playGame() {
		loadPuzzles();
		loadTemplates();
		while (puzzles.size() > 0 && !ui.exit()) {
			if (puzzleLoaded) {
				Main.errMsg("nextPuzzle called while puzzle was still loaded", false);
			} else {
				Random rand = new Random();
				int size = puzzles.size();
				currentPuzzle = puzzles.get(rand.nextInt(size));
				puzzleLoaded = true;
				puzzles.remove(currentPuzzle);
				int i = rand.nextInt(templates.size());
				ui.nextPuzzle(currentPuzzle.getImage(), templates.get(i), currentPuzzle.getName());
				while (!ui.completed() && !ui.exit()) {
					try {
						Thread.sleep(125);
					} catch (InterruptedException e) {
						Main.saveStackTrace(e);
					}
				}
				if (!ui.exit()) {
					ui.displayCorrect();
					showDescriptionDialog(currentPuzzle.getDescription(), new ImageIcon(currentPuzzle.getImage()), currentPuzzle.getName());
				}
			}
		}
		ui.gameOver();
	}

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
		JOptionPane.showOptionDialog(null, formatted, title, JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, ico, new String[] { "Next" }, "Next");
	}

	public void loadTemplates() {
		File dir = new File("gameFiles/puzzleTemplates/");
		if (!dir.exists()) {
			dir.mkdirs();
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			if (!f.getName().endsWith(".txt")) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				templates.add(img);
			}
		}
	}

	public void loadPuzzles() {
		File dir = new File("gameFiles/puzzles/");
		if (!dir.exists()) {
			dir.mkdirs();
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			String path = null;
			String name = null;
			String description = null;
			try {
				BufferedReader read = new BufferedReader(new FileReader(f));
				path = read.readLine();
				description = read.readLine();
				name = read.readLine();
				read.close();
			} catch (FileNotFoundException e) {
				Main.saveStackTrace(e);
			} catch (IOException e) {
				Main.saveStackTrace(e);
			}
			if (path != null && name != null && description != null) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(new File(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (img != null)
					puzzles.add(new Puzzle(img, description, name));
				else {
					Main.errMsg("Unable to load image from file", false);
					try {
						throw new FileNotFoundException("Unable to load image");
					} catch (FileNotFoundException e) {
						Main.saveStackTrace(e);
					}
				}
			} else {
				Main.errMsg("The puzzle file " + f.getName() + "did not load correctly", false);
			}
		}
	}

}