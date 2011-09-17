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

import hudson.model.ManagementLink;

import javax.inject.Named;
import javax.inject.Singleton;

import org.kohsuke.stapler.StaplerFallback;

/**
 * Provides the <tt>vault</tt> management link.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@Named
@Singleton
public class VaultLink
    extends ManagementLink
    implements StaplerFallback
{
    @Override
    public String getUrlName() {
        return "vault";
    }

    public String getDisplayName() {
        // TODO: Use localizer
        return "Vault";
    }

    @Override
    public String getDescription() {
        // TODO: Use localizer
        return "Manage files in the vault.";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/vault/images/safe-icon-48x48.png";
    }

    public Object getStaplerFallback() {
        return new VaultUI(this);
    }
}
