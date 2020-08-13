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
import play.api.libs.json.Json
import testUtils.TestSupport

class SchemaValidationSpec2 extends TestSupport with MockSchemaRepository {

  object TestSchemaValidation extends SchemaValidation(mockSchemaRepository)

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
      lazy val result = TestSchemaValidation.loadResponseSchema("testSchema")

      "return a json schema" in {
        setUpMockFindById("testSchema")(schemaModel)
        await(result).isInstanceOf[JsonSchema]
      }
    }
  }

  "Calling .validateResponseJson" should {

    "with a valid json body" should {

      "return true" in {
        val schemaModel = SchemaModel("testSchema","/test","GET", responseSchema = schema)

        setUpMockFindById("testSchema")(schemaModel)
        val json = Json.parse("""{ "firstName" : "Bob", "lastName" : "Bobson" }""")
        val result = TestSchemaValidation.validateResponseJson("testSchema", Some(json))


        await(result) shouldEqual true
      }
    }

    "with an invalid json body" should {
      "return false" in {
        val schemaModel = SchemaModel("testSchema","/test","GET", responseSchema = schema)

        setUpMockFindById("testSchema")(schemaModel)
        val json = Json.parse("""{ "firstName" : "Bob" }""")

        lazy val result = TestSchemaValidation.validateResponseJson("testSchema", Some(json))
        await(result) shouldEqual false
      }
    }
  }
}
