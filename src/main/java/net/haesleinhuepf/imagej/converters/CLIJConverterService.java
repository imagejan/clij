package net.haesleinhuepf.imagej.converters;

import clearcl.ClearCL;
import net.haesleinhuepf.imagej.ClearCLIJ;
import net.imagej.ImageJService;
import org.scijava.plugin.AbstractPTService;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.Service;

import java.util.HashMap;

/**
 * CLIJConverterService
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = Service.class)
public class CLIJConverterService extends AbstractPTService<CLIJConverterPlugin> implements ImageJService {
    private static ClearCLIJ clij;

    private HashMap<ClassPair, PluginInfo<CLIJConverterPlugin>> converterPlugins = new HashMap<>();

    private class ClassPair {
        Class a;
        Class b;

        public ClassPair(Class a, Class b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ClassPair)) {
                return false;
            }
            return ((ClassPair) obj).a == a && ((ClassPair) obj).b == b;
        }
    }

    @Override
    public void initialize() {
        for (final PluginInfo<CLIJConverterPlugin> info : getPlugins()) {
            final CLIJConverterPlugin plugin = pluginService().createInstance(info);
            converterPlugins.put(new ClassPair(plugin.getSourceType(), plugin.getTargetType()), info);
        }
    }

    @Override
    public Class<CLIJConverterPlugin> getPluginType() {
        return CLIJConverterPlugin.class;
    }

    public <S, T> CLIJConverterPlugin<S, T> getConverter(Class<S> a, Class<T> b) {
        PluginInfo<CLIJConverterPlugin> info = converterPlugins.get(new ClassPair(a, b));
        if (info == null) {
            throw new IllegalArgumentException("No converter found from " + a + " to " + b);
        }
        CLIJConverterPlugin<S, T> converter = pluginService().createInstance(info);
        if (converter == null) {
            throw new IllegalArgumentException("Couldn't instantiate converter found from " + a + " to " + b);
        }
        converter.setCLIJ(getCLIJ());
        return converter;
    }

    public void setCLIJ(ClearCLIJ clij) {
        this.clij = clij;
    }

    public ClearCLIJ getCLIJ() {
        if (clij == null) {
            clij = ClearCLIJ.getInstance();
        }
        return clij;
    }
}
