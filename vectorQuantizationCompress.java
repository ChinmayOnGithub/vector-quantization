import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class vectorQuantizationCompress {

    // Define the tile size (each tile is tileSize x tileSize pixels)
    int tileSize = 8; // default, will be set by user
    int tilesPerRow;
    int tilesPerColumn;

    // 3D array to store image tiles (now for color:
    // [row][col][tileSize*tileSize*3])
    private int[][][] tileGrid;

    // Codebook and compressed image
    private int[][] codeBook;
    private int[][] compressedImage;

    // Quality and codebook settings
    int qualityOption;
    int codeBookSize;
    int imageWidth, imageHeight;

    // Allow user to choose compression quality and tile size
    public void chooseQualityAndTileSize(Scanner sc) {
        System.out.print("Enter tile size (e.g., 4, 6, 8): ");
        tileSize = sc.nextInt();
        System.out.println("Select compression quality option: ");
        System.out.println("1. Low Compression (High Quality)");
        System.out.println("2. Medium Compression (Medium Quality)");
        System.out.println("3. High Compression (Low Quality)");
        qualityOption = sc.nextInt();

        // Determine codebook size based on quality
        switch (qualityOption) {
            case 1:
                codeBookSize = 256;
                break;
            case 2:
                codeBookSize = 128;
                break;
            case 3:
                codeBookSize = 16;
                break;
            default:
                System.out.println("Invalid option; defaulting to Medium Compression.");
                codeBookSize = 128;
                break;
        }
    }

    // Load image and split into 8x8 color tiles
    public void loadImage(String imgPath) {
        try {
            BufferedImage image = ImageIO.read(new File(imgPath));
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            tilesPerRow = imageHeight / tileSize;
            tilesPerColumn = imageWidth / tileSize;

            tileGrid = new int[tilesPerRow][tilesPerColumn][tileSize * tileSize * 3];
            compressedImage = new int[tilesPerRow][tilesPerColumn];

            for (int row = 0; row < tilesPerRow; row++) {
                for (int col = 0; col < tilesPerColumn; col++) {
                    for (int i = 0; i < tileSize; i++) {
                        for (int j = 0; j < tileSize; j++) {
                            int pixel = image.getRGB(col * tileSize + j, row * tileSize + i);
                            int r = (pixel >> 16) & 0xFF;
                            int g = (pixel >> 8) & 0xFF;
                            int b = pixel & 0xFF;
                            int idx = (i * tileSize + j) * 3;
                            tileGrid[row][col][idx] = r;
                            tileGrid[row][col][idx + 1] = g;
                            tileGrid[row][col][idx + 2] = b;
                        }
                    }
                }
            }
            System.out.println("Image loaded and split into " + tilesPerRow + " x " + tilesPerColumn + " color tiles.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Improved codebook initialization: sample tiles from across the entire image
    // (no change needed)
    public void initializeCodebook() {
        codeBook = new int[codeBookSize][tileSize * tileSize * 3];
        int totalTiles = tilesPerRow * tilesPerColumn;
        int step = Math.max(1, totalTiles / codeBookSize);
        int count = 0;

        for (int row = 0; row < tilesPerRow; row++) {
            for (int col = 0; col < tilesPerColumn; col++) {
                int flatIndex = row * tilesPerColumn + col;
                if (flatIndex % step == 0 && count < codeBookSize) {
                    codeBook[count++] = tileGrid[row][col].clone();
                }
                if (count >= codeBookSize)
                    break;
            }
            if (count >= codeBookSize)
                break;
        }

        // Fill any remaining codebook vectors if image had fewer tiles
        while (count < codeBookSize) {
            codeBook[count++] = new int[tileSize * tileSize * 3]; // blank tiles
        }

        System.out.println("Codebook initialized with " + count + " vectors.");
    }

    // Quantize the image using the closest codebook vector (multithreaded per row)
    public void quantizeImage() {
        Thread[] threads = new Thread[tilesPerRow];
        for (int row = 0; row < tilesPerRow; row++) {
            final int r = row;
            threads[row] = new Thread(new Runnable() {
                public void run() {
                    for (int col = 0; col < tilesPerColumn; col++) {
                        compressedImage[r][col] = findClosestCodebookVector(tileGrid[r][col]);
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
    }

    // Find the closest vector in the codebook using Euclidean distance (for color)
    public int findClosestCodebookVector(int[] tile) {
        long minDistance = Integer.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < codeBookSize; i++) {
            long distance = 0;
            for (int j = 0; j < tileSize * tileSize * 3; j++) {
                int diff = tile[j] - codeBook[i][j];
                distance += (long) diff * diff;
            }
            if (distance < minDistance) {
                minDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    // Save compressed image and codebook to file (update codebook write/read for
    // color)
    public void saveCompressedFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write metadata: image width, height, tile size, codebook size, color flag
            bw.write(imageWidth + " " + imageHeight + " " + tileSize + " " + codeBookSize + " 3");
            bw.newLine();
            // Write compressed image (tile indices)
            for (int i = 0; i < tilesPerRow; i++) {
                for (int j = 0; j < tilesPerColumn; j++) {
                    bw.write(compressedImage[i][j] + " ");
                }
                bw.newLine();
            }
            // Write codebook vectors (color)
            for (int i = 0; i < codeBookSize; i++) {
                for (int j = 0; j < tileSize * tileSize * 3; j++) {
                    bw.write(codeBook[i][j] + " ");
                }
                bw.newLine();
            }
            System.out.println("Compression successful. Data saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method
    public static void main(String[] args) {
        vectorQuantizationCompress compressor = new vectorQuantizationCompress();

        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Give file path : ");
            String filePath = sc.nextLine();
            compressor.chooseQualityAndTileSize(sc);
            compressor.loadImage(filePath);
            compressor.initializeCodebook();
            compressor.quantizeImage();
            compressor.saveCompressedFile("Compressed.txt");
        }
    }
}
