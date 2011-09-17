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

package org.hudsonci.plugins.vault.install;

import groovy.lang.GroovyShell;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Hudson;
import hudson.remoting.DelegatingCallable;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.hudsonci.plugins.vault.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute <tt>install.groovy</tt> script on installation if the file exists.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class InstallScriptExecutor
    implements PackageInstallListener
{
    private static final Logger log = LoggerFactory.getLogger(InstallScriptExecutor.class);

    public void installed(final Package pkg, final FilePath location, final TaskListener listener)
        throws Exception
    {
        // Run an optional installation script
        FilePath script = location.child("install.groovy");
        if (script.exists()) {
            String msg = String.format("Executing installation script: %s", script);
            log.info(msg);
            listener.getLogger().println(msg);

            ExecuteScript task = new ExecuteScript(script);
            String result = location.getChannel().call(task);

            log.info("Script result:\n{}", result);
            listener.getLogger().println(result);
        }
    }

    /**
     * Execute a script on a remote node.
     */
    private static final class ExecuteScript
        implements DelegatingCallable<String,RuntimeException>
    {
        private final FilePath script;

        private transient ClassLoader cl;

        private ExecuteScript(final FilePath script) {
            this.script = script;
            this.cl = getClassLoader();
        }

        public ClassLoader getClassLoader() {
            return Hudson.getInstance().getPluginManager().uberClassLoader;
        }

        public String call() throws RuntimeException {
            // if we run locally, cl!=null. Otherwise the delegating classloader will be available as context classloader.
            if (cl==null) {
                cl = Thread.currentThread().getContextClassLoader();
            }

            GroovyShell shell = new GroovyShell(cl);

            File file = new File(script.getRemote());

            File baseDir = file.getParentFile();
            shell.setVariable("baseDir", baseDir);
            shell.setVariable("basedir", baseDir);

            // FIXME: This will not work due to: http://issues.hudson-ci.org/browse/HUDSON-2259, cant use Ant :-(
//            AntBuilder ant = new AntBuilder();
//            ant.getAntProject().setBaseDir(baseDir);
//            shell.setVariable("ant", ant);

            StringWriter buff = new StringWriter();
            PrintWriter out = new PrintWriter(buff);
            shell.setVariable("out", out);

            try {
                Object output = shell.evaluate(file);
                if (output != null) {
                    out.printf("Result: %s%n", output);
                }
            }
            catch (Throwable t) {
                t.printStackTrace(out);
            }

            return buff.toString();
        }
    }
}