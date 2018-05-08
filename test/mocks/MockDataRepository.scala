/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mocks

import models.DataModel
import org.scalamock.handlers.{CallHandler1, CallHandler2}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json.JsValueWrapper
import reactivemongo.api.commands.{DefaultWriteResult, WriteError, WriteResult}
import repositories.DataRepository
import testUtils.TestSupport

import scala.concurrent.{ExecutionContext, Future}

trait MockDataRepository extends TestSupport with MockFactory {

  val successWriteResult = DefaultWriteResult(ok = true, n = 1, writeErrors = Seq(), None, None, None)
  val errorWriteResult = DefaultWriteResult(ok = false, n = 1, writeErrors = Seq(WriteError(1,1,"Error")), None, None, None)

  lazy val mockDataRepository: DataRepository = mock[DataRepository]

  def mockAddEntry(document: DataModel)(response: WriteResult): CallHandler2[DataModel, ExecutionContext, Future[WriteResult]] = {
    (mockDataRepository.insert(_: DataModel)(_: ExecutionContext))
      .expects(document, *)
      .returning(response)
  }

  def mockRemoveById(url: String)(response: WriteResult): CallHandler2[String, ExecutionContext, Future[WriteResult]] = {
    (mockDataRepository.removeById(_: String)(_: ExecutionContext))
      .expects(url, *)
      .returning(response)
  }

  def mockRemoveAll()(response: WriteResult): CallHandler1[ExecutionContext, Future[WriteResult]] = {
    (mockDataRepository.removeAll()(_: ExecutionContext))
      .expects(*)
      .returning(response)
  }

  def mockFind(response: List[DataModel]): CallHandler2[(String, JsValueWrapper), ExecutionContext, Future[List[DataModel]]] = {
    (mockDataRepository.find(_: (String, JsValueWrapper))(_: ExecutionContext))
      .expects(*,*)
      .returning(response)
  }

}
