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

package org.hudsonci.plugins.vault.util;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.hudsonci.plugins.vault.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to build the package cache zip archive.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Archiver
{
    private static final Logger log = LoggerFactory.getLogger(Archiver.class);

    private transient Project ant;

    private Project getAnt() {
        if (ant == null) {
            ant = new org.apache.tools.ant.Project();
            ant.init();
        }
        return ant;
    }

    public File archive(final Package pkg, final File archive, final File source) throws IOException {
        assert pkg != null;

        Mkdir mkdir = new Mkdir();
        mkdir.setProject(getAnt());
        mkdir.setDir(archive.getParentFile());
        mkdir.execute();

        Zip zip = new Zip();
        zip.setProject(getAnt());
        zip.setDestFile(archive);
        zip.setUpdate(false);
        zip.setDefaultexcludes(true);
        zip.setFollowSymlinks(true);

        ZipFileSet files = new ZipFileSet();
        files.setDir(source);
        files.setIncludes(pkg.getIncludes());
        files.setExcludes(pkg.getExcludes());
        zip.addZipfileset(files);

        zip.execute();

        return archive;
    }
}