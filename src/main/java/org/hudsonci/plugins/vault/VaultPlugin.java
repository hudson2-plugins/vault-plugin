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

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.hudsonci.plugins.vault.tool.AntInstaller;
import org.hudsonci.plugins.vault.tool.JdkInstaller;
import org.hudsonci.plugins.vault.tool.LegacyMavenInstaller;
import org.hudsonci.utils.plugin.PluginUtil;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Hudson Vault plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@Named
@Singleton
public class VaultPlugin
    extends Plugin
    implements Serializable
{
    private static final Logger log = LoggerFactory.getLogger(Vault.class);

    private static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.alias("bundle", Bundle.class);
        XSTREAM.alias("package", Package.class);
    }

    private Vault vault;

    public String getShortName() {
        return PluginUtil.getWrapper(this).getShortName();
    }

    public Vault getVault() {
        if (vault == null) {
            vault = new Vault(this);
        }
        return vault;
    }

    @Override
    public void start() throws Exception {
        Hudson.XSTREAM.processAnnotations(new Class[] {
            AntInstaller.class,
            JdkInstaller.class,
            LegacyMavenInstaller.class,
        });
    }

    @Override
    public void postInitialize() throws Exception {
        load();

        // FIXME: Externalize vault data from basic configuration, like how surrogates catalog works
        
        // Force the vault to load
        Vault vault = getVault();
        File dir = vault.getStoreDir();
        log.info("Storage directory: {}", dir);

        // FIXME: Need to make directory structure needed, so links work as expected
    }

    @Override
    protected XmlFile getConfigXml() {
        return new XmlFile(XSTREAM, new File(Hudson.getInstance().getRootDir(), getShortName() + ".xml"));
    }

    @Override
    public void configure(final StaplerRequest req, final JSONObject formData)
        throws IOException, ServletException, Descriptor.FormException
    {
        assert req != null;
        assert formData != null;

        String dir = formData.getString("storeDir");
        File file = new File(dir);
        vault.setStoreDir(file);
        
        save();
    }
}
