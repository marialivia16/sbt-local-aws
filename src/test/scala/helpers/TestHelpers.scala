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
package helpers

import java.io.{File, FileInputStream, InputStreamReader}

import io.circe.Json
import io.circe.yaml.parser

object TestHelpers {
  val cfFile: File = new File(getClass.getResource("/cf.yml").getPath)
  val cfJson: Json = parser.parse(new InputStreamReader(new FileInputStream(cfFile))).getOrElse(Json.Null)

  val dynamoDbResourceJsonStr: String =
    """
      |{
      |  "Type" : "AWS::DynamoDB::Table",
      |  "Properties" : {
      |    "TableName" : "Example-groups-byid-CODE",
      |    "AttributeDefinitions" : [
      |      {
      |        "AttributeName" : "userId",
      |        "AttributeType" : "S"
      |      },
      |      {
      |        "AttributeName" : "groupId",
      |        "AttributeType" : "S"
      |      }
      |    ],
      |    "KeySchema" : [
      |      {
      |        "AttributeName" : "userId",
      |        "KeyType" : "HASH"
      |      },
      |      {
      |        "AttributeName" : "groupId",
      |        "KeyType" : "RANGE"
      |      }
      |    ],
      |    "ProvisionedThroughput" : {
      |      "ReadCapacityUnits" : 6,
      |      "WriteCapacityUnits" : 1
      |    },
      |    "SSESpecification" : {
      |      "SSEEnabled" : true
      |    }
      |  }
      |}
    """.stripMargin

  val dynamoDbResourceJson = parser.parse(dynamoDbResourceJsonStr).getOrElse(Json.Null)

  val dynamoDbResourceJsonStrWithSub =
    """
      | "Properties" : {
      |   "TableName" : {
      |     "Sub" : "${AppName}-users-byid-${Environment}"
      |   },
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

  val dynamoDbResourceJsonWithSub = parser.parse(dynamoDbResourceJsonStrWithSub).getOrElse(Json.Null)
}
