package code.schema

import code.model._
import code.schema.SquerylEntrypoint._
import org.squeryl.Schema

object DB extends Schema {
  val users = table[User]
  on(users)(u => declare(
    u.loginId is(indexed, unique)
  ))
}
