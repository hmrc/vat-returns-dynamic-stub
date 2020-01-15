/*
 * Copyright 2020 HM Revenue & Customs
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

import models.SchemaModel
import org.scalamock.handlers.{CallHandler1, CallHandler2}
import org.scalamock.scalatest.MockFactory
import reactivemongo.api.commands.{DefaultWriteResult, WriteError, WriteResult}
import repositories.SchemaRepository
import testUtils.TestSupport

import scala.concurrent.{ExecutionContext, Future}

trait MockSchemaRepository extends TestSupport with MockFactory {

  val successWriteResult = DefaultWriteResult(ok = true, n = 1, writeErrors = Seq(), None, None, None)
  val errorWriteResult = DefaultWriteResult(ok = false, n = 1, writeErrors = Seq(WriteError(1,1,"Error")), None, None, None)

  lazy val mockSchemaRepository: SchemaRepository = mock[SchemaRepository]

  def setupMockAddSchema(model: SchemaModel)(response: WriteResult): CallHandler2[SchemaModel, ExecutionContext, Future[WriteResult]] = {
    (mockSchemaRepository.addEntry(_: SchemaModel)(_: ExecutionContext))
      .expects(model, *)
      .returning(response)
  }

  def setupMockRemoveSchema(id: String)(response: WriteResult): CallHandler2[String, ExecutionContext, Future[WriteResult]] = {
    (mockSchemaRepository.removeById(_: String)(_: ExecutionContext))
      .expects(id, *)
      .returning(response)
  }

  def setupMockRemoveAllSchemas()(response: WriteResult): CallHandler1[ExecutionContext, Future[WriteResult]] = {
    (mockSchemaRepository.removeAll()(_: ExecutionContext))
      .expects(*)
      .returning(response)
  }
}
