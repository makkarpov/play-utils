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

package ru.makkarpov.playutils

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class EnumerationSuite extends FunSuite {
  import MyDriver.api._

  implicit val langEnumType = enumerationType(LangEnum)
  type LangEnum = LangEnum.Value
  object LangEnum extends Enumeration {
    val Java = Value
    val Scala = Value
    val Groovy = Value
    val Clojure = Value
  }

  case class EnumBean(id: Long, lang: LangEnum)

  class EnumTable(t: Tag) extends Table[EnumBean](t, "enum_test") {
    def id      = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def lang    = column[LangEnum]("lang")

    def * = (id, lang) <> ((EnumBean.apply _).tupled, EnumBean.unapply)
  }

  val query = TableQuery[EnumTable]

  val testData = Seq(
    EnumBean(1, LangEnum.Java),
    EnumBean(2, LangEnum.Groovy),
    EnumBean(3, LangEnum.Scala),
    EnumBean(4, LangEnum.Clojure)
  )

  test("Enumeration support") {
    Await.result(MyDriver.database.run(
      DBIO.seq(
        query.schema.create,
        query.forceInsertAll(testData)
      ).andThen(DBIO.seq(
        sql"SELECT lang FROM enum_test WHERE id = 2".as[Int].map(x => assert(x === Vector(LangEnum.Groovy.id))),
        query.filter(_.lang === LangEnum.Scala).map(_.id).result.map(x => assert(x === List(3))),
        query.filter(_.lang >= LangEnum.Scala).map(_.id).result.map(x => assert(x == List(2, 3, 4)))
      )).andFinally(query.schema.drop).transactionally
    ), 10 seconds span)
  }
}
