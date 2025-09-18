import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class vectorQuantizationDecompress {
    private int tileSize;
    private int tilesPerRow;
    private int tilesPerColumn;
    private int[][] codeBook; // [codebookSize][tileSize*tileSize*3]
    private int[][] decompressedIndices;

    public void loadCompressedData(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Line 1: metadata -> imageWidth imageHeight tileSize codebookSize channels
            String[] header = reader.readLine().trim().split("\\s+");
            int imageWidth = Integer.parseInt(header[0]);
            int imageHeight = Integer.parseInt(header[1]);
            tileSize = Integer.parseInt(header[2]);
            int codebookSize = Integer.parseInt(header[3]);
            int channels = header.length > 4 ? Integer.parseInt(header[4]) : 1;
            // Compute tiles per row/column
            tilesPerRow = imageHeight / tileSize;
            tilesPerColumn = imageWidth / tileSize;
            // Initialize arrays
            decompressedIndices = new int[tilesPerRow][tilesPerColumn];
            codeBook = new int[codebookSize][tileSize * tileSize * channels];
            // Read tile indices (tilesPerRow lines with tilesPerColumn values)
            for (int i = 0; i < tilesPerRow; i++) {
                String[] indices = reader.readLine().trim().split("\\s+");
                for (int j = 0; j < tilesPerColumn; j++) {
                    decompressedIndices[i][j] = Integer.parseInt(indices[j]);
                }
            }
            // Read codebook vectors (codebookSize lines)
            for (int i = 0; i < codebookSize; i++) {
                String[] pixels = reader.readLine().trim().split("\\s+");
                for (int j = 0; j < tileSize * tileSize * channels; j++) {
                    codeBook[i][j] = Integer.parseInt(pixels[j]);
                }
            }
            System.out.println("Compressed data loaded successfully.");
            // Reconstruct and save the image
            BufferedImage img = reconstructImage(imageWidth, imageHeight, channels);
            saveImage(img, "DecompressedImage.jpg");
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage reconstructImage(int width, int height, int channels) {
        BufferedImage reconstructedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Thread[] threads = new Thread[tilesPerRow];
        for (int row = 0; row < tilesPerRow; row++) {
            final int r = row;
            threads[row] = new Thread(new Runnable() {
                public void run() {
                    for (int col = 0; col < tilesPerColumn; col++) {
                        int index = decompressedIndices[r][col];
                        int[] tile = codeBook[index];
                        for (int i = 0; i < tileSize; i++) {
                            for (int j = 0; j < tileSize; j++) {
                                int x = col * tileSize + j;
                                int y = r * tileSize + i;
                                if (x < width && y < height) {
                                    int idx = (i * tileSize + j) * channels;
                                    int rVal = tile[idx];
                                    int g = tile[idx + 1];
                                    int b = tile[idx + 2];
                                    int rgbVal = (rVal << 16) | (g << 8) | b;
                                    reconstructedImage.setRGB(x, y, rgbVal);
                                }
                            }
                        }
                    }
                }
            });
            threads[row].start();
        }
        // Wait for all threads to finish
        for (int row = 0; row < tilesPerRow; row++) {
            try {
                threads[row].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Image reconstructed successfully.");
        return reconstructedImage;
    }

    private void saveImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "jpg", new File(path));
            System.out.println("Decompressed image saved as JPEG to " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        vectorQuantizationDecompress vqd = new vectorQuantizationDecompress();
        vqd.loadCompressedData("Compressed.txt");
    }
}
