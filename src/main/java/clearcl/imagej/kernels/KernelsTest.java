package clearcl.imagej.kernels;

import clearcl.ClearCL;
import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.*;
import ij.plugin.filter.MaximumFinder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imglib2.realtransform.Scale3D;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KernelsTest {

    ImagePlus testImp1;
    ImagePlus testImp2;
    ImagePlus mask;
    ClearCLIJ clij;

    @Before
    public void initTest() {
        testImp1 = NewImage.createImage("", 100, 100, 100,16, NewImage.FILL_BLACK);
        testImp2 = NewImage.createImage("", 100, 100, 100,16, NewImage.FILL_BLACK);
        mask = NewImage.createImage("", 100, 100, 100,16, NewImage.FILL_BLACK);

        for (int z = 0; z < 5; z++) {
            testImp1.setZ(z + 1);
            ImageProcessor ip1 = testImp1.getProcessor();
            ip1.set(5, 5, 1);
            ip1.set(6, 6, 1);
            ip1.set(7, 7, 1);

            testImp2.setZ(z + 1);
            ImageProcessor ip2 = testImp2.getProcessor();
            ip2.set(7, 5, 2);
            ip2.set(6, 6, 2);
            ip2.set(5, 7, 2);

            if (z < 3) {
                mask.setZ(z + 3);
                ImageProcessor ip3 = mask.getProcessor();
                ip3.set(2, 2, 1);
                ip3.set(2, 3, 1);
                ip3.set(2, 4, 1);
                ip3.set(3, 2, 1);
                ip3.set(3, 3, 1);
                ip3.set(3, 4, 1);
                ip3.set(4, 2, 1);
                ip3.set(4, 3, 1);
                ip3.set(4, 4, 1);
            }



        }


        clij = ClearCLIJ.getInstance();
    }

    boolean compareImages(ImagePlus a, ImagePlus b) {
        if (a.getWidth() != b.getWidth() ||
            a.getHeight() != b.getHeight() ||
            a.getNChannels() != b.getNChannels() ||
            a.getNFrames() != b.getNFrames() ||
            a.getNSlices() != b.getNSlices()) {
            System.out.println("sizes different");
            System.out.println("w " + a.getWidth() + " != " + b.getWidth());
            System.out.println("h " + a.getHeight() + " != " + b.getHeight());
            System.out.println("c " + a.getNChannels() + " != " + b.getNChannels());
            System.out.println("f " + a.getNFrames() + " != " + b.getNFrames());
            System.out.println("s " + a.getNSlices() + " != " + b.getNSlices());
            return false;
        }

        for (int c = 0; c < a.getNChannels(); c++) {
            a.setC(c + 1);
            b.setC(c + 1);
            for (int t = 0; t < a.getNFrames(); t++) {
                a.setT(t + 1);
                b.setT(t + 1);
                for (int z = 0; z < a.getNSlices(); z++) {
                    a.setZ(z + 1);
                    b.setZ(z + 1);
                    ImageProcessor aIP = a.getProcessor();
                    ImageProcessor bIP = b.getProcessor();
                    for (int x = 0; x < a.getWidth(); x++) {
                        for (int y = 0; y < a.getHeight(); y++) {
                            if (aIP.getPixelValue(x, y ) != bIP.getPixelValue(x, y)) {
                                System.out.println("pixels different " + aIP.getPixelValue(x, y ) + " != " + bIP.getPixelValue(x, y));
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Test
    public void addPixelwise() {
        ImageCalculator ic = new ImageCalculator();
        ImagePlus sumImp = ic.run("Add create stack", testImp1, testImp2);

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage src1 = clij.converter(testImp2).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.addPixelwise(clij, src, src1, dst);
        ImagePlus sumImpFromCL = clij.converter(dst).getImagePlus();

        //sumImp.show();
        //sumImpFromCL.show();
        assertTrue(compareImages(sumImp, sumImpFromCL));
    }

    @Test
    public void addScalar() {
        ImagePlus added = new Duplicator().run(testImp1);
        IJ.run(added, "Add...", "value=1 stack");

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.addScalar(clij, src, dst, 1);
        ImagePlus addedFromCL = clij.converter(dst).getImagePlus();

        assertTrue(compareImages(added, addedFromCL));

    }

    @Test
    public void addWeightedPixelwise() {
        float factor1 = 3f;
        float factor2 = 2;

        ImagePlus testImp1copy = new Duplicator().run(testImp1);
        ImagePlus testImp2copy = new Duplicator().run(testImp2);
        IJ.run(testImp1copy, "Multiply...", "value=" + factor1 + " stack");
        IJ.run(testImp2copy, "Multiply...", "value=" + factor2 + " stack");

        ImageCalculator ic = new ImageCalculator();
        ImagePlus sumImp = ic.run("Add create stack", testImp1copy, testImp2copy);

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage src1 = clij.converter(testImp2).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.addWeightedPixelwise(clij, src, src1, dst, factor1, factor2);
        ImagePlus sumImpFromCL = clij.converter(dst).getImagePlus();

        //sumImp.show();
        //sumImpFromCL.show();
        assertTrue(compareImages(sumImp, sumImpFromCL));
    }

    @Test
    public void argMaxProjection() {
      ImagePlus maxProjection = NewImage.createShortImage("", testImp1.getWidth(), testImp2.getHeight(), 1, NewImage.FILL_BLACK);
      ImageProcessor ipMax = maxProjection.getProcessor();
      ImagePlus argMaxProjection = NewImage.createShortImage("", testImp1.getWidth(), testImp2.getHeight(), 1, NewImage.FILL_BLACK);
      ImageProcessor ipArgMax = maxProjection.getProcessor();

      ImagePlus testImp1copy = new Duplicator().run(testImp1);
      for (int z = 0; z < testImp1copy.getNSlices(); z++) {
        testImp1copy.setZ(z + 1);
        ImageProcessor ip = testImp1copy.getProcessor();
        for (int x = 0; x < testImp1copy.getWidth(); x++) {
          for (int y = 0; y < testImp1copy.getHeight(); y++)
          {
            float value = ip.getf(x, y);
            if (value > ipMax.getf(x, y)) {
              ipMax.setf(x, y, value);
              ipArgMax.setf(x, y, z);
            }
          }
        }
      }

      ClearCLImage src = clij.converter(testImp1).getClearCLImage();
      ClearCLImage dst = clij.createCLImage(new long[]{src.getWidth(), src.getHeight()}, src.getChannelDataType());
      ClearCLImage dst_arg = clij.createCLImage(new long[]{src.getWidth(), src.getHeight()}, src.getChannelDataType());

      Kernels.argMaxProjection(clij, src, dst, dst_arg);

      ImagePlus maxProjectionCL = clij.converter(dst).getImagePlus();
      ImagePlus argMaxProjectionCL = clij.converter(dst_arg).getImagePlus();

      assertTrue(compareImages(maxProjection, maxProjectionCL));
      assertTrue(compareImages(argMaxProjection, argMaxProjectionCL));
    }

    @Test
    public void blur() {

        ImagePlus gauss = new Duplicator().run(testImp1);

        GaussianBlur3D.blur(gauss, 2, 2, 2);

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.blur(clij, src, dst, 6,6,6, 2,2,2);
        ImagePlus gaussFromCL = clij.converter(dst).getImagePlus();

        assertTrue(compareImages(gauss, gaussFromCL));
    }

    @Test
    public void blurSlicewise() {
        ImagePlus gauss = new Duplicator().run(testImp1);

        IJ.run(gauss, "Gaussian Blur...", "sigma=2 stack");

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.blurSlicewise(clij, src, dst, 6,6, 2,2);
        ImagePlus gaussFromCL = clij.converter(dst).getImagePlus();

        assertTrue(compareImages(gauss, gaussFromCL));
    }

    @Test
    public void copy() {
        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.converter(dst).getImagePlus();

        assertTrue(compareImages(testImp1, copyFromCL));
    }

    @Test
    public void copySlice() {
      ImagePlus copy = new Duplicator().run(testImp1, 3,3);

      ClearCLImage src = clij.converter(testImp1).getClearCLImage();
      ClearCLImage dst = clij.createCLImage(new long[]{src.getWidth(), src.getHeight()}, src.getChannelDataType());

      Kernels.copySlice(clij, src, dst, 2);
      ImagePlus copyFromCL = clij.converter(dst).getImagePlus();

      assertTrue(compareImages(copy, copyFromCL));
    }

    @Test
    public void crop() {
        Roi roi = new Roi(2,2,10,10);
        testImp1.setRoi(roi);
        ImagePlus crop = new Duplicator().run(testImp1, 3, 12);


        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.createCLImage(new long[]{10,10, 10}, src.getChannelDataType());

        Kernels.crop(clij, src, dst, 2, 2,2);
        ImagePlus cropFromCL = clij.converter(dst).getImagePlus();

        assertTrue(compareImages(crop, cropFromCL));
    }


/*
    public static void main(String... args) {
        new ImageJ();
        KernelsTest t = new KernelsTest();
        t.initTest();
        t.detectMaxima();
    }
*/

    @Test
    public void detectMaxima() {

        ImagePlus spotsImage = NewImage.createImage("", 100, 100, 3,16, NewImage.FILL_BLACK);

        spotsImage.setZ(2);
        ImageProcessor ip1 = spotsImage.getProcessor();
        ip1.set(50, 50, 10);
        ip1.set(60, 60, 10);
        ip1.set(70, 70, 10);

        spotsImage.show();
        //IJ.run(spotsImage, "Find Maxima...", "noise=2 output=[Single Points]");

        ByteProcessor byteProcessor = new MaximumFinder().findMaxima(spotsImage.getProcessor(), 2, MaximumFinder.SINGLE_POINTS, true);
        ImagePlus maximaImp = new ImagePlus("A", byteProcessor);

        ClearCLImage src = clij.converter(spotsImage).getClearCLImage();
        ClearCLImage dst = clij.converter(spotsImage).getClearCLImage();

        Kernels.detectOptima(clij, src, dst, 1, true);
        ImagePlus maximaFromCL = clij.converter(dst).getImagePlus();
        maximaFromCL = new Duplicator().run(maximaFromCL, 2, 2);

        IJ.run(maximaImp, "Divide...", "value=255");

        assertTrue(compareImages(maximaImp, maximaFromCL));
    }

    @Test
    public void differenceOfGaussian() {
        System.out.println("Todo: implement test for DoG");
    }

    @Test
    public void dilate() {
        ClearCLImage maskCL = clij.converter(mask).getClearCLImage();
        ClearCLImage maskCLafter = clij.converter(mask).getClearCLImage();

        Kernels.dilate(clij, maskCL,maskCLafter);

        double sum = Kernels.sumPixels(clij, maskCLafter);

        assertTrue(sum == 81);
    }

    @Test
    public void downsample() {
        System.out.println("Todo: implement test for downsample");
/*
        testImp1.show();
        IJ.run(testImp1, "Scale...", "x=0.5 y=0.5 z=0.5 width=512 height=1024 depth=5 interpolation=None process create");
        ImagePlus downsampled = IJ.getImage();

        ClearCLImage src = clij.converter(testImp1).getClearCLImage();
        ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

        Kernels.downsample(clij, src, dst, 0.5f, 0.5f, 0.5f);

        ImagePlus downsampledCL = clij.converter(dst).getImagePlus();

      assertTrue(compareImages(downsampled, downsampledCL));
      */
    }

    @Test
    public void erode() {
        ClearCLImage maskCL = clij.converter(mask).getClearCLImage();
        ClearCLImage maskCLafter = clij.converter(mask).getClearCLImage();

        Kernels.erode(clij, maskCL,maskCLafter);

        double sum = Kernels.sumPixels(clij, maskCLafter);

        assertTrue(sum == 1);
    }

  @Test
  public void invertBinary() {
    ClearCLImage maskCL = clij.converter(mask).getClearCLImage();
    ClearCLImage maskCLafter = clij.converter(mask).getClearCLImage();

    Kernels.invertBinary(clij, maskCL, maskCLafter);

    double sumCL = Kernels.sumPixels(clij, maskCL);
    double sumCLafter = Kernels.sumPixels(clij, maskCLafter);

    assertTrue(sumCLafter == maskCL.getWidth() * maskCL.getHeight() * maskCL.getDepth() - sumCL);
  }


  @Test
    public void mask() {
        System.out.println("Todo: implement test for mask");
    }

    @Test
    public void maskStackWithPlane() {
      System.out.println("Todo: implement test for maskStackWithPlane");
    }


    @Test
    public void maxProjection() throws InterruptedException
    {
      ImagePlus maxProjection = NewImage.createShortImage("", testImp1.getWidth(), testImp2.getHeight(), 1, NewImage.FILL_BLACK);
      ImageProcessor ipMax = maxProjection.getProcessor();

      ImagePlus testImp1copy = new Duplicator().run(testImp1);
      for (int z = 0; z < testImp1copy.getNSlices(); z++) {
        testImp1copy.setZ(z + 1);
        ImageProcessor ip = testImp1copy.getProcessor();
        for (int x = 0; x < testImp1copy.getWidth(); x++) {
          for (int y = 0; y < testImp1copy.getHeight(); y++)
          {
            float value = ip.getf(x, y);
            if (value > ipMax.getf(x, y)) {
              ipMax.setf(x, y, value);
            }
          }
        }
      }

      ClearCLImage src = clij.converter(testImp1).getClearCLImage();
      ClearCLImage dst = clij.createCLImage(new long[]{src.getWidth(), src.getHeight()}, src.getChannelDataType());

      Kernels.maxProjection(clij, src, dst);

      ImagePlus maxProjectionCL = clij.converter(dst).getImagePlus();

      assertTrue(compareImages(maxProjection, maxProjectionCL));

    }

    @Test
    public void multiplyPixelwise() {
        System.out.println("Todo: implement test for multiplPixelwise");

      ImagePlus multiplied = new ImageCalculator().run("Multiply create stack", testImp1, testImp2);
      ClearCLImage src = clij.converter(testImp1).getClearCLImage();
      ClearCLImage src1 = clij.converter(testImp2).getClearCLImage();
      ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

      Kernels.multiplyPixelwise(clij, src, src1, dst);

      ImagePlus multipliedCL = clij.converter(dst).getImagePlus();

      assertTrue(compareImages(multiplied, multipliedCL));
    }


    @Test
    public void multiplyScalar() {
      ImagePlus added = new Duplicator().run(testImp1);
      IJ.run(added, "Multiply...", "value=2 stack");

      ClearCLImage src = clij.converter(testImp1).getClearCLImage();
      ClearCLImage dst = clij.converter(testImp1).getClearCLImage();

      Kernels.multiplyScalar(clij, src, dst, 2);
      ImagePlus addedFromCL = clij.converter(dst).getImagePlus();

      assertTrue(compareImages(added, addedFromCL));

    }

    @Test
    public void multiplyStackWithPlane() {
      System.out.println("Todo: implement test for multiplyStackWithPlane");
    }

  @Test
  public void set() {
    ClearCLImage imageCL = clij.converter(mask).getClearCLImage();

    Kernels.set(clij, imageCL, 2);

    double sum = Kernels.sumPixels(clij, imageCL);

    assertTrue(sum == imageCL.getWidth() * imageCL.getHeight() * imageCL.getDepth() * 2);

  }


  @Test
    public void sumPixels() {
        ClearCLImage maskCL = clij.converter(mask).getClearCLImage();

        double sum = Kernels.sumPixels(clij, maskCL);

        assertTrue(sum == 27);

    }

    @Test
    public void threshold() {
        System.out.println("Todo: implement test for threshold");
    }
}