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
import models.NotImplemented
import org.scalatest.{FlatSpec, Matchers}

class YMLParserTest extends FlatSpec with Matchers {

  "YMLParser" should "create command" in {
    YMLParser.getAwsCommands(cfFile) shouldBe List(
      Right(
        """aws --endpoint-url=http://localhost:4569 dynamodb create-table
          |--table-name Example-users-byid-CODE
          |--attribute-definitions AttributeName=UserId,AttributeType=S AttributeName=PermissionKey,AttributeType=S AttributeName=PrimaryEmail,AttributeType=S
          |--key-schema AttributeName=UserId,KeyType=HASH AttributeName=PermissionKey,KeyType=RANGE
          |--global-secondary-indexes IndexName=UserId-index,KeySchema=[{AttributeName=UserId,KeyType=HASH}],Projection={ProjectionType=ALL},ProvisionedThroughput={ReadCapacityUnits=1,WriteCapacityUnits=1} IndexName=PrimaryEmail-index,KeySchema=[{AttributeName=PrimaryEmail,KeyType=HASH},{AttributeName=PermissionKey,KeyType=RANGE}],Projection={ProjectionType=ALL},ProvisionedThroughput={ReadCapacityUnits=1,WriteCapacityUnits=1}
          |--provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1""".stripMargin.replaceAll("\n", " ")),
      Right(
        """aws --endpoint-url=http://localhost:4569 dynamodb create-table
          |--table-name Example-groups-byid-CODE
          |--attribute-definitions AttributeName=userId,AttributeType=S AttributeName=groupId,AttributeType=S
          |--key-schema AttributeName=userId,KeyType=HASH AttributeName=groupId,KeyType=RANGE
          |--provisioned-throughput ReadCapacityUnits=6,WriteCapacityUnits=1""".stripMargin.replaceAll("\n", " ")),
      Left(NonEmptyList.fromListUnsafe(List(NotImplemented("S3 Service not implemented yet"))))
    )
  }

  it should "get resource names" in {
    YMLParser.getAwsResourcesNames(cfFile) shouldBe List("dynamodb", "s3")
  }
}
