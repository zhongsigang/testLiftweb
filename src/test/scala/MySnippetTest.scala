import code.model.User
import code.session.LoggedUser
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import net.liftweb.json
import net.liftweb.mocks.MockHttpServletRequest
import net.liftweb.mockweb.MockWeb._
import net.liftweb.util.Helpers._
import org.scalatest.{FunSpec, Matchers}

import scala.xml.{Null, Text, UnprefixedAttribute}

class MySnippet {
  val userO = LoggedUser.is

  def showNames = {
    ".name *" #> LoggedUser.map(_.loginId)
  }
}

class MySnippetTest extends FunSpec with Matchers {
  override protected def withFixture(test: NoArgTest) = {
    // set up your db here
    try {
      // Following code will interfere testS, which is not stackable.
      // val session: LiftSession = new LiftSession("", StringHelpers.randomString(20), Empty)
      // S.initIfUninitted(session) { test() }
      test()
    }
    finally {
      // tear down your db here
    }
  }

  val mockLiftRules = new LiftRules()

  LiftRulesMocker.devTestLiftRulesInstance.doWith(mockLiftRules) {
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(List("test", "stateless"), _, _, _), _, _) =>
        RewriteResponse(List("stateless", "works"))
    }

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath(List("test", "stateful"), _, _, _), _, _) =>
        RewriteResponse(List("stateful", "works"))
    }

    LiftRules.early.append {
      case httpReq: HTTPRequestServlet =>
        httpReq.req match {
          case mocked: MockHttpServletRequest =>
            mocked.remoteAddr = "1.2.3.4"
          case _ => println("Not a mocked request?")
        }
      case _ => println("Not a servlet request?")
    }
  }

  describe("Testing MockWeb") {
    it("Testing liftweb snippet, which use SessionVar") {
      val xml = <xml:group>
        <div class="name">Name</div>
      </xml:group>

      val session = testS("http://foo.com/test") {
        val user = User(1, "admin", "admin")
        LoggedUser.set(Some(user))
        S.session
      }

      testS("http://foo.com/test", session) {
        val snippet = new MySnippet()
        val output = snippet.showNames(xml)

        output.text should include("admin")
      }

      testS("http://foo.com/test") {
        val snippet = new MySnippet()
        val output = snippet.showNames(xml)

        output.text should not include "admin"
      }
    }

    it("Testing a Req instance based on a String URL") {
      testReq("http://foo.com/test/this?a=b&a=c", "/test") {
        req =>
          req.uri should be("/this")
          req.params("a") should be(List("b", "c"))
      }
    }

    it("Testing a Req instance based on a MockHttpServletRequest") {
      import json.JsonDSL._
      val mockReq = new MockHttpServletRequest("http://foo.com/test/that", "/test")

      mockReq.body_=(("name" -> "joe") ~ ("age" -> 35))
      //mockReq.body = ("name" -> "joe") ~ ("age" -> 35) IntelliJ IDEA regard body as property

      //mockReq.body = JsonAST.compactRender(("name" -> "joe") ~ ("age" -> 35)).getBytes another usage
      //mockReq.contentType = "text/json"

      testReq(mockReq) {
        req =>
          req.uri should be("/that")
          req.json_? should be(true)
      }
    }

    it("Processing LiftRules.early when configured") {
      LiftRulesMocker.devTestLiftRulesInstance.doWith(mockLiftRules) {
        useLiftRules.doWith(true) {
          testReq("http://foo.com/test/this") {
            req => req.remoteAddr should be("1.2.3.4")
          }
        }
      }
    }

    it("Processing LiftRules stateless rewrites when configured") {
      LiftRulesMocker.devTestLiftRulesInstance.doWith(mockLiftRules) {
        useLiftRules.doWith(true) {
          testReq("http://foo.com/test/stateless") {
            req => req.path.partPath should be(List("stateless", "works"))
          }
        }
      }
    }

    it("Testing against S with a String URL") {
      testS("http://foo.com/test/that?a=b&b=c") {
        S.param("b") should be(Full("c"))
      }
    }

    it("Testing against S based on a MockHttpServletRequest") {
      val mockReq =
        new MockHttpServletRequest("http://foo.com/test/this?foo=bar", "/test")

      testS(mockReq) {
        S.param("foo") should be(Full("bar"))
        S.uri should be("/this")
      }
    }

    it("Processing S with stateless rewrites") {
      LiftRulesMocker.devTestLiftRulesInstance.doWith(mockLiftRules) {
        useLiftRules.doWith(true) {
          testS("http://foo.com/test/stateless") {
            S.request.foreach(_.path.partPath should be(List("stateless", "works")))
          }
        }
      }
    }

    it("Processing S with stateful rewrites") {
      LiftRulesMocker.devTestLiftRulesInstance.doWith(mockLiftRules) {
        useLiftRules.doWith(true) {
          testS("http://foo.com/test/stateful") {
            S.request.foreach(_.path.partPath should be(List("stateful", "works")))
          }
        }
      }
    }

    it("Emulating a snippet invocation") {
      testS("http://foo.com/test/stateful") {
        withSnippet("MyWidget.foo", new UnprefixedAttribute("bar", Text("bat"), Null)) {
          S.currentSnippet should be(Full("MyWidget.foo"))
          S.attr("bar") should not be Full("bat2")
          S.attr("bar") should be(Full("bat"))
        }
      }
    }

    it("Sharing a session across tests. It should work") {
      object testVar extends SessionVar[String]("Empty")

      val session = testS("http://foo.com/test") {
        testVar("Foo!")
        S.session
      }

      testS("http://foo.com/test2", session) {
        testVar.is should be("Foo!")
        testVar("Foo2!")
      }

      testS("http://foo.com/test3", session) {
        testVar.is should be("Foo2!")
      }
    }
  }
}

