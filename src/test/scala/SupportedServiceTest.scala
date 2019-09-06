import io.circe.Json
import io.circe.yaml.parser
import models.SupportedService
import org.scalatest.{FlatSpec, Matchers}

class SupportedServiceTest extends FlatSpec with Matchers {
  "SupportedService - DynamoDB" should "create command correctly" in {
    val jsonStr =
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

    val json = parser.parse(jsonStr).getOrElse(Json.Null)

    SupportedService.DynamoDB.createCommand(json) shouldBe
      Right("""awslocal dynamodb create-table \
        |--table-name Example-groups-byid-CODE \
        |--attribute-definitions AttributeName=userId,AttributeType=S AttributeName=groupId,AttributeType=S \
        |--key-schema AttributeName=userId,KeyType=HASH AttributeName=groupId,KeyType=RANGE \
        |--provisioned-throughput ReadCapacityUnits=6,WriteCapacityUnits=1 \
        |--region eu-west-1""".stripMargin)
  }
}