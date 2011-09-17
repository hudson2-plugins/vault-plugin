/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.plugins.vault;

import hudson.model.Hudson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.hudsonci.plugins.vault.util.Archiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hudson Vault.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Vault
    implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(Vault.class);

    private transient VaultPlugin plugin;

    private transient Archiver archiver;

    private File storeDir;

    private transient File rootDir;

    private transient File cacheDir;

    private transient File uploadDir;

    private final Set<Bundle> bundles = new TreeSet<Bundle>();

    public Vault(final VaultPlugin plugin) {
        assert plugin != null;
        this.plugin = plugin;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        // Re-attach to our plugin, needed for save()
        plugin = Hudson.getInstance().getPlugin(VaultPlugin.class);

        // Re-attach package cache file ref
        for (Bundle bundle : bundles) {
            for (Package pkg : bundle.getPackages()) {
                pkg.setCacheFile(getCacheFile(bundle, pkg));
            }
        }

        return this;
    }

    public void save() throws IOException {
        plugin.save();
    }

    private static Vault vault;

    public static Vault get() {
        if (vault == null) {
            vault = Hudson.getInstance().getPlugin(VaultPlugin.class).getVault();
        }
        return vault;
    }

    public File getStoreDir() {
        if (storeDir == null) {
            storeDir = new File(Hudson.getInstance().getRootDir(), "vault");
        }
        return storeDir;
    }

    public void setStoreDir(final File dir) {
        this.storeDir = dir;
    }

    public File getRootDir() {
        if (rootDir == null) {
            // don't save, just return
            return new File(getStoreDir(), "root");
        }
        return rootDir;
    }

    public void setRootDir(final File dir) {
        this.rootDir = dir;
    }

    public File getCacheDir() {
        if (cacheDir == null) {
            // don't save, just return
            return new File(getStoreDir(), "cache");
        }
        return cacheDir;
    }

    public void setCacheDir(final File dir) {
        this.cacheDir = dir;
    }

    public File getUploadsDir() {
        if (uploadDir == null) {
            // don't save, just return
            return new File(getStoreDir(), "uploads");
        }
        return uploadDir;
    }

    public void setUploadDir(final File dir) {
        this.uploadDir = dir;
    }

    public Collection<Bundle> getBundles() {
        return bundles;
    }

    public Bundle getBundle(final String name) {
        assert name != null;

        for (Bundle bundle : bundles) {
            if (name.equals(bundle.getName())) {
                return bundle;
            }
        }
        
        return null;
    }

    public Collection<Bundle> findBundles(final String type) {
        Collection<Bundle> bundles = getBundles();

        // w/o type, we will show all bundles
        if (type == null) {
            return bundles;
        }

        // else, limit the bundles to those with matching types
        Collection<Bundle> found = new ArrayList<Bundle>();
        for (Bundle bundle : bundles) {
            if (type.equals(bundle.getType())) {
                found.add(bundle);
            }
        }

        return found;
    }

    public Bundle addBundle(final Bundle bundle) throws Exception {
        assert bundle != null;

        log.info("Creating bundle: {}", bundle);

        if (getBundle(bundle.getName()) != null) {
            throw new DuplicateBundleException(bundle.getName());
        }

        bundles.add(bundle);

        save();

        return bundle;
    }

    public void removeBundle(final String name) throws Exception {
        Bundle bundle = getBundle(name);

        log.info("Removing bundle: {}", name);

        if (bundle == null) {
            throw new NoSuchBundleException(name);
        }

        bundles.remove(bundle);

        save();

        for (Package pkg : bundle.getPackages()) {
            removePackageCache(bundle, pkg);
        }
    }

    public void renameBundle(final String source, final String target) throws Exception {
        assert source != null;
        assert target != null;

        log.info("Renaming bundle: {} -> {}", source, target);

        Bundle bundle = getBundle(source);
        if (bundle == null) {
            throw new NoSuchBundleException(source);
        }

        if (getBundle(target) != null) {
            throw new DuplicateBundleException(source);
        }

        bundles.remove(bundle);
        bundle = new Bundle(target, bundle);
        bundles.add(bundle);

        save();

        // Remove cache, let installation rebuild as needed
        for (Package pkg : bundle.getPackages()) {
            removePackageCache(bundle, pkg);
        }
    }

    private Archiver getArchiver() {
        if (archiver == null) {
            archiver = new Archiver();
        }
        return archiver;
    }

    public File resolvePath(String path) {
        assert path != null;

        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }
        return new File(Vault.get().getRootDir(), path);
    }

    private File getCacheFile(final Bundle bundle, final Package pkg) {
        assert bundle != null;
        assert pkg != null;

        String name = String.format("%s,%s.zip", bundle.getName(), pkg.getId());
        return new File(getCacheDir(), name);
    }

    private File getSourceDir(final Package pkg) {
        assert pkg != null;

        return resolvePath(pkg.getPath());
    }

    public File buildPackageCache(final Bundle bundle, final Package pkg) throws IOException {
        assert bundle != null;
        assert pkg != null;

        File file = getCacheFile(bundle, pkg);
        log.info("Building package cache: {}", file);

        if (file.exists()) {
            if (!file.delete()) {
                log.error("Failed to remove old cache: {}", file);
            }
        }

        File source = getSourceDir(pkg);
        if (!source.exists()) {
            throw new FileNotFoundException(source.getAbsolutePath());
        }

        getArchiver().archive(pkg, file, source);

        pkg.setCacheFile(file);

        return file;
    }

    public void removePackageCache(final Bundle bundle, final Package pkg) {
        assert bundle != null;
        assert pkg != null;

        File file = getCacheFile(bundle, pkg);
        log.info("Removing package cache: {}", file);

        if (file.exists()) {
            if (!file.delete()) {
                log.error("Failed to remove cache: {}", file);
            }
        }

        pkg.setCacheFile(null);
    }
}
