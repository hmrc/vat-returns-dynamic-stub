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

package utils

import com.github.fge.jsonschema.main.JsonSchema
import mocks.MockSchemaRepository
import models.SchemaModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import play.api.libs.json.Json
import repositories.{DynamicStubRepository, SchemaRepository}
import testUtils.TestSupport

import scala.concurrent.Future

class SchemaValidationSpec2 extends TestSupport with MockSchemaRepository {

//  def setupMocks(schemaModel: SchemaModel): SchemaValidation = {
////    val mockCollection = app.injector.instanceOf[DynamicStubRepository[SchemaModel, String]]
//    val mockConnection = mock[SchemaRepository]
//
//    when(mockConnection.findById("testSchema")).thenReturn(schemaModel)
//
////    when(mockCollection.findById(ArgumentMatchers.eq(schemaModel._id))(ArgumentMatchers.any()))
////      .thenReturn(Future.successful(schemaModel))
////
////    setupMockAddSchema(schemaModel)(successWriteResult)
//
//    new SchemaValidation(mockConnection)
//  }

  def setup(schemaModel: SchemaModel): SchemaValidation = {
    val mockConnection = mockSchemaRepository
    //when(mockSchemaRepository.findById("testSchema")).thenReturn(Future(schemaModel))
    when(mockSchemaRepository.findById(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(schemaModel))
    new SchemaValidation(mockSchemaRepository)
  }



  val schema = Json.parse("""{
                            	"title": "Person",
                            	"type": "object",
                            	"properties": {
                            		"firstName": {
                            			"type": "string"
                            		},
                            		"lastName": {
                            			"type": "string"
                            		}
                            	},
                            	"required": ["firstName", "lastName"]
                            }""")

  "Calling .loadResponseSchema" should {

    "with a matching schema in mongo" should {
      val schemaModel = SchemaModel("testSchema","/test","GET", responseSchema = schema)
      lazy val validation = setup(schemaModel)
//        lazy val validation = setupMocks(SchemaModel("testSchema","/test","GET", responseSchema = schema))

      "return a json schema" in {
        lazy val result = validation.loadResponseSchema("testSchema")
        await(result).isInstanceOf[JsonSchema]
      }
    }

//    "without a matching schema in mongo" should {
//
//      "throw an exception" in {
//        val validation = setupFutureFailedMocks(SchemaModel("testSchema","/test","GET", responseSchema = schema))
//
//        val ex = intercept[RuntimeException] {
//          await(validation.loadResponseSchema("testSchema"))
//        }
//        ex.getMessage shouldEqual "Schema could not be retrieved/found in MongoDB"
//      }
//    }
  }
}
