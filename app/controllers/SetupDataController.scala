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

package controllers

import javax.inject.Inject

import models.DataModel
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import models.HttpMethod._
import repositories.DataRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SetupDataController @Inject()(dataRepository: DataRepository) extends BaseController {

  val addData: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[DataModel](json => json.method.toUpperCase match {
      case GET | POST => addStubDataToDB(json)
      case x => Future.successful(BadRequest(s"The method: $x is currently unsupported"))
    }
    ).recover {
      case ex => InternalServerError(s"Error Parsing Json DataModel: \n\t{$ex}")
    }
  }

  private def addStubDataToDB(json: DataModel): Future[Result] = {
    dataRepository.insert(json).map {
      case result if result.ok => Ok(s"The following JSON was added to the stub: \n\n${Json.toJson(json)}")
      case _ => InternalServerError(s"Failed to add data to Stub.")
    }
  }

  val removeData: String => Action[AnyContent] = url => Action.async { implicit request =>
    dataRepository.removeById(url).map {
      case result if result.ok => Ok("Success")
      case _ => InternalServerError("Could not delete data")
    }
  }

  val removeAll: Action[AnyContent] = Action.async { implicit request =>
    dataRepository.removeAll().map {
      case result if result.ok => Ok("Removed All Stubbed Data")
      case _ => InternalServerError("Unexpected Error Clearing MongoDB.")
    }
  }

}
