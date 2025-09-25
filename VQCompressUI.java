import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

public class VQCompressUI extends JFrame {

    private JTextField filePathField;
    private JButton browseButton;
    private JComboBox<Integer> tileSizeBox;
    private JComboBox<String> qualityBox;
    private JButton compressButton;
    private JTextArea statusArea;
    private JLabel imageLabel;
    private JLabel decompressedImageLabel;
    private JProgressBar progressBar;

    private JLabel originalSizeLabel;
    private JLabel compressedSizeLabel;
    private JLabel ratioLabel;

    private File selectedFile;

    // A simple class to hold the results of the background task
    private static class CompressionResult {
        final BufferedImage image;
        final long originalSize;
        final long compressedSize;

        CompressionResult(BufferedImage image, long originalSize, long compressedSize) {
            this.image = image;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
        }
    }

    public VQCompressUI() {
        // 1. Use a modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fallback to system default if Nimbus is not available
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setTitle("VQ Image Compressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Panel for Settings ---
        JPanel settingsPanel = createSettingsPanel();

        // --- Center Panel for Image Previews ---
        JPanel imagePanel = createImagePreviewPanel();

        // --- Bottom Panel for Controls and Status ---
        JPanel bottomPanel = createBottomPanel();

        add(settingsPanel, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addListeners();
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("1. Compression Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Image File:"), gbc);

        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(filePathField, gbc);

        browseButton = new JButton("Browse...");
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(browseButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tile Size:"), gbc);

        tileSizeBox = new JComboBox<>(new Integer[]{2, 4, 8, 16});
        tileSizeBox.setSelectedItem(4);
        gbc.gridx = 1;
        panel.add(tileSizeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Quality:"), gbc);

        qualityBox = new JComboBox<>(new String[]{"High Quality", "Medium Quality", "Low Quality"});
        qualityBox.setSelectedIndex(1);
        gbc.gridx = 1;
        panel.add(qualityBox, gbc);

        return panel;
    }

    private JPanel createImagePreviewPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Image Preview"));

        imageLabel = createPlaceholderLabel("Original Image");
        decompressedImageLabel = createPlaceholderLabel("Compressed Result");

        panel.add(new JScrollPane(imageLabel));
        panel.add(new JScrollPane(decompressedImageLabel));

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        compressButton = new JButton("Compress Image");
        compressButton.setFont(compressButton.getFont().deriveFont(Font.BOLD, 14f));
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(150, 20));
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        actionPanel.add(compressButton);
        actionPanel.add(progressBar);

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(new TitledBorder("2. Compression Results"));
        originalSizeLabel = new JLabel("Original: -", SwingConstants.CENTER);
        compressedSizeLabel = new JLabel("Compressed: -", SwingConstants.CENTER);
        ratioLabel = new JLabel("Ratio: -", SwingConstants.CENTER);
        statsPanel.add(originalSizeLabel);
        statsPanel.add(compressedSizeLabel);
        statsPanel.add(ratioLabel);

        // Status Area
        statusArea = new JTextArea(4, 40);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Status Log"));

        JPanel controlAndStatsPanel = new JPanel(new BorderLayout(5, 5));
        controlAndStatsPanel.add(actionPanel, BorderLayout.NORTH);
        controlAndStatsPanel.add(statsPanel, BorderLayout.CENTER);

        panel.add(controlAndStatsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addListeners() {
        browseButton.addActionListener(e -> onBrowse());
        compressButton.addActionListener(e -> onCompress());
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            try {
                BufferedImage img = ImageIO.read(selectedFile);
                displayImage(imageLabel, img, "Original Image");
                showStatus("Loaded image: " + selectedFile.getName());
                resetStats();
            } catch (Exception ex) {
                showError("Could not read the selected image file.", "Image Load Error");
            }
        }
    }

    private void onCompress() {
        if (selectedFile == null) {
            showError("Please select an image file first.", "No Image Selected");
            return;
        }

        setBusy(true);

        // Run compression in the background to keep the UI responsive
        SwingWorker<CompressionResult, String> worker = new SwingWorker<>() {
            @Override
            protected CompressionResult doInBackground() throws Exception {
                // --- Get settings from UI ---
                int tileSize = (Integer) tileSizeBox.getSelectedItem();
                int qualityOption = qualityBox.getSelectedIndex(); // 0=High, 1=Medium, 2=Low
                String inputPath = selectedFile.getAbsolutePath();
                long originalSize = selectedFile.length();
                
                // --- 1. Choose a location to save the compressed file ---
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Compressed File");
                fileChooser.setSelectedFile(new File(selectedFile.getName() + ".vq")); // Suggest a name
                fileChooser.setFileFilter(new FileNameExtensionFilter("VQ Compressed File (*.vq)", "vq"));
                
                int userSelection = fileChooser.showSaveDialog(VQCompressUI.this);
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return null; // User cancelled
                }
                File compressedFile = fileChooser.getSelectedFile();
                
                // --- 2. Perform Compression ---
                publish("Compressing image...");
                vectorQuantizationCompress compressor = new vectorQuantizationCompress();
                compressor.tileSize = tileSize;
                switch (qualityOption) {
                    case 0: compressor.codeBookSize = 256; break; // High
                    case 1: compressor.codeBookSize = 128; break; // Medium
                    case 2: compressor.codeBookSize = 64; break;  // Low
                }
                compressor.loadImage(inputPath);
                compressor.initializeCodebook(); // Consider adding a more advanced algorithm here later (LBG)
                compressor.quantizeImage();
                compressor.saveCompressedFile(compressedFile.getAbsolutePath());
                long compressedSize = compressedFile.length();
                publish("Compression complete. Decompressing for preview...");

                // --- 3. Perform Decompression for preview ---
                // We create a temporary file for the decompressed image preview
                File tempDecompressedFile = File.createTempFile("decompressed_preview", ".jpg");
                tempDecompressedFile.deleteOnExit();

                vectorQuantizationDecompress decompressor = new vectorQuantizationDecompress();
                // This assumes the decompressor saves the file itself
                decompressor.loadCompressedData(compressedFile.getAbsolutePath(), tempDecompressedFile.getAbsolutePath());
                
                BufferedImage decompressedImg = ImageIO.read(tempDecompressedFile);
                publish("Decompression complete.");

                return new CompressionResult(decompressedImg, originalSize, compressedSize);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                // Update status area with messages from doInBackground
                for (String message : chunks) {
                    showStatus(message);
                }
            }

            @Override
            protected void done() {
                try {
                    CompressionResult result = get();
                    if (result != null) { // Not cancelled
                        displayImage(decompressedImageLabel, result.image, "Compressed Result");
                        updateStats(result.originalSize, result.compressedSize);
                        showStatus("Process finished successfully!");
                        
                        // --- 4. Ask user to save the final image ---
                        int choice = JOptionPane.showConfirmDialog(VQCompressUI.this, 
                            "Compression successful! Do you want to save the final decompressed image?",
                            "Save Image", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                             saveFinalImage(result.image);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("An error occurred during the process: " + e.getMessage(), "Error");
                    resetStats();
                } finally {
                    setBusy(false);
                }
            }
        };

        worker.execute();
    }

    private void saveFinalImage(BufferedImage image) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Decompressed Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG Image (*.jpg)", "jpg"));
        fileChooser.setSelectedFile(new File(selectedFile.getName() + "_decompressed.jpg"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "jpg", saveFile);
                showStatus("Decompressed image saved to: " + saveFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Image saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError("Failed to save the image: " + ex.getMessage(), "Save Error");
            }
        }
    }

    // --- Helper Methods ---

    private void setBusy(boolean busy) {
        compressButton.setEnabled(!busy);
        browseButton.setEnabled(!busy);
        progressBar.setIndeterminate(busy);
        progressBar.setString(busy ? "Processing..." : "Ready");
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private JLabel createPlaceholderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setBorder(BorderFactory.createDashedBorder(Color.GRAY));
        label.setFont(new Font("SansSerif", Font.ITALIC, 16));
        label.setPreferredSize(new Dimension(300, 300));
        return label;
    }

    private void displayImage(JLabel label, BufferedImage img, String defaultText) {
        if (img != null) {
            int w = label.getWidth();
            int h = label.getHeight();
            if (w == 0 || h == 0) { // If component not yet sized
                w = 350; h = 350;
            }
            Image scaledImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImg));
            label.setText(null);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        } else {
            label.setIcon(null);
            label.setText(defaultText);
            label.setBorder(BorderFactory.createDashedBorder(Color.GRAY));
        }
    }

    private void updateStats(long original, long compressed) {
        originalSizeLabel.setText("Original: " + formatFileSize(original));
        compressedSizeLabel.setText("Compressed: " + formatFileSize(compressed));
        if (compressed > 0) {
            double ratio = (double) original / compressed;
            ratioLabel.setText("Ratio: " + new DecimalFormat("0.00").format(ratio) + " : 1");
        }
    }

    private void resetStats() {
        originalSizeLabel.setText("Original: -");
        compressedSizeLabel.setText("Compressed: -");
        ratioLabel.setText("Ratio: -");
        displayImage(decompressedImageLabel, null, "Compressed Result");
    }
    
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    private void showStatus(String msg) {
        statusArea.append(msg + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        showStatus("ERROR: " + message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VQCompressUI().setVisible(true));
    }
}