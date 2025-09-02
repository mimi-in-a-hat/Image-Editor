package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Image {
    private final List<Pixel> rows;

    private int width;
    private int height;


    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    double energy(Pixel above, Pixel current, Pixel below) {
        Pixel left = current.left;
        Pixel right = current.right;

        Pixel aboveLeft = (above != null) ? above.left : null;
        Pixel aboveRight = (above != null) ? above.right : null;
        Pixel belowLeft = (below != null) ? below.left : null;
        Pixel belowRight = (below != null) ? below.right : null;

        if (aboveLeft == null || above == null || aboveRight == null ||
                left == null || right == null ||
                belowLeft == null || below == null || belowRight == null) {
            return current.brightness();
        }

        double horizEnergy = (aboveLeft.brightness() + 2 * left.brightness() + belowLeft.brightness()) -
                (aboveRight.brightness() + 2 * right.brightness() + belowRight.brightness());

        double vertEnergy = (aboveLeft.brightness() + 2 * above.brightness() + aboveRight.brightness()) -
                (belowLeft.brightness() + 2 * below.brightness() + belowRight.brightness());

        return Math.sqrt(horizEnergy * horizEnergy + vertEnergy * vertEnergy);
    }

    public void calculateEnergy() {
        if (height == 0) return;

        Pixel aboveRow = null;
        Pixel currentRow = rows.get(0);
        Pixel belowRow = (height > 1) ? rows.get(1) : null;

        for (int row = 0; row < height; row++) {
            Pixel above = aboveRow;
            Pixel current = currentRow;
            Pixel below = belowRow;

            while (current != null) {
                current.energy = energy(above, current, below);
                
                if (above != null) above = above.right;
                current = current.right;
                if (below != null) below = below.right;
            }

            aboveRow = currentRow;
            currentRow = belowRow;
            belowRow = (row < height - 2) ? rows.get(row + 2) : null;
        }
    }

    public List<Pixel> higlightSeam(List<Pixel> seam, Color color) {
        //TODO: highlight the seam, return previous values
        return null;
    }

    public void removeSeam(List<Pixel> seam) {
        //TODO: remove the provided seam
    }

    public void addSeam(List<Pixel> seam) {
        //TODO: Add the provided seam
    }

    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        //TODO: find the seam which maximizes total value extracted from the given pixel
        return null;
    }

    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/

    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing(pixel -> -pixel.energy);

        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return -pixel.energy;
            }
        });
        */
    }
}
