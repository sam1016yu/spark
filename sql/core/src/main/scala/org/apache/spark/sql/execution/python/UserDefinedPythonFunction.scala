/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.python

import org.apache.spark.api.python.{PythonEvalType, PythonFunction}
import org.apache.spark.sql.Column
import org.apache.spark.sql.catalyst.expressions.{Expression, PythonUDAF, PythonUDF}
import org.apache.spark.sql.types.DataType

/**
 * A user-defined Python function. This is used by the Python API.
 */
case class UserDefinedPythonFunction(
    name: String,
    func: PythonFunction,
    dataType: DataType,
    pythonEvalType: Int,
    udfDeterministic: Boolean) {

  def builder(e: Seq[Expression]): Expression = {
    if (pythonEvalType == PythonEvalType.SQL_GROUPED_AGG_PANDAS_UDF) {
      PythonUDAF(name, func, dataType, e, udfDeterministic)
    } else {
      PythonUDF(name, func, dataType, e, pythonEvalType, udfDeterministic)
    }
  }

  /** Returns a [[Column]] that will evaluate to calling this UDF with the given input. */
  def apply(exprs: Column*): Column = {
    fromUDFExpr(builder(exprs.map(_.expr)))
  }

  /**
   * Returns a [[Column]] that will evaluate the UDF expression with the given input.
   */
  def fromUDFExpr(expr: Expression): Column = {
    expr match {
      case udaf: PythonUDAF => Column(udaf.toAggregateExpression())
      case _ => Column(expr)
    }
  }
}
