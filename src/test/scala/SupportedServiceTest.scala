import cats.data.NonEmptyList
import helpers.TestHelpers._
import io.circe.Json
import models.SupportedService.DynamoDB
import models.{NotImplemented, SupportedService}
import org.scalatest.{FlatSpec, Matchers}

class SupportedServiceTest extends FlatSpec with Matchers {
  "SupportedService" should "create command correctly for DynamoDb service" in {
    val expectedResult = List("aws --endpoint-url=http://localhost:4569 dynamodb create-table",
      "--table-name Example-groups-byid-CODE",
      "--attribute-definitions AttributeName=userId,AttributeType=S AttributeName=groupId,AttributeType=S",
      "--key-schema AttributeName=userId,KeyType=HASH AttributeName=groupId,KeyType=RANGE",
      "--provisioned-throughput ReadCapacityUnits=6,WriteCapacityUnits=1").mkString(" ")

    SupportedService.DynamoDB.createCommand(dynamoDbResourceJson) shouldBe Right(expectedResult)
  }

  it should "create command correctly for S3 service" in {
    SupportedService.S3.createCommand(Json.Null) shouldBe Left(NonEmptyList.one(NotImplemented("S3 Service not implemented yet")))
  }

  it should "create correct service from string" in {
    SupportedService.fromName("dynamodb") shouldBe Some(DynamoDB)
    SupportedService.fromName("DynamoDB") shouldBe Some(DynamoDB)
  }
}