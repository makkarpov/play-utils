/******************************************************************************
 * Copyright © 2017 Maxim Karpov                                              *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package ru.makkarpov.playutils.slickutils

import slick.ast.Library.SqlOperator
import slick.ast.{Library, TypedType}
import slick.jdbc.{JdbcProfile, JdbcType}
import slick.lifted.ExtensionMethods

import scala.language.implicitConversions

object BinaryFlags {
  trait BinaryFlagsSupport { driver: JdbcProfile =>
    import driver.api._

    private lazy val op_& = new SqlOperator("&")
    private lazy val op_!= = new SqlOperator("!=")
    private lazy val zero = LiteralColumn(0).toNode

    trait BinaryFlagsImplicits {
      def binaryFlagsType(flags: BinaryFlags): BaseColumnType[flags.ValueSet] =
        MappedColumnType.base[flags.ValueSet, Long](
          flags => flags.foldLeft(0L)((bits, flag) => bits | (1L << flag.id)),
          bits => flags.values.filter(flag => (bits & (1L << flag.id)) != 0)
        )

      implicit class FlagsExtensionMethods[F <: BinaryFlags](val c: Rep[F#ValueSet])(implicit tt: JdbcType[F#ValueSet])
      extends ExtensionMethods[F#ValueSet, F#ValueSet] {
        override protected[this] implicit def b1Type: TypedType[F#ValueSet] = tt
        import slick.lifted.FunctionSymbolExtensionMethods._

        @inline private def op(x: Long): Rep[Boolean] = op(x, x)

        private def op(mask: Long, target: Long, op: SqlOperator = Library.==): Rep[Boolean] =
          op.column[Boolean](op_&.column[Int](n, LiteralColumn(mask).toNode).toNode,
            if (target == 0) zero else LiteralColumn(target).toNode)

        def &&(r: F#Value) = op(1L << r.id)
        def &&(r: F#ValueSet) = op(r.foldLeft(0L)((bits, flag) => bits | (1L << flag.id)))

        def &!(r: F#Value) = op(1L << r.id, 0)
        def &!(r: F#ValueSet) = op(r.foldLeft(0L)((bits, flag) => bits | (1L << flag.id)), 0)

        def &?(r: F#ValueSet) = op(r.foldLeft(0L)((bits, flag) => bits | (1L << flag.id)), 0, op_!=)

        def nonEmpty = op_!=.column[Boolean](n, zero)
        def isEmpty = Library.==.column[Boolean](n, zero)
        def contains(r: F#Value) = &&(r)
      }
    }
  }
}

/**
  * Enumeration containing set of flags that are stored in database using `Int` or `Long`.
  *
  * Available operations:
  *
  *   column && flag      := if flag is set
  *   column &! flag      := if flag is cleared
  *
  *   column && valueSet  := if all flags are set
  *   column &! valueSet  := if all flags are cleared
  *   column &? valueSet  := if any flag is set
  *
  *   column.isEmpty          := if all flags are cleared
  *   column.nonEmpty         := if any flag is set
  *   column.contains(flag)   := alias of &&
  */
class BinaryFlags extends Enumeration {
  val empty = ValueSet()

  implicit def value2valueSet(bf: Value): ValueSet = empty + bf
}
