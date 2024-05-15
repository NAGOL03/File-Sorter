/*
 * Creator: NAGOL03
 */
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOrganizer {
    public Font guiFont = new Font("Dialog", Font.BOLD, 18);
    public Font listFont = new Font("Dialog", Font.PLAIN, 14);
    private String userDirectoryPath = System.getProperty("user.home");
    private ArrayList<File> sorterFiles = new ArrayList<>();
    private ArrayList<Sorter> sorters = new ArrayList<>();
    private File sortFolder;
    private Scanner scanner;
    private GUI gui;

    public FileOrganizer(String headless) {
        if (headless.equals("headless")) {
            System.out.println("Starting in headless mode");
        } else {
            System.out.println("Starting in GUI mode");
            gui = new GUI(this);
        }

        // Create sort Folder if it does not exist
        sortFolder = new File(userDirectoryPath + "\\Sorters");
        try {
            if (sortFolder.mkdir()) {
                System.out.println("Folder Created");
            } else {
                this.scanForSorters(sortFolder);
            }
        } catch (SecurityException e) {
            System.out.println("An error occured: " + e);
        }
        if (gui != null)
            gui.refreshSorterList();
        else run();

        // Called upon exit of program
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                scanner.close();
                System.out.println("Exiting...");
            }
        });
    }

    

    public void scanForSorters(File folder) {
        sorters.clear();
        // Scans to make sure files are Sorter files
        for (File file : folder.listFiles()) {
            try {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    if (scanner.nextLine().matches("SORTER:")) {
                        this.sorterFiles.add(file);
                        sorters.add(new Sorter(file));;
                        continue;
                    }
                }
            } catch (Exception e) {
                System.out.println("An error occured: " + e);
            }
        }
    }

    // Runs all sorterFiles
    public void run() {
        for (Sorter sorter : sorters) {
            sorter.sort();
        }
    }

    public ArrayList<File> getSorters() {
        return sorterFiles;
    }

    public class Sorter extends JCheckBox {
        public String tag;
        public String target;
        public Boolean active;
        public ArrayList<String> source = new ArrayList<>();
        public ArrayList<String> name = new ArrayList<>();
        public ArrayList<String> extension = new ArrayList<>();
        private Pattern p;
        private Matcher m;
        // private File sorterFile;

        public Sorter(File file) {
            super();
            // sorterFile = file;
            String fileContents = "";
            ArrayList<String> splitContents = new ArrayList<>();
            
            // Grab the file contents and store as string
            try {
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    fileContents += scanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println("An error occured: " + e);
            }
            
            // Remove "SORTER:" from text contents and splitContents contents for processing
            fileContents = fileContents.replace("SORTER:", "");
            for (String string : fileContents.split(";")) {
                splitContents.add(string);
            }

            processText(splitContents);
            super.setText(tag);
            super.setFont(listFont);
            super.setSelected(active);
            super.setBackground(Color.GRAY);
            super.setFocusable(false);
            super.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    setSelected(Sorter.this.active);
                }
            });
        }

        /**
         * Goes through split Contents and processes the text.
         * Either set sorter file data from sorter object (for modification),
         * or get the splitContents file data and plug it into Sorter object.
         * @param splitContents Split file contents
         * @param e set or get file data
         */
        public void processText(ArrayList<String> splitContents) {
            for (String string : splitContents) {
                p = Pattern.compile("\\s?([#]|[@])\\s?(\\w+)\\s?(.+?)\\s?$");
                m = p.matcher(string);
                while (m.find()) {
                    switch (m.group(1)) {
                        // # : Sorter setting
                        case "#" -> {
                            // Setting type
                            switch (m.group(2)) {
                                case "Tag" -> {
                                    tag = m.group(3);
                                }
                                case "Target" -> target = m.group(3);
                                case "Active" -> {
                                    if (m.group(3).equals("true")) {
                                        active = true;
                                    }
                                    else active = false;
                                }
                            }
                        }
                        // @ : Sorter variable
                        case "@" -> {
                            switch (m.group(2)) {
                                case "source" -> source.add(m.group(3));
                                case "name" -> name.add(m.group(3));
                                case "extension" -> extension.add(m.group(3));
                            }
                        }
                    }
                }
            }
        }

        public void sort() {
            File sDir;
            Path filePath;
            Path targetPath;
            /*
             * TO:DO
             * Only run files that have an Active value of true
             * Check all sources
             * Transfer all files with specified name
             * Transfer all files with specified extension
             */
            
            // For every source listed in source ArrayList
            for (String s : source) {
                if (this.active != true) break;

                // Get source directory
                sDir = new File(s);

                // For every file listed in the source directory
                for (File file : sDir.listFiles()) {
                    filePath = Paths.get(file.getAbsolutePath());

                    // For every name listed in name ArrayList
                    for (String n : name) {

                        // For every extension listed in extension ArrayList
                        for (String e : extension) {

                            // Determines if file matches name and extension
                            if (file.getName().matches(".*" + n + ".*" + e)) {
                                targetPath = Paths.get(target + "\\" + file.getName());

                                // Attempts to move file to target path using atomic move
                                try{
                                    Files.move(filePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
                                    System.out.println(file.getName() + " -> " + target);
                                } catch (IOException exception) {
                                    System.out.println("An error occured: " + exception);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class GUI implements ActionListener {
        private JButton createButton;
        private JButton runButton;
        private JButton editButton;
        private JButton deleteButton;
        private JButton refreshButton;

        private JPanel rightPanel;
        private JPanel leftPanel;
        private JPanel leftButtonPanel;
        private JLabel sorterLabel;
        private JScrollPane scrollPane;
        private JFrame frame;

        public GUI(FileOrganizer organizer) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            int screenWidth = (int) screenSize.getWidth();
            int screenHeight = (int) screenSize.getHeight();

            // Frame
            frame = new JFrame("File Organizer");
            frame.setSize(screenWidth / 4, screenHeight / 3);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new GridLayout());
            frame.setResizable(false);
            frame.setVisible(true);

            // Run Button
            runButton = new JButton();
            runButton.setFocusable(false);
            runButton.addActionListener(this);
            runButton.setText("Run");
            runButton.setFont(guiFont);

            // Refresh Button
            refreshButton = new JButton();
            refreshButton.setFocusable(false);
            refreshButton.addActionListener(this);
            refreshButton.setText("Refresh");
            refreshButton.setFont(guiFont);

            // Create Button
            createButton = new JButton("click");
            createButton.setFocusable(false);
            createButton.addActionListener(this);
            createButton.setText("Create");

            // Edit Button
            editButton = new JButton();
            editButton.setFocusable(false);
            editButton.addActionListener(this);
            editButton.setText("Edit");

            // Delete Button
            deleteButton = new JButton();
            deleteButton.setFocusable(false);
            deleteButton.addActionListener(this);
            deleteButton.setText("Delete");

            // Left Panel
            leftPanel = new JPanel();
            leftPanel.setLayout(new GridLayout(1, 1, 0, 0));
            frame.add(leftPanel);

            // Left Button Panel
            leftButtonPanel = new JPanel();
            leftButtonPanel.setBackground(Color.GRAY);
            leftButtonPanel.setBounds(leftPanel.getBounds());
            leftButtonPanel.setLayout(new BoxLayout(leftButtonPanel, BoxLayout.Y_AXIS));

            // Sort Label
            sorterLabel = new JLabel();
            sorterLabel.setText("Sorters");
            sorterLabel.setHorizontalAlignment(JLabel.CENTER);

            // Scroll Pane
            scrollPane = new JScrollPane(leftButtonPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setColumnHeaderView(sorterLabel);
            leftPanel.add(scrollPane);

            // Right Panel
            rightPanel = new JPanel();
            rightPanel.setBackground(Color.GRAY);
            rightPanel.add(runButton);
            rightPanel.add(refreshButton);
            // rightPanel.add(createButton);
            // rightPanel.add(editButton);
            // rightPanel.add(deleteButton);
            rightPanel.setLayout(new GridLayout(2, 1, 0, 10));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.add(rightPanel);

            frame.revalidate();
        }

        // Actions that occur on button press
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == this.runButton) {
                run();
            }
            
            if (e.getSource() == this.refreshButton) {
                refreshSorterList();
            }
            // if (e.getSource() == this.createButton) {
            //     System.out.println("Create");
            // }

            // if (e.getSource() == this.editButton) {
            //     System.out.println("edit");
            // }

            // if (e.getSource() == this.deleteButton) {
            //     System.out.println("delete");
            // }
        }

        private void refreshSorterList() {
            leftButtonPanel.removeAll();
            scanForSorters(sortFolder);
            for (Sorter sort : sorters) {
                leftButtonPanel.add(sort);
            }
            frame.revalidate(); // Sorters were not showing up unless I revalidated frame twice
        }

    }

    public static void main(String[] args) {
        if (args.length == 0) {
            new FileOrganizer("");
        } else {
            new FileOrganizer(args[0]);
        }
    }
}