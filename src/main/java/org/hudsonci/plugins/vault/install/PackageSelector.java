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

package org.hudsonci.plugins.vault.install;

import java.io.IOException;
import java.util.Map;

import org.hudsonci.plugins.vault.Bundle;
import org.hudsonci.plugins.vault.Package;
import org.hudsonci.plugins.vault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

/**
 * Selects a package for a given bundle name.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PackageSelector
{
    private static final Logger log = LoggerFactory.getLogger(PackageSelector.class);

    private String bundleName;

    private NodeContext context;

    public String getBundleName() {
        if (bundleName == null) {
            throw new IllegalStateException("Bundle name not configured");
        }
        return bundleName;
    }

    public void setBundleName(final String name) {
        this.bundleName = name;
    }

    public NodeContext getContext() {
        if (context == null) {
            throw new IllegalStateException("Context not configured");
        }
        return context;
    }

    public void setContext(final NodeContext context) {
        this.context = context;
    }

    public org.hudsonci.plugins.vault.Package select() {
        String name = getBundleName();

        log.debug("Selecting bundle: {} with context: {}", name, getContext());

        Bundle bundle = Vault.get().getBundle(name);
        if (bundle == null) {
            log.warn("Unable to select package; no such bundle: {}", name);
            return null;
        }

        Package found = null;
        for (Package pkg : bundle.getPackages()) {
            if (matches(pkg, getContext())) {
                found = pkg;
                break;
            }
        }

        if (found != null) {
            log.debug("Selected: {}", found);
            
            if (!found.isCached()) {
                log.warn("Package cache is missing; rebuilding");
                try {
                    Vault.get().buildPackageCache(bundle, found);
                }
                catch (IOException e) {
                    throw new PackageInstallException("Failed to build package cache", e);
                }
            }
        }

        if (found == null) {
            throw new PackageInstallException(String.format("Unable to select package for bundle: %s", getBundleName()));
        }
        
        return found;
    }

    public boolean matches(final Package pkg, final NodeContext context) {
        assert pkg != null;
        assert context != null;

        Multimap<String, String> props = pkg.getProperties();

        // No properties, matches anything
        if (props.isEmpty()) {
            return true;
        }

        Map<String,String> attrs = context.getAttributes();
        for (String key : props.keySet()) {
            String found = attrs.get(key);

            boolean matched = false;

            // See if the found data, matches at least one of our properties keys
            for (String expect : props.get(key)) {
                if (expect.equalsIgnoreCase(found)) {
                    matched = true;
                    break;
                }
            }

            // If none of the values matched, we don't match
            if (!matched) {
                return false;
            }
        }

        // If we get this far, all property keys have matched
        return true;
    }

    @Override
    public String toString() {
        return "PackageSelector{" +
            "bundleName='" + bundleName + '\'' +
            ", context=" + context +
            '}';
    }
}