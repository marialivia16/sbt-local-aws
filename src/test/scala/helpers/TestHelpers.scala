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
