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

import javax.inject.{Inject, Singleton}
import models.SchemaModel
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.SchemaRepository
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SetupSchemaController @Inject()(schemaRepository: SchemaRepository, cc: ControllerComponents
                                     ) extends BackendController(cc) {

  val addSchema: Action[JsValue] = Action.async(parse.json) {
    implicit request => withJsonBody[SchemaModel] {
      json =>
        schemaRepository.addEntry(json) map ( result =>
          if(result.ok) {
            Ok(s"Successfully added Schema: ${request.body}")
          } else {
            InternalServerError(s"Could not store schema: ${json._id}")
          }
        )
      } recover {
      case _ => BadRequest("Error Parsing Json SchemaModel")
    }
  }

  val removeSchema: String => Action[AnyContent] = id => Action.async {
    implicit request =>
      schemaRepository.removeById(id).map ( result =>
        if (result.ok) {
          Ok("Success")
        } else {
          InternalServerError(s"Could not delete schema: $id")
        }
      )
  }

  val removeAllSchemas: Action[AnyContent] = Action.async {
    implicit request =>
      schemaRepository.removeAll().map(_.ok match {
        case true => Ok("Removed All Schemas")
        case _ => InternalServerError("Unable to remove schemas")
      })
  }
}
