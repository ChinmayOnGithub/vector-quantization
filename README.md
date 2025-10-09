# VQ Compress

## Vector Quantization Image Compression & Decompression

---

### 🖼️ Visual Comparison: Original, Compressed Images

<table>
  <tr>
    <td align="center">
      <b>Original Image</b><br>
      <img src="morskie-oko-tatry.jpg" alt="Original" width="200"/><br>
      <sub>Chinmay.png<br>21 MB</sub>
    </td>
    <td align="center">
      <b>Decompressed Image</b><br>
      <img src="CompressedImage.jpg" alt="Decompressed" width="200"/><br>
      <sub>CompressedImage.jpg<br>4.1 MB</sub>
    </td>
  </tr>
</table>


---

### 🏗️ How Vector Quantization Works: Step-by-Step Example

Suppose you have a small 4x4 grayscale image:

```
Original Image (pixel values):
[
  [52, 55, 61, 59],
  [49, 52, 60, 60],
  [48, 50, 62, 61],
  [47, 49, 63, 62]
]
```

**Step-by-Step Process:**

- **Step 1: Divide into Tiles (2x2)**
  - Tile 0: [52, 55, 49, 52]
  - Tile 1: [61, 59, 60, 60]
  - Tile 2: [48, 50, 47, 49]
  - Tile 3: [62, 61, 63, 62]

- **Step 2: Build Codebook (Clustering)**
  - Suppose we want a codebook of size 2 (for illustration):
    - Codebook[0]: [50, 52, 48, 50]   (average of Tile 0 and Tile 2)
    - Codebook[1]: [62, 60, 62, 61]   (average of Tile 1 and Tile 3)

- **Step 3: Map Each Tile to Closest Codebook Vector**
  - Tile 0 → Codebook[0] (index 0)
  - Tile 1 → Codebook[1] (index 1)
  - Tile 2 → Codebook[0] (index 0)
  - Tile 3 → Codebook[1] (index 1)

- **Step 4: Store Compressed Data**
  - Metadata: image size, tile size, codebook size
  - Indices: [0, 1, 0, 1]
  - Codebook: [ [50, 52, 48, 50], [62, 60, 62, 61] ]

- **Step 5: Decompression (Reconstruction)**
  - For each tile index, replace with the corresponding codebook vector:

```
Reconstructed Image:
[
  [50, 52, 62, 60],
  [48, 50, 62, 61],
  [50, 52, 62, 60],
  [48, 50, 62, 61]
]
```

*Note: The reconstructed image is an approximation of the original, with some loss depending on codebook size and tile size.*

---

### 📦 Compression Results: File Size Comparison

| File                              | Size      | Description                       |
|-----------------------------------|-----------|-----------------------------------|
|morskie-oko-tatry.jpg (Original)   | 21 MB     | Original input image              |
| Compressed.txt                    | 1.2 MB    | Compressed representation         |
| CompressedImage.jpg             | 4.1 MB      | Output after Compression          |

*Note: The compressed file may be larger than the original for small/simple images or certain settings. For large/color images, compression is more effective.*

---

### 📝 Overview

This project demonstrates how to compress and decompress a grayscale image using Vector Quantization (VQ). The main goal is to reduce image size by converting small blocks (tiles) of pixels into codebook indices and reconstructing the image using that codebook.



### 🔧 Prerequisites

- Java 8 or later installed
- Grayscale image file
- Basic terminal or IDE like IntelliJ, VS Code

---

### 🔁 Flow of Execution

#### 1. 📦 Compression (`vectorQuantizationCompress.java`)
- **Input:** OriginalImage.png
- **Process:**
  - Load image
  - Divide into tiles (e.g., 8×8 pixels)
  - Create a codebook by grouping similar tiles
  - Replace each tile with its codebook index
  - Store metadata, tile indices, and codebook vectors in Compressed.txt
- **Output:** Compressed.txt

#### 2. 🧩 Decompression (`vectorQuantizationDecompress.java`)
- **Input:** Compressed.txt
- **Process:**
  - Read metadata (image dimensions, tile size, codebook size)
  - Reconstruct tiles using codebook
  - Stitch tiles into a full image
  - Save final output as DecompressedImage.png
- **Output:** DecompressedImage.png

---

### ▶️ How to Run

- **Compile Java files:**
  ```bash
  javac VQCompressUI.java
  ```
- **Run compression:**
  ```bash
  java VQCompressUI
  ```
  Produces Compressed.txt
  Produces DecompressedImage.png

---

### 🧪 Example Compressed.txt Format

```
300 192 8 256           # imageWidth imageHeight tileSize codebookSize
0 1 1 2 3 4 5...        # tile indices (tilesPerRow × tilesPerCol)
...                     # more index rows
12 45 67 ...            # codebook vector 0 (tileSize × tileSize values)
...                     # codebook vector 1
...
```

---



### 💻 Project Demo

- **Original Image:**

  ![Original](morskie-oko-tatry.jpg)

- **Running compression code:**
   <img width="1365" height="738" alt="image" src="https://github.com/user-attachments/assets/f665c33b-4726-4f2e-8fff-38428cf37978" />


- **Compressed file (snippet):**
  ```bash
  300 192 8 16
  0 1 2 3 4 5 6 11 7 8 10 11 11 13 10 11 13 13 11 2 1 1 0 8 10 3 4 1 1 1 1 14 15 15 15 8 3 
  ...
  ```
- **Compressed image:**

  ![Decompressed Image](DecompressedImage.jpg)

---
