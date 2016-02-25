package code.model

import org.squeryl.KeyedEntity

case class User(id: Long = 0,
                var loginId: String,
                var password: String
               ) extends KeyedEntity[Long]
