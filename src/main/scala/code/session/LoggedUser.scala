package code.session

import code.model.User
import net.liftweb.http.SessionVar

object LoggedUser extends SessionVar[Option[User]](None)