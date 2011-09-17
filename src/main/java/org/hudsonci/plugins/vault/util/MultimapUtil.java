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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.common.collect.Multimap;

/**
 * Helper to convert Multimap to and from text-area friendly strings.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class MultimapUtil
{
    public static void load(final Multimap<String,String> map, final Reader source) throws IOException {
        assert map != null;
        assert source != null;

        BufferedReader reader = new BufferedReader(source);
        String line;
        while ((line = reader.readLine()) != null) {
            String[] items = line.split("=", 2);
            String key = items[0].trim();
            String value = null;
            if (items.length == 2) {
                value = items[1].trim();
            }
            map.put(key, value);
        }
    }

    public static void load(final Multimap<String, String> map, final String source) {
        assert map != null;
        if (source == null || source.trim().length() == 0) {
            return;
        }

        try {
            load(map, new StringReader(source));
        }
        catch (IOException e) {
            // should never happen
        }
    }

    public static void save(final Multimap<String,String> map, final Writer target, final String sep) throws IOException {
        assert map != null;
        assert target != null;

        if (map.isEmpty()) {
            return;
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(target));

        for (String key : map.keySet()) {
            for (String value : map.get(key)) {
                writer.append(key).append('=').append(value).append(sep);
            }
        }

        writer.flush();
    }

    public static String save(final Multimap<String,String> map, final String sep) {
        assert map != null;

        StringWriter buff = new StringWriter();
        try {
            save(map, buff, sep);
        }
        catch (IOException e) {
            // should never happen
        }

        return buff.toString();
    }

    public static String save(final Multimap<String,String> map) {
        return save(map, "\n");
    }
}