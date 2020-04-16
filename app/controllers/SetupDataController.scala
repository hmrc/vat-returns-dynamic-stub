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

package controllers

import javax.inject.Inject
import models.DataModel
import models.HttpMethod._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.SchemaValidation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SetupDataController @Inject()(schemaValidation: SchemaValidation,
                                    dataRepository: DataRepository,
                                    cc: ControllerComponents) extends BackendController(cc) {

  val addData: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[DataModel]( json =>
      json.method.toUpperCase match {
        case GET | POST =>
          json.schemaId match {
            case Some(schemaId) =>
              schemaValidation.validateResponseJson(schemaId, json.response) flatMap {
                case true => addStubDataToDB(json)
                case false => Future.successful(BadRequest(s"Stub data response did not validate against schema: $schemaId. Stub data: ${json.response}"))
              }
            case None => addStubDataToDB(json)
          }
        case x => Future.successful(MethodNotAllowed(s"The method: $x is currently unsupported"))
      }
    ) recover {
      case ex => BadRequest(s"Error Parsing Json DataModel: \n $ex")
    }
  }

  private def addStubDataToDB(json: DataModel): Future[Result] = {
    dataRepository.addEntry(json).map {
      case result if result.ok => Ok(s"The following JSON was added to the stub: \n\n ${Json.toJson(json)}")
      case _ => InternalServerError(s"Failed to add data to Stub.")
    }
  }

  val removeDataBySchemaId: String => Action[AnyContent] = schemaId => Action.async { implicit request =>
    dataRepository.removeBySchemaId(schemaId).map {
      case result if result.ok => Ok("Success")
      case _ => InternalServerError("Could not delete data by schema id")
    }
  }

  val removeAll: Action[AnyContent] = Action.async { implicit request =>
    dataRepository.removeAll().map {
      case result if result.ok => Ok("Removed All Stubbed Data")
      case _ => InternalServerError("Unexpected Error Clearing MongoDB.")
    }
  }
}
