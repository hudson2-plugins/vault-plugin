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

import java.util.Collection;

import org.hudsonci.plugins.vault.Bundle;
import org.hudsonci.plugins.vault.Package;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.plugins.vault.util.MultimapUtil;
import org.hudsonci.utils.plugin.ui.AdministratorUIComponent;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.StaplerAccessible;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * User-interface for {@link org.hudsonci.plugins.vault.Bundle}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class BundleUI
    extends AdministratorUIComponent<VaultUI>
{
    private final Bundle data;

    public BundleUI(final VaultUI parent, final Bundle data) {
        super(parent);
        assert data != null;
        this.data = data;
    }

    @JellyAccessible
    public Bundle getData() {
        return data;
    }

    @JellyAccessible
    public Collection<org.hudsonci.plugins.vault.Package> getPackages() {
        return data.getPackages();
    }
    
    public String getDisplayName() {
        return data.getName();
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("archive-icon-48x48.png");
    }

    @StaplerAccessible
    public Object getDynamic(final String id, final StaplerRequest req, final StaplerResponse resp) {
        Package pkg = data.getPackage(id);
        if (pkg != null) {
            return new PackageUI(this, pkg);
        }
        return null;
    }

    /**
     * Render package properties suitable for display in the package table.
     */
    @JellyAccessible
    public String renderProperties(final Package pkg) {
        assert pkg != null;
        return MultimapUtil.save(pkg.getProperties(), "<br/>");
    }

    @StaplerAccessible
    public void doUpdate(final StaplerRequest req, final StaplerResponse resp,
                         final @QueryParameter(value="name", required=true, fixEmpty=true) String name,
                         final @QueryParameter(value="type", required=true, fixEmpty=true) String type,
                         final @QueryParameter(value="description", required=false, fixEmpty=true) String description)
        throws Exception
    {
        checkPermission();

        data.setType(type);
        data.setDescription(description);

        if (!data.getName().equals(name)) {
            Vault.get().renameBundle(data.getName(), name);
        }
        else {
            Vault.get().save();
        }

        redirectSelf(req, resp);
    }

    @StaplerAccessible
    public void doDelete(final StaplerRequest req, final StaplerResponse resp)
        throws Exception
    {
        checkPermission();

        Vault.get().removeBundle(data.getName());

        redirectParent(req, resp);
    }

    @StaplerAccessible
    public void doCreatePackage(final StaplerRequest req, final StaplerResponse resp,
                                final @QueryParameter(value="path", required=true, fixEmpty=true) String path,
                                final @QueryParameter(value="includes", required=false, fixEmpty=true) String includes,
                                final @QueryParameter(value="excludes", required=false, fixEmpty=true) String excludes,
                                final @QueryParameter(value="properties", required=false, fixEmpty=true) String properties,
                                final @QueryParameter(value="description", required=false, fixEmpty=true) String description)
        throws Exception
    {
        checkPermission();

        Package pkg = new Package();
        pkg.setPath(path);
        pkg.setIncludes(includes);
        pkg.setExcludes(excludes);

        if (properties != null && properties.trim().length() != 0) {
            MultimapUtil.load(pkg.getProperties(), properties);
        }

        pkg.setDescription(description);
        
        data.addPackage(pkg);

        // Create the cache
        Vault.get().buildPackageCache(data, pkg);

        Vault.get().save();

        redirectSelf(req, resp);
    }
}