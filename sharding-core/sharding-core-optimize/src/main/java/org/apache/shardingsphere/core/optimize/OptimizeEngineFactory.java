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

package org.apache.shardingsphere.core.optimize;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.optimize.engine.NewOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.encrypt.EncryptDefaultOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.encrypt.EncryptInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.insert.InsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.query.NewQueryOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.query.QueryOptimizeEngine;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.MergeStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.And;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Group;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLFunctionExector;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Optimize engine factory.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizeEngineFactory {
    
    /**
     * Create optimize engine instance.
     * 
     * @param shardingRule sharding rule
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @param generatedKey generated key
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<Object> parameters, final GeneratedKey generatedKey, final SQLFunctionExector sqlFunctionExector) {
        if (sqlStatement instanceof InsertStatement) {
            return new InsertOptimizeEngine(shardingRule, (InsertStatement) sqlStatement, parameters, generatedKey, sqlFunctionExector);
        }
        if (sqlStatement instanceof SelectStatement || sqlStatement instanceof DMLStatement) {
            return new QueryOptimizeEngine(sqlStatement.getRouteConditions().getOrCondition(), parameters, sqlFunctionExector);
        }
        // TODO do with DDL and DAL
        return new QueryOptimizeEngine(sqlStatement.getRouteConditions().getOrCondition(), parameters, sqlFunctionExector);
    }

    /**
     * Create optimize engine instance.
     *
     * @param shardingRule sharding rule
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @param generatedKey generated key
     * @return optimize engine instance
     */
    public static NewOptimizeEngine newInstance2(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<Object> parameters, final GeneratedKey generatedKey, final SQLFunctionExector sqlFunctionExector) {
//        if (sqlStatement instanceof InsertStatement) {
//            return new InsertOptimizeEngine(shardingRule, (InsertStatement) sqlStatement, parameters, generatedKey, sqlFunctionExector);
//        }
        if (sqlStatement instanceof SelectStatement || sqlStatement instanceof DMLStatement) {
            return new NewQueryOptimizeEngine(sqlStatement.getRouteCondition(), parameters, sqlFunctionExector);
        }
        // TODO do with DDL and DAL
        if (sqlStatement instanceof MergeStatement) {
            //Combine using select, mergeStatement on together.
            Group group = sqlStatement.getRouteCondition();
            if (((MergeStatement) sqlStatement).getUsingSelectStatement() != null) {
                Group innerGroup = ((MergeStatement) sqlStatement).getUsingSelectStatement().getRouteCondition();
                if (innerGroup != null && innerGroup.size() > 0) {
                    Group newGroup = new Group();
                    newGroup.add(group).add(And.instance).add(innerGroup);
                    newGroup.optimize();
                    group = newGroup;
                }
            }
            return new NewQueryOptimizeEngine(group, parameters, sqlFunctionExector);
        }
        return new NewQueryOptimizeEngine(sqlStatement.getRouteCondition(), parameters, sqlFunctionExector);
    }

    /**
     * Create encrypt optimize engine instance.
     * 
     * @param encryptRule encrypt rule
     * @param sqlStatement sql statement
     * @param parameters parameters
     * @return encrypt optimize engine instance
     */
    public static OptimizeEngine newInstance(final EncryptRule encryptRule, final SQLStatement sqlStatement, final List<Object> parameters) {
        if (sqlStatement instanceof InsertStatement) {
            return new EncryptInsertOptimizeEngine(encryptRule, (InsertStatement) sqlStatement, parameters);
        }
        return new EncryptDefaultOptimizeEngine();
    }
}
