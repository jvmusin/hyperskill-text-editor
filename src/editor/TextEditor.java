package editor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextEditor extends JFrame {

    private final JTextField searchField;
    private final JTextArea textArea;
    private final JFileChooser fileChooser;
    private final JCheckBox useRegExCheckbox;
    private final JButton startSearchButton;
    private final JButton previousMatchButton;
    private final JButton nextMatchButton;
    private final JButton saveButton;
    private final JButton openButton;
    private final OccurrenceSelector occurrenceSelector = new OccurrenceSelector();

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        setTitle("Text Editor");
        setJMenuBar(createMenuBar());

        JPanel pane = createPane();
        add(pane, BorderLayout.CENTER);

        pane.add(openButton = createOpenButton(), createConstraints(0, 0, 0, 1));
        pane.add(saveButton = createSaveButton(), createConstraints(0, 0, 0, 1));
        pane.add(searchField = createSearchField(), createConstraints(0, 1, 0, 1));
        pane.add(startSearchButton = createStartSearchButton(), createConstraints(0, 0, 0, 1));
        pane.add(previousMatchButton = createPreviousMatchButton(), createConstraints(0, 0, 0, 1));
        pane.add(nextMatchButton = createNextMatchButton(), createConstraints(0, 0, 0, 1));
        pane.add(useRegExCheckbox = createUseRegExCheckbox(), createConstraints(0, 0, 0, 1));


        textArea = createTextArea();
        pane.add(createScrollPane(), createConstraints(1, 1, 1, 7));

        add(fileChooser = createFileChooser(), BorderLayout.PAGE_START);

        setVisible(true);
    }

    private static Insets createInsets(@SuppressWarnings("SameParameterValue") int size) {
        return new Insets(size, size, size, size);
    }

    private static GridBagConstraints createConstraints(int row, double weightRow, double weightCol, int width) {
        return new GridBagConstraints() {{
            gridy = row;
            weightx = weightRow;
            weighty = weightCol;
            gridwidth = width;
            fill = BOTH;
            insets = createInsets(5);
        }};
    }

    private static void addIcon(JButton button, String iconName) {
        try {
            File icon = new File("icons/" + iconName + "/" + iconName + "_16x16.png");
            button.setIcon(new ImageIcon(ImageIO.read(icon)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        fileChooser.setVisible(false);
        fileChooser.setSize(400, 300);
        return fileChooser;
    }

    private void openFile() {
        try {
            fileChooser.setVisible(true);
            fileChooser.showOpenDialog(null);
            File file = fileChooser.getSelectedFile();
            if (file != null) textArea.setText(Files.readString(Paths.get(file.toURI())));
        } catch (IOException ioException) {
            System.err.println("Unable to load file");
            ioException.printStackTrace();
            textArea.setText("");
        } finally {
            fileChooser.setSelectedFile(null);
        }
    }

    private void saveFile() {
        try {
            fileChooser.setVisible(true);
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            if (file != null) Files.write(Paths.get(file.toURI()), textArea.getText().getBytes());
        } catch (IOException e) {
            System.err.println("Unable to save file");
            e.printStackTrace();
        } finally {
            fileChooser.setSelectedFile(null);
        }
    }

    private JPanel createPane() {
        JPanel pane = new JPanel(new GridBagLayout());
        pane.setBorder(new EmptyBorder(createInsets(5)));
        return pane;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createSearchMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenuItem menuOpen = new JMenuItem("Open");
        menuOpen.setName("MenuOpen");
        menuOpen.addActionListener(e -> openButton.doClick());

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.addActionListener(e -> saveButton.doClick());

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.addActionListener(e -> dispose());

        JMenu menuFile = new JMenu("File");
        menuFile.setName("MenuFile");

        menuFile.add(menuOpen);
        menuFile.add(menuSave);
        menuFile.addSeparator();
        menuFile.add(menuExit);

        return menuFile;
    }

    private JMenu createSearchMenu() {
        JMenuItem menuStartSearch = new JMenuItem("Start search");
        menuStartSearch.setName("MenuStartSearch");
        menuStartSearch.addActionListener(e -> startSearchButton.doClick());

        JMenuItem menuPreviousMatch = new JMenuItem("Previous match");
        menuPreviousMatch.setName("MenuPreviousMatch");
        menuPreviousMatch.addActionListener(e -> previousMatchButton.doClick());

        JMenuItem menuNextMatch = new JMenuItem("Next match");
        menuNextMatch.setName("MenuNextMatch");
        menuNextMatch.addActionListener(e -> nextMatchButton.doClick());

        JMenuItem menuUseRegExp = new JMenuItem("User regular expressions");
        menuUseRegExp.setName("MenuUseRegExp");
        menuUseRegExp.addActionListener(e -> useRegExCheckbox.doClick());

        JMenu menuSearch = new JMenu("Search");
        menuSearch.setName("MenuSearch");

        menuSearch.add(menuStartSearch);
        menuSearch.add(menuPreviousMatch);
        menuSearch.add(menuNextMatch);
        menuSearch.add(menuUseRegExp);

        return menuSearch;
    }

    private JTextField createSearchField() {
        JTextField searchField = new JTextField();
        searchField.setName("SearchField");
        return searchField;
    }

    private JButton createStartSearchButton() {
        JButton button = new JButton();
        button.setName("StartSearchButton");
        button.addActionListener(e -> new TextSearcher(searchField.getText(), useRegExCheckbox.isSelected()).execute());
        addIcon(button, "Search");
        return button;
    }

    private JButton createPreviousMatchButton() {
        JButton button = new JButton();
        button.setName("PreviousMatchButton");
        button.addActionListener(e -> occurrenceSelector.selectPreviousMatch());
        addIcon(button, "Previous");
        return button;
    }

    private JButton createNextMatchButton() {
        JButton button = new JButton();
        button.setName("NextMatchButton");
        button.addActionListener(e -> occurrenceSelector.selectNextMatch());
        addIcon(button, "Next");
        return button;
    }

    private JCheckBox createUseRegExCheckbox() {
        JCheckBox cb = new JCheckBox("Use regex");
        cb.setName("UseRegExCheckbox");
        return cb;
    }

    private JButton createOpenButton() {
        JButton button = new JButton();
        button.setName("OpenButton");
        button.addActionListener(e -> openFile());
        addIcon(button, "Open");
        return button;
    }

    private JButton createSaveButton() {
        JButton saveButton = new JButton();
        saveButton.setName("SaveButton");
        saveButton.addActionListener(e -> saveFile());
        addIcon(saveButton, "Save");
        return saveButton;
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setName("TextArea");
        textArea.setSize(600, 400);
        return textArea;
    }

    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        return scrollPane;
    }

    private void select(Occurrence occurrence) {
        textArea.setCaretPosition(occurrence.getEnd());
        textArea.select(occurrence.getStart(), occurrence.getEnd());
        textArea.grabFocus();
    }

    private static class Occurrence {
        private final int start;
        private final int end;

        public Occurrence(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    private class OccurrenceSelector {
        private final List<Occurrence> occurrences = new ArrayList<>();
        private int lastShown;

        public OccurrenceSelector() {
            clear();
        }

        public void selectNextMatch() {
            if (occurrences.isEmpty()) return;

            lastShown = (lastShown + 1) % occurrences.size();
            select(occurrences.get(lastShown));
        }

        public void selectPreviousMatch() {
            if (occurrences.isEmpty()) return;

            lastShown = (lastShown - 1 + occurrences.size()) % occurrences.size();
            select(occurrences.get(lastShown));
        }

        public void init(List<Occurrence> occurrences) {
            this.occurrences.clear();
            this.occurrences.addAll(occurrences);
            lastShown = -1;
            selectNextMatch();
        }

        public void clear() {
            init(Collections.emptyList());
        }
    }

    private class TextSearcher extends SwingWorker<List<Occurrence>, Void> {

        private final String pattern;
        private final boolean isRegExp;

        public TextSearcher(String pattern, boolean isRegExp) {
            this.pattern = pattern;
            this.isRegExp = isRegExp;
        }

        @Override
        protected List<Occurrence> doInBackground() {
            String text = textArea.getText();
            Pattern p = Pattern.compile(pattern, isRegExp ? 0 : Pattern.LITERAL);
            Matcher matcher = p.matcher(text);

            return matcher.results()
                    .map(e -> new Occurrence(e.start(), e.end()))
                    .collect(Collectors.toList());
        }

        @Override
        protected void done() {
            try {
                occurrenceSelector.init(get());
            } catch (Exception e) {
                occurrenceSelector.clear();
                System.err.println("Unable to find a text");
                e.printStackTrace();
            }
        }
    }
}
