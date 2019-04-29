/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.old.parser.dialect.oracle.clause;

import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.old.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.old.lexer.dialect.oracle.OracleKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.clause.SQLClauseParser;
import org.apache.shardingsphere.core.parse.old.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parse.old.parser.dialect.ExpressionParserFactory;

/**
 * Select for clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleForClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public OracleForClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse for.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqualType(DefaultKeyword.FOR)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.UPDATE);
        if (lexerEngine.skipIfEqualType(DefaultKeyword.OF)) {
            do {
                basicExpressionParser.parse(selectStatement);
            } while (lexerEngine.skipIfEqualType(Symbol.COMMA));
        }
        if (lexerEngine.equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqualType(OracleKeyword.SKIP)) {
            lexerEngine.accept(OracleKeyword.LOCKED);
        }
    }
}
