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

import com.github.tminglei.slickpg.ExPostgresDriver
import ru.makkarpov.playutils.slickutils.BinaryFlags.BinaryFlagsSupport

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by makkarpov on 31.01.17.
  */
object MyDriver extends ExPostgresDriver with EnumerationSupport with BinaryFlagsSupport with MiscSupport {
  lazy val database = {
    val drv = api.Database.forURL(
      url       = "jdbc:postgresql://localhost/play-utils-test",
      user      = "play-utils-test",
      password  = "play-utils-test",
      driver    = "org.postgresql.Driver"
    )

    // workaround for https://github.com/slick/slick/issues/1400
    import api._
    Await.ready(drv.run(sql"SELECT 1".as[Int]), 10 seconds span)

    drv
  }

  override val api = MyAPI

  object MyAPI extends API with BinaryFlagsImplicits with EnumerationImplicits with MiscImplicits {

  }
}
