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

package org.hudsonci.plugins.vault.ui;

import hudson.FilePath;
import hudson.model.DirectoryBrowserSupport;

import java.io.File;

import org.hudsonci.plugins.vault.Bundle;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.utils.plugin.ui.AdministratorUIComponent;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.StaplerAccessible;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * User-interface for the {@link org.hudsonci.plugins.vault.Vault}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class VaultUI
    extends AdministratorUIComponent<VaultLink>
{
    public VaultUI(final VaultLink parent) {
        super(parent);
    }

    public String getDisplayName() {
        // TODO: Use localizer
        return "Vault";
    }

    private Vault vault;

    @JellyAccessible
    public Vault getVault() {
        if (vault == null) {
            vault = Vault.get();
        }
        return vault;
    }

    private DirectoryBrowserSupport browse(final File dir, final String title) {
        checkPermission();
        // HACK: Make sure the dir exists, or we'll get a 404
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new DirectoryBrowserSupport(this, new FilePath(dir), String.format("%s %s", getDisplayName(), title), "folder.gif", true);
    }

    @StaplerAccessible
    public DirectoryBrowserSupport doFiles(final StaplerRequest req, final StaplerResponse resp) {
        // TODO: Use localizer
        return browse(getVault().getRootDir(), "Files");
    }

    @StaplerAccessible
    public DirectoryBrowserSupport doCache(final StaplerRequest req, final StaplerResponse resp) {
        // TODO: Use localizer
        return browse(getVault().getCacheDir(), "Cache");
    }

    @StaplerAccessible
    public Object getBundle(final String name) {
        Bundle bundle = getVault().getBundle(name);
        if (bundle != null) {
            return new BundleUI(this, bundle);
        }

        // FIXME: This isn't very nice
        return null;
    }

    @StaplerAccessible
    public void doCreateBundle(final StaplerRequest req, final StaplerResponse resp,
                               final @QueryParameter(value="name", required=true, fixEmpty=true) String name,
                               final @QueryParameter(value="type", required=true, fixEmpty=true) String type,
                               final @QueryParameter(value="description", required=false, fixEmpty=true) String description)
        throws Exception
    {
        checkPermission();

        Bundle bundle = new Bundle(name);
        bundle.setType(type);
        bundle.setDescription(description);

        getVault().addBundle(bundle);

        redirectParent(req, resp);
    }

    @StaplerAccessible
    public Object getUploads() {
        return new UploadsUI(this);
    }
}