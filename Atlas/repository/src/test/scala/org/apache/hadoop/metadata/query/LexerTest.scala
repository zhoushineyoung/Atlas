/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.metadata.query

import org.junit.{Assert, Test}

import scala.util.parsing.input.CharArrayReader

class LexerTest {

    def scan(p: QueryParser, str: String): p.lexical.ParseResult[_] = {
        val l = p.lexical
        var s: l.Input = new CharArrayReader(str.toCharArray)
        var r = (l.whitespace.? ~ l.token)(s)
        s = r.next

        while (r.successful && !s.atEnd) {
            s = r.next
            if (!s.atEnd) {
                r = (l.whitespace.? ~ l.token)(s)
            }
        }
        r.asInstanceOf[p.lexical.ParseResult[_]]
    }

    @Test def testSimple {
        val p = new QueryParser
        val r = scan(p, """DB where db1.name""")
        Assert.assertTrue(r.successful)

    }
}
