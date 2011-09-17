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

import hudson.model.Computer;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeProperty;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.Os;
import org.hudsonci.plugins.vault.slave.CustomNodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to fetch the context for a node.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class NodeContext
    implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(NodeContext.class);

    private final Map<String,String> attributes;

    private final Properties systemProperties;

    private NodeContext(final Map<String, String> attributes, final Properties systemProperties) {
        assert attributes != null;
        this.attributes = attributes;
        assert systemProperties != null;
        this.systemProperties = systemProperties;
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    @Override
    public String toString() {
        return "NodeContext{" +
            "attributes=" + attributes +
            '}';
    }

    private static class FetchContext
        implements Callable<NodeContext,RuntimeException>
    {
        public NodeContext call() {
            Map<String,String> attrs = new HashMap<String,String>();

            attrs.put("os.family", Os.OS_FAMILY);
            attrs.put("os.name", Os.OS_NAME);
            attrs.put("os.arch", Os.OS_ARCH);
            attrs.put("os.version", Os.OS_VERSION);

            return new NodeContext(attrs, System.getProperties());
        }
    }

    public static NodeContext fetch(final Node node, final VirtualChannel channel) throws IOException, InterruptedException {
        assert node != null;
        assert channel != null;

        log.debug("Fetching context for node: {} on channel: {}", node.getDisplayName(), channel);

        NodeContext context = channel.call(new FetchContext());
        assert context != null;

        // Apply any custom context properties configured for the node
        for (NodeProperty prop : node.getNodeProperties()) {
            if (prop instanceof CustomNodeContext) {
                ((CustomNodeContext)prop).applyTo(context);
            }
        }

        return context;
    }

    private static Map<Node,NodeContext> cache = Collections.synchronizedMap(new HashMap<Node,NodeContext>());

    public static NodeContext get(final Node node, final VirtualChannel channel) throws IOException, InterruptedException {
        NodeContext context = cache.get(node);
        if (context == null) {
            context = fetch(node, channel);
            cache.put(node, context);
        }
        return context;
    }

    public static Map<Node,NodeContext> getCache() {
        return cache;
    }

    /**
     * Clear the cache when nodes go offline.
     */
    @Named
    @Singleton
    public static class ComputerListenerImpl
        extends ComputerListener
    {
        @Override
        public void onOffline(final Computer c) {
            log.trace("Clearing caches");
            
            NodeContext context = getCache().remove(c.getNode());
            if (context != null) {
                log.trace("Removed cached context for: {}", c.getDisplayName());
            }
        }
    }
}