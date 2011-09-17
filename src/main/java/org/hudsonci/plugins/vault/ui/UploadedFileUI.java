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

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.utils.plugin.ui.AdministratorUIComponent;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.StaplerAccessible;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User-interface for uploaded file.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class UploadedFileUI
    extends AdministratorUIComponent<UploadsUI>
{
    private static final Logger log = LoggerFactory.getLogger(UploadedFileUI.class);

    private final File data;

    public UploadedFileUI(final UploadsUI parent, final File data) {
        super(parent);
        assert data != null;
        this.data = data;
    }

    @JellyAccessible
    public File getData() {
        return data;
    }

    public String getDisplayName() {
        return data.getName();
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("new-icon-48x48.png");
    }

    @StaplerAccessible
    public void doDelete(final StaplerRequest req, final StaplerResponse resp)
        throws Exception
    {
        checkPermission();

        getData().delete();

        redirectParent(req, resp);
    }

    @StaplerAccessible
    public void doExtract(final StaplerRequest req, final StaplerResponse resp,
                          final @QueryParameter(value="path", required=true, fixEmpty=true) String path)
        throws Exception
    {
        checkPermission();

        log.info("Extracting file: {} -> {}", data, path);

        Expand expand = new Expand();
        expand.setProject(new Project());
        expand.setSrc(data);

        File target = Vault.get().resolvePath(path);
        if (target.exists() && target.isFile()) {
            log.warn("Replacing previous content");
            target.delete();
        }

        target.mkdirs();
        expand.setDest(target);
        expand.execute();

        redirectParent(req, resp);
    }
}