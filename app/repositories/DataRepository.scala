/*
 * Copyright 2019 HM Revenue & Customs
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

package repositories

import javax.inject.{Inject, Singleton}
import models.DataModel
import play.api.libs.json.Json.JsValueWrapper
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.commands._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject()() extends MongoDbConnection {

  private lazy val repository = new DataRepositoryBase()

  def removeAll()(implicit ec: ExecutionContext): Future[WriteResult] = repository.removeAll(WriteConcern.Acknowledged)

  def removeById(url: String)(implicit ec: ExecutionContext): Future[WriteResult] = repository.remove("_id" -> url)

  def removeBySchemaId(schemaId: String)(implicit ec: ExecutionContext): Future[WriteResult] = repository.remove("schemaId" -> schemaId)

  def addEntry(document: DataModel)(implicit ec: ExecutionContext): Future[WriteResult] = repository.insert(document)

  def findById(url: String)(implicit ec: ExecutionContext): Future[DataModel] = repository.find("_id" -> url).map(_.last)

  def find(query: (String, JsValueWrapper)*)(implicit ec: ExecutionContext): Future[Option[DataModel]] =
    repository.find(query: _*).map(_.headOption)

}
