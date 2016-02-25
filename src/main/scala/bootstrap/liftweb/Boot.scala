package bootstrap.liftweb

import java.sql.DriverManager

import code.schema.DB
import code.schema.SquerylEntrypoint._
import net.liftweb.http._
import org.squeryl.adapters.H2Adapter
import org.squeryl.{Session, SessionFactory}

class Boot {
  def boot() {
    LiftRules.addToPackages("code")

    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        DriverManager.getConnection("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1", "sa", ""),
        new H2Adapter))

    transaction {
      DB.drop
      DB.printDdl
      DB.create
    }

    LiftRules.handleMimeFile = OnDiskFileParamHolder.apply
    LiftRules.maxMimeSize = 100 * 1024 * 1024
    LiftRules.maxMimeFileSize = 100 * 1024 * 1024

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.maxConcurrentRequests.default.set((_: Req) => 6)
  }
}
