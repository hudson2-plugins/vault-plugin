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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.FilePath;
import hudson.model.TaskListener;
import org.hudsonci.plugins.vault.Package;
import org.hudsonci.plugins.vault.install.PackageInstallListener;
import org.hudsonci.utils.tasks.Chmod;
import org.kohsuke.stapler.DataBoundConstructor;
import org.hudsonci.maven.plugin.install.MavenInstallation;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <a href="http://maven.apache.org">Apache Maven 3</a> {@link ToolInstallerSupport}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@XStreamAlias("vault-maven3-installer")
public class Maven3Installer
    extends ToolInstallerSupport
    implements PackageInstallListener
{
    public static final String TYPE = "maven3";

    @DataBoundConstructor
    public Maven3Installer(final String name) {
        super(name);
    }

    public void installed(final Package pkg, final FilePath location, final TaskListener listener) throws Exception {
        // Make sure everything under the bin directory is executable
        FilePath dir = location.child("bin");
        if (dir.exists()) {
            //noinspection OctalInteger
            dir.act(new Chmod(0755));
        }
        else {
            log.warn("Unable to change permissions; missing directory: {}", dir);
        }
    }

    @Named
    @Singleton
    @Typed(hudson.model.Descriptor.class)
    public static final class DescriptorImpl
        extends Descriptor<Maven3Installer>
    {
        public DescriptorImpl() {
            super(MavenInstallation.class, TYPE);
        }
    }
}