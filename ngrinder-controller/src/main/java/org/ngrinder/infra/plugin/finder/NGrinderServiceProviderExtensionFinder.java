package org.ngrinder.infra.plugin.finder;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ro.fortsoft.pf4j.AbstractExtensionFinder;
import ro.fortsoft.pf4j.PluginClassLoader;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.processor.ServiceProviderExtensionStorage;

/**
 * AbstractExtensionFinder extended class.
 * readPluginsStorages method override.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j
 * @since 3.0
 */
public class NGrinderServiceProviderExtensionFinder extends AbstractExtensionFinder {

	private final String EXTENSIONS_RESOURCE_PATH = "META-INF/extensions.idx";

	public NGrinderServiceProviderExtensionFinder(PluginManager pluginManager) {
		super(pluginManager);
	}

	@Override
    public Map<String, Set<String>> readClasspathStorages() {
		return new LinkedHashMap<String, Set<String>>();
    }

    @Override
    public Map<String, Set<String>> readPluginsStorages() {
        log.debug("Reading extensions storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.debug("Reading extensions storages for plugin '{}'", pluginId);
            final Set<String> bucket = new HashSet<String>();

            try {
                URL url = ((PluginClassLoader) plugin.getPluginClassLoader()).findResource(EXTENSIONS_RESOURCE_PATH);
                if (url != null) {
                    Path extensionPath;
                    if (url.toURI().getScheme().equals("jar")) {
                        FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.<String, Object>emptyMap());
                        extensionPath = fileSystem.getPath(EXTENSIONS_RESOURCE_PATH);
                    } else {
                        extensionPath = Paths.get(url.toURI());
                    }
                    Files.walkFileTree(extensionPath, Collections.<FileVisitOption>emptySet(), 1, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            log.debug("Read '{}'", file);
                            Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                            ServiceProviderExtensionStorage.read(reader, bucket);
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } else {
                    log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE_PATH);
                }

                if (bucket.isEmpty()) {
                    log.debug("No extensions found");
                } else {
                    log.debug("Found possible {} extensions:", bucket.size());
                    for (String entry : bucket) {
                        log.debug("   " + entry);
                    }
                }

                result.put(pluginId, bucket);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (URISyntaxException e) {
            	log.error(e.getMessage(), e);
			}
        }

        return result;
    }

}
