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

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hudsonci.plugins.vault.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs a package onto a node.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PackageInstaller
{
    private static final Logger log = LoggerFactory.getLogger(PackageInstaller.class);

    private String bundleName;

    private FilePath location;

    private Node node;

    private VirtualChannel channel;

    private TaskListener listener;

    private final List<PackageInstallListener> installListeners = new ArrayList<PackageInstallListener>();

    public PackageInstaller() {
        addInstallListener(new InstallScriptExecutor());
    }

    public String getBundleName() {
        if (bundleName == null) {
            throw new IllegalStateException("Bundle name not configured");
        }
        return bundleName;
    }

    public void setBundleName(final String bundleName) {
        this.bundleName = bundleName;
    }

    public FilePath getLocation() {
        if (location == null) {
            throw new IllegalStateException("Location not configured");
        }
        return location;
    }

    public void setLocation(final FilePath location) {
        this.location = location;
    }

    public Node getNode() {
        if (node == null) {
            throw new IllegalStateException("Node not configured");
        }
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
    }

    public VirtualChannel getChannel() {
        if (channel == null) {
            channel = getNode().getChannel();
        }
        if (channel == null) {
            throw new IllegalStateException("Channel not configured; unable to detect");
        }
        return channel;
    }

    public void setChannel(final VirtualChannel channel) {
        this.channel = channel;
    }

    public TaskListener getListener() {
        if (listener == null) {
            listener = TaskListener.NULL;
        }
        return listener;
    }

    public void setListener(final TaskListener listener) {
        this.listener = listener;
    }

    public Collection<PackageInstallListener> getInstallListeners() {
        return installListeners;
    }

    public void addInstallListener(final PackageInstallListener listener) {
        installListeners.add(listener);
    }

    public void removeInstallListener(final PackageInstallListener listener) {
        installListeners.remove(listener);
    }

    private Package selectPackage() throws Exception {
        PackageSelector selector = new PackageSelector();
        selector.setBundleName(getBundleName());

        NodeContext context = NodeContext.get(getNode(), getChannel());
        selector.setContext(context);

        return selector.select();
    }

    public FilePath install() throws Exception {
        log.debug("Installing package for bundle: {} on: {}", getBundleName(), getNode().getDisplayName());

        Package pkg = selectPackage();
        log.debug("Selected package: {}", pkg);

        FilePath location = getLocation();
        log.debug("Install location: {}", location);

        String msg = String.format("Installing package for bundle: %s (%s) to: %s",
            getBundleName(), pkg.getId(), location);

        if (location.installIfNecessaryFrom(pkg.getCacheFile().toURI().toURL(), getListener(), msg)) {
            for (PackageInstallListener listener : getInstallListeners()) {
                try {
                    listener.installed(pkg, location, getListener());
                }
                catch (Exception e) {
                    log.error("Install listener execution failed: {}", e);
                }
            }

            msg = "Package installed";
            log.debug(msg);
            getListener().getLogger().println(msg);
        }
        else {
            log.debug("Package is already installed");
        }

        return location;
    }

    @Override
    public String toString() {
        return "PackageInstaller{" +
            "bundleName='" + bundleName + '\'' +
            ", location=" + location +
            ", node=" + node +
            ", channel=" + channel +
            ", listener=" + listener +
            ", installListeners=" + installListeners +
            '}';
    }
}