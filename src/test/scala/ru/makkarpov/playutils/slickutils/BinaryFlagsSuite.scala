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

import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class BinaryFlagsSuite extends FunSuite {
  import MyDriver.api._

  type TestFlags = TestFlags.ValueSet
  object TestFlags extends BinaryFlags {
    val Harder = Value
    val Better = Value
    val Faster = Value
    val Stronger = Value
  }

  implicit val testFlagsType = enumerationType(TestFlags)

  case class EnumBean(id: Long, flags: TestFlags)

  class EnumTable(t: Tag) extends Table[EnumBean](t, "binary_enum_test") {
    def id      = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def flags   = column[TestFlags]("flags")

    def * = (id, flags) <> ((EnumBean.apply _).tupled, EnumBean.unapply)
  }

  val query = TableQuery[EnumTable]

  val testData = Seq(
    EnumBean(1, TestFlags.empty),
    EnumBean(2, TestFlags.values),
    EnumBean(3, TestFlags.Harder),
    EnumBean(4, TestFlags.Better),
    EnumBean(5, TestFlags.Harder + TestFlags.Stronger),
    EnumBean(6, TestFlags.Faster + TestFlags.Better)
  )

  def test(f: EnumTable => Rep[Boolean], r: List[Long]) =
    query.sortBy(_.id).filter(f).map(_.id).to[List].result.map(x => assert(x === r))

  test("BinaryFlags support") {
    Await.result(MyDriver.database.run(
      DBIO.seq(
        query.schema.create,
        query.forceInsertAll(testData)
      ).andThen(DBIO.seq(
        test(_ => true, List(1, 2, 3, 4, 5, 6)),
        test(_.flags && TestFlags.Better, List(2, 4, 6)),
        test(_.flags &! TestFlags.Better, List(1, 3, 5)),
        test(_.flags && (TestFlags.Better + TestFlags.Faster), List(2, 6)),
        test(_.flags && (TestFlags.Harder + TestFlags.Faster), List(2)),
        test(_.flags &! (TestFlags.Harder + TestFlags.Faster), List(1, 4)),
        test(_.flags &? (TestFlags.Harder + TestFlags.Stronger), List(2, 3, 5)),
        test(_.flags.isEmpty, List(1)),
        test(_.flags.nonEmpty, List(2, 3, 4, 5, 6))
      )).andFinally(query.schema.drop).transactionally
    ), 10 seconds span)
  }
}
