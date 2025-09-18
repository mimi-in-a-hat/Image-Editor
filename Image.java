package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Image {
  private final List<Pixel> rows;
  private int width;
  private int height;

  public Image(BufferedImage img) {
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.rows = new ArrayList();
    Pixel current = null;

    for(int row = 0; row < this.height; ++row) {
      for(int col = 0; col < this.width; ++col) {
        Pixel pixel = new Pixel(img.getRGB(col, row));
        if (col == 0) {
          this.rows.add(pixel);
        } else {
          current.right = pixel;
          pixel.left = current;
        }

        current = pixel;
      }
    }

  }

  public BufferedImage toBufferedImage() {
    BufferedImage image = new BufferedImage(this.width, this.height, 1);

    for(int row = 0; row < this.height; ++row) {
      Pixel pixel = (Pixel)this.rows.get(row);

      for(int col = 0; pixel != null; pixel = pixel.right) {
        image.setRGB(col++, row, pixel.color.getRGB());
      }
    }

    return image;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  double energy(Pixel above, Pixel current, Pixel below) {
    Pixel left = current.left;
    Pixel right = current.right;
    Pixel aboveLeft = above != null ? above.left : null;
    Pixel aboveRight = above != null ? above.right : null;
    Pixel belowLeft = below != null ? below.left : null;
    Pixel belowRight = below != null ? below.right : null;
    if (aboveLeft != null && above != null && aboveRight != null && left != null && right != null && belowLeft != null && below != null && belowRight != null) {
      double horizEnergy = aboveLeft.brightness() + (double)2.0F * left.brightness() + belowLeft.brightness() - (aboveRight.brightness() + (double)2.0F * right.brightness() + belowRight.brightness());
      double vertEnergy = aboveLeft.brightness() + (double)2.0F * above.brightness() + aboveRight.brightness() - (belowLeft.brightness() + (double)2.0F * below.brightness() + belowRight.brightness());
      return Math.sqrt(horizEnergy * horizEnergy + vertEnergy * vertEnergy);
    } else {
      return current.brightness();
    }
  }

  public void calculateEnergy() {
    if (this.height != 0) {
      Pixel aboveRow = null;
      Pixel currentRow = (Pixel)this.rows.get(0);
      Pixel belowRow = this.height > 1 ? (Pixel)this.rows.get(1) : null;

      for(int row = 0; row < this.height; ++row) {
        Pixel above = aboveRow;
        Pixel current = currentRow;
        Pixel below = belowRow;

        while(current != null) {
          current.energy = this.energy(above, current, below);
          if (above != null) {
            above = above.right;
          }

          current = current.right;
          if (below != null) {
            below = below.right;
          }
        }

        aboveRow = currentRow;
        currentRow = belowRow;
        belowRow = row < this.height - 2 ? (Pixel)this.rows.get(row + 2) : null;
      }

    }
  }

  public List<Pixel> higlightSeam(List<Pixel> seam, Color color) {
    List<Pixel> modifiedPixels = new ArrayList();

    for(Pixel pixel : seam) {
      Pixel modified = new Pixel(color);
      modified.left = pixel.left;
      modified.right = pixel.right;
      modifiedPixels.add(modified);
      pixel.setColor(color);
    }

    return seam;
  }

  public void removeSeam(List<Pixel> seam) {
    for(int row = 0; row < this.height; ++row) {
      Pixel pixel = (Pixel)seam.get(row);
      if (pixel.left != null) {
        pixel.left.right = pixel.right;
      }

      if (pixel.right != null) {
        pixel.right.left = pixel.left;
      }

      if (this.rows.get(row) == pixel) {
        this.rows.set(row, pixel.right);
      }
    }

    --this.width;
  }

  public void addSeam(List<Pixel> seam) {
    for(int row = 0; row < this.height; ++row) {
      Pixel newPixel = (Pixel)seam.get(row);
      Pixel left = newPixel.left;
      Pixel right = newPixel.right;
      if (left != null) {
        left.right = newPixel;
      }

      if (right != null) {
        right.left = newPixel;
      }

      if (left == null) {
        this.rows.set(row, newPixel);
      }
    }

    ++this.width;
  }

  private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
    double[] previousValues = new double[this.width];
    double[] currentValue = new double[this.width];
    List<List<Pixel>> previousSeams = new ArrayList();
    List<List<Pixel>> currentSeams = new ArrayList();
    Pixel currentPixel = (Pixel)this.rows.getFirst();

    for(int col = 0; currentPixel != null; ++col) {
      previousValues[col] = (Double)valueGetter.apply(currentPixel);
      previousSeams.add(List.of(currentPixel));
      currentPixel = currentPixel.right;
    }

    for(int row = 1; row < this.height; ++row) {
      currentPixel = (Pixel)this.rows.get(row);

      for(int var13 = 0; currentPixel != null; currentPixel = currentPixel.right) {
        double max = previousValues[var13];
        int ref = var13;
        if (var13 > 0 && previousValues[var13 - 1] > max) {
          max = previousValues[var13 - 1];
          ref = var13 - 1;
        }

        if (var13 < this.width - 1 && previousValues[var13 + 1] > max) {
          max = previousValues[var13 + 1];
          ref = var13 + 1;
        }

        currentValue[var13] = max + (Double)valueGetter.apply(currentPixel);
        currentSeams.add(this.concat(currentPixel, (List)previousSeams.get(ref)));
        ++var13;
      }

      previousValues = currentValue;
      currentValue = new double[this.width];
      previousSeams = currentSeams;
      currentSeams = new ArrayList();
    }

    double maxValue = previousValues[0];
    int maxValueIndex = 0;

    for(int i = 0; i < this.width; ++i) {
      if (previousValues[i] > maxValue) {
        maxValue = previousValues[i];
        maxValueIndex = i;
      }
    }

    List<Pixel> finalSeam = (List)previousSeams.get(maxValueIndex);
    Collections.reverse(finalSeam);
    return finalSeam;
  }

  private List<Pixel> concat(Pixel currentPixel, List<Pixel> previousSeam) {
    List<Pixel> newSeam = new ArrayList(previousSeam.size() + 1);
    newSeam.add(currentPixel);
    newSeam.addAll(previousSeam);
    return newSeam;
  }

  public List<Pixel> getGreenestSeam() {
    return this.getSeamMaximizing(Pixel::getGreen);
  }

  public List<Pixel> getLowestEnergySeam() {
    this.calculateEnergy();
    return this.getSeamMaximizing((pixel) -> -pixel.energy);
  }
}
