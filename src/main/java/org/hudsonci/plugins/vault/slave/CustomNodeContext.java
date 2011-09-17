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

import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.tools.ant.filters.StringInputStream;
import org.hudsonci.plugins.vault.install.NodeContext;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Allows nodes to contribute custom context properties.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CustomNodeContext
    extends NodeProperty<Node>
{
    private final Map<String,String> properties;

    @DataBoundConstructor
    public CustomNodeContext(final String properties) throws IOException {
        Properties tmp = new Properties();
        tmp.load(new StringInputStream(properties));
        this.properties = new HashMap<String,String>();
        for (Object key : tmp.keySet()) {
            this.properties.put(String.valueOf(key), String.valueOf(tmp.get(key)));
        }
    }

    /**
     * Render for configuration form's text-area.
     */
    public String render() {
        if (properties == null) {
            return null;
        }

        StringBuilder buff = new StringBuilder();
        for (String key : properties.keySet()) {
            buff.append(key).append("=").append(properties.get(key)).append("\n");
        }
        
        return buff.toString();
    }

    @Override
    public String toString() {
        return "CustomContext{" +
            "properties=" + properties +
            '}';
    }

    public void applyTo(final NodeContext context) {
        assert context != null;
        context.getAttributes().putAll(properties);
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
            return "Vault selection properties";
        }
    }
}