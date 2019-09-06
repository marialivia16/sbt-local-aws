import helpers.TestHelpers._
import io.circe.Json
import io.circe.yaml.parser
import models.Dictionary
import org.scalatest.{FlatSpec, Matchers}

class DictionaryTest extends FlatSpec with Matchers {

  val dictionary = Dictionary(Map("AppName" -> "Example", "Environment" -> "CODE"))

  "Dictionary" should "extract parameters from cf" in {
    Dictionary.loadFromJson(cfJson) shouldBe dictionary
  }

  it should "apply the dictionary in a string" in {
    val result = Dictionary.replaceInString(Json.fromString("Some-${AppName}${AppName}-in-${Environment}"), dictionary)

    result shouldBe Json.fromString("Some-ExampleExample-in-CODE")
  }

  it should "apply the dictionary in JSON" in {
    val jsonStr =
      """
        |      "Properties" : {
        |        "TableName" : {
        |          "Sub" : "${AppName}-users-byid-${Environment}"
        |        },
        |        "AttributeDefinitions" : [
        |          {
        |            "AttributeName" : "UserId",
        |            "AttributeType" : "S"
        |          },
        |          {
        |            "AttributeName" : "PermissionKey",
        |            "AttributeType" : "S"
        |          },
        |          {
        |            "AttributeName" : "PrimaryEmail",
        |            "AttributeType" : "S"
        |          }
        |        ],
        |        "KeySchema" : [
        |          {
        |            "AttributeName" : "UserId",
        |            "KeyType" : "HASH"
        |          },
        |          {
        |            "AttributeName" : "PermissionKey",
        |            "KeyType" : "RANGE"
        |          }
        |        ],
        |        "ProvisionedThroughput" : {
        |          "ReadCapacityUnits" : 1,
        |          "WriteCapacityUnits" : 1
        |        },
        |        "SSESpecification" : {
        |          "SSEEnabled" : true
        |        }
        |      }
      """.stripMargin

    val jsonStrExpected =
      """
        |      "Properties" : {
        |        "TableName" : "Example-users-byid-CODE",
        |        "AttributeDefinitions" : [
        |          {
        |            "AttributeName" : "UserId",
        |            "AttributeType" : "S"
        |          },
        |          {
        |            "AttributeName" : "PermissionKey",
        |            "AttributeType" : "S"
        |          },
        |          {
        |            "AttributeName" : "PrimaryEmail",
        |            "AttributeType" : "S"
        |          }
        |        ],
        |        "KeySchema" : [
        |          {
        |            "AttributeName" : "UserId",
        |            "KeyType" : "HASH"
        |          },
        |          {
        |            "AttributeName" : "PermissionKey",
        |            "KeyType" : "RANGE"
        |          }
        |        ],
        |        "ProvisionedThroughput" : {
        |          "ReadCapacityUnits" : 1,
        |          "WriteCapacityUnits" : 1
        |        },
        |        "SSESpecification" : {
        |          "SSEEnabled" : true
        |        }
        |      }
      """.stripMargin

    val jsonTransformed = Dictionary.applyDictionary(parser.parse(jsonStr).getOrElse(Json.Null), dictionary)

    val jsonExpected = parser.parse(jsonStrExpected).getOrElse(Json.Null)

    println(jsonTransformed == jsonExpected)

    jsonTransformed shouldBe jsonExpected
  }
}
