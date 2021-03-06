package net.haesleinhuepf.clij.macro.modules;

import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.kernels.Kernels;
import net.haesleinhuepf.clij.test.TestUtilities;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.*;

public class CopyTest {

    @Test
    public void copyImageToBuffer3d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp1 = TestUtilities.getRandomImage(100, 100, 3, 32, 0, 100);

        ClearCLImage src = clij.convert(testImp1, ClearCLImage.class);
        ClearCLBuffer
                dst =
                clij.createCLBuffer(src.getDimensions(), src.getNativeType());

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copyImageToBuffer2d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp2D1 = TestUtilities.getRandomImage(100, 100, 1, 32, 0, 100);

        ClearCLImage src = clij.convert(testImp2D1, ClearCLImage.class);
        ClearCLBuffer
                dst =
                clij.createCLBuffer(src.getDimensions(), src.getNativeType());

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp2D1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copyBufferToImage3d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp1 = TestUtilities.getRandomImage(100, 100, 3, 32, 0, 100);

        ClearCLBuffer src = clij.convert(testImp1, ClearCLBuffer.class);
        ClearCLImage dst = clij.convert(testImp1, ClearCLImage.class);

        Kernels.set(clij, dst, 0f);

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copyBufferToImage2d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp2D1 = TestUtilities.getRandomImage(100, 100, 1, 32, 0, 100);

        ClearCLBuffer src = clij.convert(testImp2D1, ClearCLBuffer.class);
        ClearCLImage dst = clij.convert(testImp2D1, ClearCLImage.class);

        Kernels.set(clij, dst, 0f);

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp2D1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copy3d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp1 = TestUtilities.getRandomImage(100, 100, 3, 32, 0, 100);

        ClearCLImage src = clij.convert(testImp1, ClearCLImage.class);
        ClearCLImage
                dst =
                clij.createCLImage(src.getDimensions(),
                        src.getChannelDataType());
        ;

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copy2d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp2D1 = TestUtilities.getRandomImage(100, 100, 1, 32, 0, 100);

        ClearCLImage src = clij.convert(testImp2D1, ClearCLImage.class);
        ClearCLImage
                dst =
                clij.createCLImage(src.getDimensions(),
                        src.getChannelDataType());

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);

        assertTrue(TestUtilities.compareImages(testImp2D1, copyFromCL));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copyBuffer3d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp1 = TestUtilities.getRandomImage(100, 100, 3, 32, 0, 100);

        ImagePlus imp = new Duplicator().run(testImp1);
        Img<FloatType> img = ImageJFunctions.convertFloat(testImp1);

        ClearCLBuffer src = clij.convert(imp, ClearCLBuffer.class);
        ClearCLBuffer
                dst =
                clij.createCLBuffer(src.getDimensions(), src.getNativeType());

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);
        assertTrue(TestUtilities.compareImages(testImp1, copyFromCL));

        RandomAccessibleInterval
                rai =
                clij.convert(dst, RandomAccessibleInterval.class);
        assertTrue(TestUtilities.compareIterableIntervals(img,
                Views.iterable(
                        rai)));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

    @Test
    public void copyBuffer2d() {
        CLIJ clij = CLIJ.getInstance();
        ImagePlus testImp2D1 = TestUtilities.getRandomImage(100, 100, 1, 32, 0, 100);

        ImagePlus imp = new Duplicator().run(testImp2D1);
        Img<FloatType> img = ImageJFunctions.convertFloat(testImp2D1);

        ClearCLBuffer src = clij.convert(imp, ClearCLBuffer.class);
        ClearCLBuffer
                dst =
                clij.createCLBuffer(src.getDimensions(), src.getNativeType());

        Kernels.copy(clij, src, dst);
        ImagePlus copyFromCL = clij.convert(dst, ImagePlus.class);
        assertTrue(TestUtilities.compareImages(testImp2D1, copyFromCL));

        RandomAccessibleInterval
                rai =
                clij.convert(dst, RandomAccessibleInterval.class);
        assertTrue(TestUtilities.compareIterableIntervals(img,
                Views.iterable(
                        rai)));

        src.close();
        dst.close();
        IJ.exit();
        clij.close();
    }

}