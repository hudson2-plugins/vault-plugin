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

import hudson.Util;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hudsonci.plugins.vault.Vault;
import org.hudsonci.utils.plugin.ui.AdministratorUIComponent;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.StaplerAccessible;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User-interface for managing file uploads.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class UploadsUI
    extends AdministratorUIComponent<VaultUI>
{
    private static final Logger log = LoggerFactory.getLogger(UploadsUI.class);

    public UploadsUI(final VaultUI parent) {
        super(parent);
    }

    public String getDisplayName() {
        // TODO: Use localizer
        return "Uploads";
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("upload-icon-48x48.png");
    }

    /**
     * Lists all files in the uploads directory.
     */
    @JellyAccessible
    public File[] getFiles() {
        File dir = Vault.get().getUploadsDir();
        if (dir.exists()) {
            return dir.listFiles();
        }
        return new File[0];
    }

    @StaplerAccessible
    public Object getDynamic(final String name, final StaplerRequest req, final StaplerResponse resp) {
        assert name != null;

        for (File file : getFiles()) {
            if (name.equals(file.getName())) {
                return new UploadedFileUI(this, file);
            }
        }

        // FIXME: This is not nice at all
        return null;
    }

    @StaplerAccessible
    public void doUpload(final StaplerRequest req, final StaplerResponse resp) throws Exception {
        assert req != null;

        checkPermission();

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        // Parse the request
        FileItem fileItem = (FileItem) upload.parseRequest(req).get(0);
        String fileName = Util.getFileName(fileItem.getName());

        log.info("File uploaded: {}", fileItem);

        File dir = Vault.get().getUploadsDir();
        dir.mkdirs();

        fileItem.write(new File(dir, fileName));
        fileItem.delete();

        redirectParent(req, resp);
    }
}