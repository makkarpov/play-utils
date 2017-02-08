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

import ru.makkarpov.playutils.slickutils.EnumerationSupport.EnumerationProvider
import slick.ast.{BaseTypedType, Library, TypedType}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcType
import slick.lifted.ExtensionMethods

import scala.reflect.ClassTag

object EnumerationSupport {
  /**
    * Allows to implicitly summon enumeration instance by enumeration type
    *
    * @tparam E Enumeration type
    */
  trait EnumerationProvider[E <: Enumeration] {
    /** Enumeration instance */
    val enumeration: E

    /**
      * @param vals Enumeration `Value`s
      * @return `ValueSet`
      */
    // ValueSet constructor expects `enumeration.Value*`, but we have `E#Value*`
    def valueSet(vals: E#Value*): E#ValueSet = enumeration.ValueSet(vals.asInstanceOf[Seq[enumeration.Value]]:_*)
  }
}

trait EnumerationSupport { driver: JdbcProfile =>
  import driver.api._

  /**
    * Slick type for enumeration (represented by Int's in underlying database) and instance of
    * `EnumerationProvider`.
    *
    * @param enumeration Underlying enumeration
    * @tparam E Enumeration type
    */
  class EnumerationType[E <: Enumeration](val enumeration: E)
  extends MappedJdbcType[E#Value, Int]()(columnTypes.intJdbcType, implicitly[ClassTag[E#Value]])
  with BaseTypedType[E#Value] with EnumerationProvider[E] {
    override def map(t: E#Value): Int = t.id
    override def comap(u: Int): E#Value = enumeration(u)
  }

  trait EnumerationImplicits {
    def enumerationType[E <: Enumeration](e: E): EnumerationType[E] = new EnumerationType(e)

    implicit class EnumExtensionMethods[F <: Enumeration](val c: Rep[F#Value])(implicit tt: JdbcType[F#Value])
    extends ExtensionMethods[F#Value, F#Value] {
      override protected[this] implicit def b1Type: TypedType[F#Value] = tt
      import slick.lifted.FunctionSymbolExtensionMethods._

      def < (x: Rep[F#Value]) = Library.<.column[Boolean](n, x.toNode)
      def > (x: Rep[F#Value]) = Library.>.column[Boolean](n, x.toNode)
      def <= (x: Rep[F#Value]) = Library.<=.column[Boolean](n, x.toNode)
      def >= (x: Rep[F#Value]) = Library.>=.column[Boolean](n, x.toNode)
    }
  }
}