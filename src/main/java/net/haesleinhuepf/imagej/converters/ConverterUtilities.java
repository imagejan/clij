package net.haesleinhuepf.imagej.converters;

import clearcl.enums.ImageChannelDataType;
import coremem.enums.NativeTypeEnum;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * ConverterUtilities
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 12 2018
 */
public class ConverterUtilities {
    public static <T extends RealType<T>> NativeTypeEnum imglib2TypeToNativeType(T lPixel) {
        if (lPixel instanceof UnsignedByteType) {
            return NativeTypeEnum.UnsignedByte;
        } else if (lPixel instanceof ByteType) {
            return NativeTypeEnum.Byte;
        } else if (lPixel instanceof UnsignedShortType) {
            return NativeTypeEnum.UnsignedShort;
        } else if (lPixel instanceof ShortType) {
            return NativeTypeEnum.Short;
        } else if (lPixel instanceof FloatType) {
            return NativeTypeEnum.Float;
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert image of type " + lPixel.toString());
        }
    }

    public static <T extends RealType<T>> ImageChannelDataType imglib2TypeToImageChannelDataType(T pixel) {
        if (pixel instanceof UnsignedByteType) {
            return ImageChannelDataType.UnsignedInt8;
        } else if (pixel instanceof ByteType) {
            return ImageChannelDataType.SignedInt8;
        } else if (pixel instanceof UnsignedShortType) {
            return ImageChannelDataType.UnsignedInt16;
        } else if (pixel instanceof ShortType) {
            return ImageChannelDataType.SignedInt16;
        } else if (pixel instanceof FloatType) {
            return ImageChannelDataType.Float;
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert image of type " + pixel);
        }
    }

    public static ImageChannelDataType nativeTypeToImageChannelDataType(NativeTypeEnum lType) {
        ImageChannelDataType lImageChannelType = null;

        if (lType == NativeTypeEnum.UnsignedByte) {
            lImageChannelType = ImageChannelDataType.UnsignedInt8;
        } else if (lType == NativeTypeEnum.Byte) {
            lImageChannelType = ImageChannelDataType.SignedInt8;
        } else if (lType == NativeTypeEnum.UnsignedShort) {
            lImageChannelType = ImageChannelDataType.UnsignedInt16;
        } else if (lType == NativeTypeEnum.Short) {
            lImageChannelType = ImageChannelDataType.SignedInt16;
        } else if (lType == NativeTypeEnum.Float) {
            lImageChannelType = ImageChannelDataType.Float;
        }
        return lImageChannelType;

    }

}
