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

package org.hudsonci.plugins.vault.tool;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import hudson.tools.ToolInstallation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import org.hudsonci.plugins.vault.Bundle;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.plugins.vault.install.PackageInstallListener;
import org.hudsonci.plugins.vault.install.PackageInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Vault-based {@link ToolInstaller} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class ToolInstallerSupport
    extends ToolInstaller
    implements Serializable
{
    protected transient Logger log = LoggerFactory.getLogger(getClass());

    private final String bundleName;

    protected ToolInstallerSupport(final String bundleName) {
        super(null);
        assert bundleName != null;
        this.bundleName = bundleName;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private Object readResolve() {
        log = LoggerFactory.getLogger(getClass());
        return this;
    }

    public String getBundleName() {
        return bundleName;
    }

    @Override
    public FilePath performInstallation(final ToolInstallation tool, final Node node, final TaskListener listener)
        throws IOException, InterruptedException
    {
        assert listener != null;
        
        PackageInstaller installer = new PackageInstaller();
        installer.setBundleName(getBundleName());
        installer.setNode(node);
        installer.setListener(listener);

        FilePath location = preferredLocation(tool, node);
        installer.setLocation(location);

        if (this instanceof PackageInstallListener) {
            installer.addInstallListener((PackageInstallListener)this);
        }

        try {
            location = installer.install();
        }
        catch (Exception e) {
            listener.fatalError("Package installation failed: %s", e);
            e.printStackTrace(listener.getLogger());
        }

        return location;
    }

    protected static abstract class Descriptor<T extends ToolInstallerSupport>
        extends ToolInstallerDescriptor<T>
    {
        private final Class<? extends ToolInstallation> toolType;

        private final String bundleType;

        protected Descriptor(final Class<? extends ToolInstallation> toolType, final String bundleType) {
            assert toolType != null;
            this.toolType = toolType;
            assert bundleType != null;
            this.bundleType = bundleType;
        }

        public String getDisplayName() {
            // TODO: Use localizer
            return "Install from Vault";
        }

        @Override
        public boolean isApplicable(final Class<? extends ToolInstallation> type) {
            return toolType == type && !getBundles().isEmpty();
        }

        public Collection<Bundle> getBundles() {
            return Vault.get().findBundles(bundleType);
        }
    }
}