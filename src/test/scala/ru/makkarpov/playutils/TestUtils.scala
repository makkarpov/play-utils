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

import java.util.concurrent.Executors

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

object TestUtils {
  trait DatabaseProvider { driver: JdbcProfile =>
    import driver.api._

    lazy val database = Database.forURL(
      url       = "jdbc:postgresql://localhost/play-utils-test",
      user      = "play-utils-test",
      password  = "play-utils-test",
      driver    = "org.postgresql.Driver"
    )
  }

  implicit val testExecContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
}