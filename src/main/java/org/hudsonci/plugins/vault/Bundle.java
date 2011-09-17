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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A bundle is a named, optionally typed, collection of packages.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Bundle
    implements Comparable<Bundle>, Serializable
{
    private final String name;

    private String type;

    private Set<Package> packages;

    private String description;

    public Bundle(final String name) {
        this.name = name;
    }

    public Bundle(final String name, final Bundle bundle) {
        assert name != null;
        this.name = name;
        assert bundle != null;
        this.type = bundle.getType();
        this.packages = bundle.getPackages();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Set<Package> getPackages() {
        if (packages == null) {
            packages = new HashSet<Package>();
        }
        return packages;
    }

    public void setPackages(final Set<Package> packages) {
        this.packages = packages;
    }

    public Package getPackage(final String id) {
        assert id != null;
        return getPackage(UUID.fromString(id));
    }

    private Package getPackage(final UUID id) {
        assert id != null;

        if (packages != null) {
            for (Package pkg : packages) {
                if (id.equals(pkg.getId())) {
                    return pkg;
                }
            }
        }

        return null;
    }

    public void addPackage(final Package pkg) {
        assert pkg != null;
        packages.add(pkg);
    }

    public void removePackage(final Package pkg) {
        assert pkg != null;
        packages.remove(pkg);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Bundle that = (Bundle) obj;

        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public int compareTo(final Bundle bundle) {
        return name.compareTo(bundle.getName());
    }

    @Override
    public String toString() {
        return "Bundle{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", packages=" + packages +
            '}';
    }
}