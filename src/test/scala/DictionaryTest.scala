/*
 * Copyright 2019+ sbt-aws-local contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import helpers.TestHelpers._
import io.circe.Json
import io.circe.yaml.parser
import models.Dictionary
import org.scalatest.{FlatSpec, Matchers}

class DictionaryTest extends FlatSpec with Matchers {

  val dictionary = Dictionary(Map("AppName" -> "Example", "Environment" -> "CODE"))

  "Dictionary" should "extract parameters from cf" in {
    Dictionary(cfJson) shouldBe dictionary
  }

  it should "apply the dictionary in a string" in {
    val result = Dictionary.stringReplace(dictionary)("Some-${AppName}${AppName}-in-${Environment}")

    result shouldBe "Some-ExampleExample-in-CODE"
  }

  it should "apply the dictionary in JSON" in {
    val jsonStrExpected =
      """
        | "Properties" : {
        |   "TableName" : "Example-users-byid-CODE",
        |   "AttributeDefinitions" : [
        |     {
        |       "AttributeName" : "UserId",
        |       "AttributeType" : "S"
        |     },
        |     {
        |       "AttributeName" : "PermissionKey",
        |       "AttributeType" : "S"
        |     },
        |     {
        |       "AttributeName" : "PrimaryEmail",
        |       "AttributeType" : "S"
        |     }
        |   ],
        |   "KeySchema" : [
        |     {
        |       "AttributeName" : "UserId",
        |       "KeyType" : "HASH"
        |     },
        |     {
        |       "AttributeName" : "PermissionKey",
        |       "KeyType" : "RANGE"
        |     }
        |   ],
        |   "ProvisionedThroughput" : {
        |     "ReadCapacityUnits" : 1,
        |     "WriteCapacityUnits" : 1
        |   },
        |   "SSESpecification" : {
        |     "SSEEnabled" : true
        |   }
        | }
      """.stripMargin

    val jsonExpected = parser.parse(jsonStrExpected).getOrElse(Json.Null)

    Dictionary.replace(dynamoDbResourceJsonWithSub, dictionary) shouldBe jsonExpected
  }
}
