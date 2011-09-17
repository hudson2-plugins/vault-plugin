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

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents the configuration of a set of files to be installed on a node.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Package
    implements Serializable
{
    private final UUID id;

    private String path;

    private String includes;

    private String excludes;

    private Multimap<String, String> properties;

    private String description;

    private transient File cacheFile;

    public Package() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(final String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(final String excludes) {
        this.excludes = excludes;
    }

    public Multimap<String, String> getProperties() {
        if (properties == null) {
            properties = HashMultimap.create();
        }
        return properties;
    }

    public void setProperties(final Multimap<String, String> properties) {
        this.properties = properties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(final File file) {
        this.cacheFile = file;
    }

    public boolean isCached() {
        return getCacheFile() != null && getCacheFile().exists();
    }
    
    @Override
    public String toString() {
        return "Package{" +
            "id=" + id +
            ", path='" + path + '\'' +
            ", includes='" + includes + '\'' +
            ", excludes='" + excludes + '\'' +
            ", properties=" + properties +
            ", cacheFile=" + cacheFile +
            '}';
    }
}