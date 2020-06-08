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
package models

import cats.Semigroupal
import cats.data.{NonEmptyList, ValidatedNel}
import cats.instances.list._
import cats.instances.option._
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.traverse._
import enumeratum._
import io.circe.{Json, ParsingFailure}

sealed abstract class SupportedService(val awsType: String) extends EnumEntry {
  val port: Int
  val name: String
  def createCommand(json: Json): Either[NonEmptyList[PluginError], String]
}

object SupportedService extends Enum[SupportedService] with CirceEnum[SupportedService] {

  def withAwsType(awsType: String): Option[SupportedService] = values find (_.awsType == awsType)

  def portFromName(name: String): Option[Int] = values.find(_.awsType.toLowerCase.contains(name.toLowerCase)).map(_.port)

  def fromName(name: String): Option[SupportedService] = values.find(_.awsType.toLowerCase.contains(name.toLowerCase))

  case object DynamoDB extends SupportedService(awsType = "AWS::DynamoDB::Table") {

    override val port: Int = 4569

    override val name: String = "dynamodb"

    override def createCommand(json: Json): Either[NonEmptyList[PluginError], String] = {
      val propertiesJson = json.hcursor.downField("Properties").focus.get
      Semigroupal.map5[ValidatedNel[PluginError, ?], String, String, String, Option[String], String, String](
        extractTableName(propertiesJson),
        extractAttributeDefinitions(propertiesJson),
        extractKeySchema(propertiesJson),
        extractSecondaryIndexes(propertiesJson).sequence[ValidatedNel[PluginError, ?], String],
        extractProvisionedThroughput(propertiesJson)
      ) {
        case (tableName, attributeDefinitions, keySchema, gsi, provisionedThroughput) =>
          val globalSecondaryIndexes: String = gsi.fold[String]("")(g => s"""--global-secondary-indexes $g""")

          List(
            "aws --endpoint-url=http://localhost:4569 dynamodb create-table",
            s"--table-name $tableName",
            s"--attribute-definitions $attributeDefinitions",
            s"--key-schema $keySchema",
            globalSecondaryIndexes,
            s"--provisioned-throughput $provisionedThroughput"
          ).filter(!_.isEmpty).mkString(" ")
      }.toEither
    }

    private def extractTableName(json: Json): ValidatedNel[PluginError, String] =
      json.hcursor.downField("TableName")
        .focus.toValidNel(CirceError(ParsingFailure(s"Missing field TableName on $json", new IllegalStateException)))
        .andThen(_.as[String].leftMap(CirceError).toValidatedNel)

    private def extractAttributeDefinitions(json: Json): ValidatedNel[PluginError, String] =
      json.hcursor.downField("AttributeDefinitions").as[List[Json]].leftMap(CirceError).toValidatedNel.andThen { jsonList =>
        jsonList.traverse[ValidatedNel[PluginError, ?], String] { attributeDefinitionJson =>
          Semigroupal.map2[ValidatedNel[PluginError, ?], String, String, String](
            attributeDefinitionJson.hcursor.downField("AttributeName").as[String].leftMap(CirceError).toValidatedNel,
            attributeDefinitionJson.hcursor.downField("AttributeType").as[String].leftMap(CirceError).toValidatedNel) {
            case (attributeName, keyType) => s"AttributeName=$attributeName,AttributeType=$keyType"
          }
        }.map(_.mkString(" "))
      }

    private def extractKeySchema(json: Json): ValidatedNel[PluginError, String] =
      json.hcursor.downField("KeySchema").as[List[Json]].leftMap(CirceError).toValidatedNel.andThen { jsonList =>
        jsonList.traverse[ValidatedNel[PluginError, ?], String] { keySchemaJson =>
          Semigroupal.map2[ValidatedNel[PluginError, ?], String, String, String](
            keySchemaJson.hcursor.downField("AttributeName").as[String].leftMap(CirceError).toValidatedNel,
            keySchemaJson.hcursor.downField("KeyType").as[String].leftMap(CirceError).toValidatedNel) {
            case (attributeName, keyType) => s"AttributeName=$attributeName,KeyType=$keyType"
          }
        }.map(_.mkString(" "))
      }

    private def extractProvisionedThroughput(json: Json): ValidatedNel[PluginError, String] =
      Semigroupal.map2[ValidatedNel[PluginError, ?], Int, Int, String](
        json.hcursor.downField("ProvisionedThroughput").downField("ReadCapacityUnits").as[Int].leftMap(CirceError).toValidatedNel,
        json.hcursor.downField("ProvisionedThroughput").downField("WriteCapacityUnits").as[Int].leftMap(CirceError).toValidatedNel) {
        case (readCapacityUnits, writeCapacityUnits) => s"ReadCapacityUnits=$readCapacityUnits,WriteCapacityUnits=$writeCapacityUnits"
      }

    private def extractProjectionType(json: Json): ValidatedNel[PluginError, String] =
      json.hcursor.downField("Projection").downField("ProjectionType")
        .as[String].leftMap(CirceError).toValidatedNel.map { projectionType =>
          s"Projection={ProjectionType=$projectionType}"
        }

    private def extractSecondaryIndexes(json: Json): Option[ValidatedNel[PluginError, String]] =
      json.hcursor.downField("GlobalSecondaryIndexes").as[List[Json]].toOption.map { jsonList =>
        jsonList.traverse[ValidatedNel[PluginError, ?], String] { gsiJson =>
          Semigroupal.map4[ValidatedNel[PluginError, ?], String, String, String, String, String](
            gsiJson.hcursor.downField("IndexName").as[String].leftMap(CirceError).toValidatedNel,
            extractKeySchema(gsiJson),
            extractProjectionType(gsiJson),
            extractProvisionedThroughput(gsiJson)
          ) {
            case (indexName, keySchema, projectionType, provisionedThroughput) =>
              val formattedKeySchema = keySchema.split(" ").map(k => s"{$k}").mkString(",")
              s"IndexName=$indexName,KeySchema=[$formattedKeySchema],$projectionType,ProvisionedThroughput={$provisionedThroughput}"
          }
        }.map(_.mkString(" "))
      }
  }

  case object S3 extends SupportedService(awsType = "AWS::S3::Bucket") {

    override val port: Int = 4572

    override val name: String = "s3"

    override def createCommand(json: Json): Either[NonEmptyList[PluginError], String] = {
      Left(NonEmptyList.one(NotImplemented("S3 Service not implemented yet")))
    }
  }

  val values = findValues

}