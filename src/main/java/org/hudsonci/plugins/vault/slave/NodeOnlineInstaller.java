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

package org.hudsonci.plugins.vault.slave;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.hudsonci.plugins.vault.Bundle;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.plugins.vault.install.NodeContext;
import org.hudsonci.plugins.vault.install.PackageInstaller;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gossip.support.DC;

/**
 * Allows packages to be installed on a node when it becomes online.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NodeOnlineInstaller
    extends NodeProperty<Node>
{
    private static final Logger log = LoggerFactory.getLogger(NodeOnlineInstaller.class);

    private final List<Entry> entries;

    @DataBoundConstructor
    public NodeOnlineInstaller(final List<Entry> config) {
        // Parameter must match what is used in config.jelly
        this.entries = config;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    private void install(final Computer c, final Channel channel, final FilePath root, final TaskListener listener)
        throws Exception
    {
        assert c != null;
        assert channel != null;
        assert root != null;
        assert listener != null;

        // Skip if there is nothing configured
        if (entries == null || entries.isEmpty()) {
            return;
        }
        
        log.debug("Installing packages on: {}", c.getDisplayName());

        NodeContext context = NodeContext.get(c.getNode(), channel);

        for (Entry entry : entries) {
            log.debug("Entry: {}", entry);
            
            PackageInstaller installer = new PackageInstaller();

            installer.setBundleName(entry.getName());

            // Default path to bundle name if not given
            String path = entry.getPath();
            if (path == null || path.trim().length() == 0) {
                path = entry.getName();
            }

            installer.setLocation(resolveLocation(context, root, path));
            installer.setNode(c.getNode());
            installer.setChannel(channel);
            installer.setListener(listener);

            try {
                installer.install();
            }
            catch (Exception e) {
                listener.error("Package installation failed: %s", e);
            }
        }
    }

    private FilePath resolveLocation(final NodeContext context, final FilePath root, final String path) throws Exception {
        assert context != null;
        assert root != null;
        assert path != null;

        Interpolator interp = new StringSearchInterpolator();
        interp.addValueSource(new PropertiesBasedValueSource(context.getSystemProperties()));
        String location = interp.interpolate(path);

        // TODO: Check what this resolves to when installing on to master, maybe use different location for master or slave
        
        FilePath base = root.child("vault").child("install");
        FilePath file = base.child(location);
        
        return file.absolutize();
    }

    @Override
    public Environment setUp(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
        throws IOException, InterruptedException
    {
        // Log the context we use for selecting packages
        NodeContext context = NodeContext.get(build.getBuiltOn(), launcher.getChannel());
        PrintStream logger = listener.getLogger();
        logger.println("Node context:");
        for (Map.Entry<String,String> attr : context.getAttributes().entrySet()) {
            logger.printf("  %s=%s", attr.getKey(), attr.getValue()).println();
        }

        return super.setUp(build, launcher, listener);
    }

    @Override
    public String toString() {
        return "NodeOnlineInstaller{" +
            "entries=" + entries +
            '}';
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends NodePropertyDescriptor
    {
        @Override
        public String getDisplayName() {
            // TODO: Use localizer
            return "Install from Vault";
        }

        public Collection<Bundle> getBundles() {
            return Vault.get().getBundles();
        }
    }

    /**
     * Container for bundle installation configuration.
     */
    public static class Entry
    {
        /** The bundle name. */
        private final String name;

        /** The path to install on remote node. */
        private final String path;

        @DataBoundConstructor
        public Entry(final String name, final String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return "Entry{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
        }
    }

    /**
     * Allow packages to be installed when a node becomes online.
     */
    @Named
    @Singleton
    public static class ComputerListenerImpl
        extends ComputerListener
    {
        @Override
        public void preOnline(final Computer c, final Channel channel, final FilePath root, final TaskListener listener)
            throws IOException, InterruptedException
        {
            assert c != null;
            Node node = c.getNode();
            log.debug("Preparing to install node packages: {}", node.getDisplayName());
            
            DC.push(node.getDisplayName());
            try {
                for (NodeProperty prop : node.getNodeProperties()) {
                    if (prop instanceof NodeOnlineInstaller) {
                        try {
                            ((NodeOnlineInstaller)prop).install(c, channel, root, listener);
                        }
                        catch (Exception e) {
                            log.error("Installation failed", e);
                        }
                    }
                }
            }
            finally {
                DC.pop();
            }
        }
    }
}