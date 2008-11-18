/* 
 * @(#)$Id$
 *
 * Copyright (c) 2001-2008, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib.tar;

import java.util.Map;
import java.util.HashMap;

/**
 * Purely static structure defining our interface to the Tar Entry Header.
 *
 * The fields controlled here are fields for the individual tar file entries
 * in an archive.  There is no such thing as a Header Field at the top archive
 * level.
 * <P>
 * We purposefully define no variable for this list of fields, since
 * we DO NOT WANT TO access or change these values, due to application
 * goals or JVM limitations:<UL>
 *   <LI>gid
 *   <LI>uid
 *   <LI>linkname
 *   <LI>magic (UStar ID),
 *   <LI>magic version
 *   <LI>group name
 *   <LI>device major num
 *   <LI>device minor num
 * </UL>
 * Our application has no use for these, or Java has no ability to
 * work with them.
 * <P>
 * This class will be very elegant when refactored as an enum with enumMap(s).
 *
 * @author Blaine Simpson
 */
public class TarHeaderFields {
    final static int FILEPATH = 1;
    final static int FILEMODE = 2;
    final static int SIZE = 3;
    final static int MODTIME = 4;  // (File.lastModified()|*.getTime())/1000
    final static int CHECKSUM = 5;
    final static int TYPE = 6;
    // The remaining are from UStar format:
    final static int OWNERNAME = 7;
    final static int PATHPREFIX = 8;
    // Replace these contants with proper enum once we require Java 1.5.

    static Map starts = new HashMap(); // Starting positions
    static Map stops = new HashMap();  // 1 PAST last position.
           /* Note that (with one exception), there is always 1 byte
            * between a numeric field stop and the next start.  This is
            * because null byte must occupy the intervening position.
            * This is not true for non-numeric fields (which includes the * link-indicator/type-flag field, which is used as a code,
            * and is not necessarily numeric with UStar format).
            *
            * As a consequence, there may be NO DELIMITER after
            * non-numerics, which may occupy the entire field segment.
            *
            * Arg.  man page for "pax" says that both original and ustar
            * headers must be <= 100 chars. INCLUDING the trailing \0
            * character.  ???
            */

    static {
        starts.put(new Integer(FILEPATH), new Integer(0));
        stops.put(new Integer(FILEPATH), new Integer(100));
        starts.put(new Integer(FILEMODE), new Integer(100));
        stops.put(new Integer(FILEMODE), new Integer(107));
        starts.put(new Integer(SIZE), new Integer(124));
        stops.put(new Integer(SIZE), new Integer(135));
        starts.put(new Integer(MODTIME), new Integer(136));
        stops.put(new Integer(MODTIME), new Integer(147));
        starts.put(new Integer(CHECKSUM), new Integer(148)); // Special fmt.
        stops.put(new Integer(CHECKSUM), new Integer(156));  // Queer terminator.
            // Pax UStore does not follow spec and delimits this field like
            // any other numeric, skipping the space byte.
        starts.put(new Integer(TYPE), new Integer(156)); // 1-byte CODE
          // With current version, we are never doing anything with this
          // field.  In future, we will support x and/or g type here.
        stops.put(new Integer(TYPE), new Integer(157));
        starts.put(new Integer(OWNERNAME), new Integer(265));
        stops.put(new Integer(OWNERNAME), new Integer(296));
        starts.put(new Integer(PATHPREFIX), new Integer(345));
        stops.put(new Integer(PATHPREFIX), new Integer(399));
    }
    static public int getStart(int field) {
        return ((Integer) starts.get(new Integer(field))).intValue();
    }
    static public int getStop(int field) {
        return ((Integer) stops.get(new Integer(field))).intValue();
    }
}
