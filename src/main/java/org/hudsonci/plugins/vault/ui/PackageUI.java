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
 * User-interface for {@link org.hudsonci.plugins.vault.Package}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PackageUI
    extends AdministratorUIComponent<BundleUI>
{
    private final org.hudsonci.plugins.vault.Package data;

    public PackageUI(final BundleUI parent, final Package data) {
        super(parent);
        assert data != null;
        this.data = data;
    }

    @JellyAccessible
    public Package getData() {
        return data;
    }

    public String getDisplayName() {
        return data.getId().toString();
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("archive-icon-48x48.png");
    }

    /**
     * Get properties as <tt>name=value</tt> suitable for rendering in the text area.
     */
    @JellyAccessible
    public String getProperties() {
        return MultimapUtil.save(data.getProperties());
    }

    @StaplerAccessible
    public void doUpdate(final StaplerRequest req, final StaplerResponse resp,
                         final @QueryParameter(value="path", required=true, fixEmpty=true) String path,
                         final @QueryParameter(value="includes", required=false, fixEmpty=true) String includes,
                         final @QueryParameter(value="excludes", required=false, fixEmpty=true) String excludes,
                         final @QueryParameter(value="properties", required=false, fixEmpty=true) String properties,
                         final @QueryParameter(value="description", required=false, fixEmpty=true) String description)
        throws Exception
    {
        checkPermission();

        data.setPath(path);
        data.setIncludes(includes);
        data.setExcludes(excludes);

        if (properties != null && properties.trim().length() != 0) {
            MultimapUtil.load(data.getProperties(), properties);
        }
        else {
            data.setProperties(null);
        }

        data.setDescription(description);

        Vault.get().buildPackageCache(getParent().getData(), data);

        Vault.get().save();

        redirectSelf(req, resp);
    }

    @StaplerAccessible
    public void doDelete(final StaplerRequest req, final StaplerResponse resp)
        throws Exception
    {
        checkPermission();

        getParent().getData().removePackage(data);

        Vault.get().removePackageCache(getParent().getData(), data);

        Vault.get().save();

        redirectParent(req, resp);
    }

    @StaplerAccessible
    public void doRefresh(final StaplerRequest req, final StaplerResponse resp)
        throws Exception
    {
        checkPermission();

        Vault.get().buildPackageCache(getParent().getData(), data);

        Vault.get().save();

        redirectParent(req, resp);
    }
}