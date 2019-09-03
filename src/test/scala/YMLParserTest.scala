import helpers.TestHelpers._
import org.scalatest.{FlatSpec, Matchers}

class YMLParserTest extends FlatSpec with Matchers {

  "YMLParser" should "create command" in {
    YMLParser.getAWSServices(cfFile) shouldBe ""
  }
}
