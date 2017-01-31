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

class MiscSuite extends FunSuite {
  import MyDriver.api._

  case class TestBean(id: Long, str: String)

  class TestTable(t: Tag) extends Table[TestBean](t, "test") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def str = column[String]("str")

    def * = (id, str) <> ((TestBean.apply _).tupled, TestBean.unapply)
  }

  val testQuery = TableQuery[TestTable]

  val testData = Seq(
    TestBean(1, "scala"),
    TestBean(2, "java"),
    TestBean(3, "groovy")
  )

  test("resultFirst should work") {
    Await.result(MyDriver.database.run(
      DBIO.seq(
        testQuery.schema.create,
        testQuery.forceInsertAll(testData)
      ).andThen(DBIO.seq(
        testQuery.sortBy(_.id).filter(_.id > 1L).map(_.id).resultFirst.map(x => assert(x === Some(2))),
        testQuery.filter(_.id === 100L).map(_.id).resultFirst.map(x => assert(x === None))
      )).andFinally(testQuery.schema.drop).transactionally
    ), 10 seconds span)
  }
}
