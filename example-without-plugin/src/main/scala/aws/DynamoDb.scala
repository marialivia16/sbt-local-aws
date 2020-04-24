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
package aws

import java.net.URI

import models.{ConcertInfo, Dev, Environment}
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, PutItemRequest, PutItemResponse, ScanRequest}

import scala.collection.JavaConverters._

class DynamoDb(environment: Environment) {
  val client: DynamoDbClient = chooseClient(environment)

  private def chooseClient(environment: Environment): DynamoDbClient = environment match {
    case Dev => DynamoDbClient.builder().endpointOverride(URI.create("http://localhost:4569")).build()
    case _ => DynamoDbClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(ProfileCredentialsProvider.builder()
        .profileName("ConcertTicketsCreds")
        .build())
      .build()
  }
}

object DynamoDb {
  private val ConcertInfoTable = "ConcertTickets-SalesTable-DEV"

  def writeConcert(concertInfo: ConcertInfo)(client: DynamoDbClient): PutItemResponse = {
    val request = PutItemRequest.builder().tableName(ConcertInfoTable).item(Map(
      "ConcertId" -> AttributeValue.builder().s(concertInfo.concertId).build(),
      "ArtistId" -> AttributeValue.builder().s(concertInfo.artistId).build(),
      "TicketSales" -> AttributeValue.builder().n(concertInfo.ticketSales.toString).build()
    ).asJava).build()

    client.putItem(request)
  }

  def getAllConcertInfos(client: DynamoDbClient): List[ConcertInfo] = {
    val request = ScanRequest.builder().tableName(ConcertInfoTable).build()
    client.scan(request).items().asScala.toList.map { fields =>
      val concertId = fields.get("ConcertId").s()
      val artistId = fields.get("ArtistId").s()
      val ticketSales = fields.get("TicketSales").n().toInt
      ConcertInfo(concertId, artistId, ticketSales)
    }
  }
}
