package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageEditor {
  private Image image;
  private final Deque<List<Pixel>> undoSeams = new ArrayDeque();
  private List<Pixel> highlightedSeam = null;

  public ImageEditor() {
  }

  public void load(String filePath) throws IOException {
    File originalFile = new File(filePath);
    BufferedImage img = ImageIO.read(originalFile);
    this.image = new Image(img);
  }

  public void save(String filePath) throws IOException {
    BufferedImage img = this.image.toBufferedImage();
    ImageIO.write(img, "png", new File(filePath));
  }

  public void highlightGreenest() throws IOException {
    this.highlightedSeam = this.image.getGreenestSeam();
    this.undoSeams.push(this.deepCopySeam(this.highlightedSeam));
    this.image.higlightSeam(this.highlightedSeam, Color.BLUE);
  }

  public void highlightLowestEnergySeam() throws IOException {
    this.highlightedSeam = this.image.getLowestEnergySeam();
    this.undoSeams.push(this.deepCopySeam(this.highlightedSeam));
    this.image.higlightSeam(this.highlightedSeam, Color.RED);
  }

  public void removeHighlighted() throws IOException {
    if (this.highlightedSeam != null) {
      this.image.removeSeam(this.highlightedSeam);
      this.highlightedSeam = null;
    }

  }

  public void undo() throws IOException {
    if (!this.undoSeams.isEmpty()) {
      List<Pixel> seam = (List)this.undoSeams.pop();
      this.image.addSeam(seam);
      this.highlightedSeam = null;
    }
  }

  private List<Pixel> deepCopySeam(List<Pixel> seam) {
    List<Pixel> copy = new ArrayList(seam.size());

    for(Pixel p : seam) {
      Pixel newPixel = new Pixel(p.color);
      newPixel.left = p.left;
      newPixel.right = p.right;
      copy.add(newPixel);
    }

    return copy;
  }
}
