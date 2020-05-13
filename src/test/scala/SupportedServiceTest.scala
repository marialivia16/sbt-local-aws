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