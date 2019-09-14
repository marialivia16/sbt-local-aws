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
    case Dev => DynamoDbClient.builder().endpointOverride(URI.create("localhost:4569")).build()
    case _ => DynamoDbClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(ProfileCredentialsProvider.builder()
        .profileName("ConcertTicketsCreds")
        .build())
      .build()
  }
}

object DynamoDb {
  private val ConcertInfoTable = "ConcertTable"

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
    val response = client.scan(request).items().asScala.map { fields =>
      val concertId = fields.get("ConcertId").s()
      val artistId = fields.get("ArtistId").s()
      val ticketSales = fields.get("TicketSales").n().toInt
      ConcertInfo(concertId, artistId, ticketSales)
    }



  }
}
