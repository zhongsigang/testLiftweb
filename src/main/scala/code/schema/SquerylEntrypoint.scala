package code.schema

import java.sql.Timestamp

import org.joda.time.DateTime
import org.squeryl.dsl._
import org.squeryl.{KeyedEntity, KeyedEntityDef, PrimitiveTypeMode}

object SquerylEntrypoint extends PrimitiveTypeMode {

  implicit val jodaTimeTEF = new NonPrimitiveJdbcMapper[Timestamp, DateTime, TTimestamp](timestampTEF, this) {
    def convertFromJdbc(t: Timestamp) = new DateTime(t)

    def convertToJdbc(t: DateTime) = new Timestamp(t.getMillis)
  }

  implicit val optionJodaTimeTEF =
    new TypedExpressionFactory[Option[DateTime], TOptionTimestamp]
      with DeOptionizer[Timestamp, DateTime, TTimestamp, Option[DateTime], TOptionTimestamp] {

      val deOptionizer = jodaTimeTEF
    }

  implicit def jodaTimeToTE(s: DateTime): TypedExpression[DateTime, TTimestamp] = jodaTimeTEF.create(s)

  implicit def optionJodaTimeToTE(s: Option[DateTime]): TypedExpression[Option[DateTime], TOptionTimestamp] = optionJodaTimeTEF.create(s)

  override implicit def kedForKeyedEntities[A, K](implicit ev: A <:< KeyedEntity[K], m: Manifest[A]): KeyedEntityDef[A, K] = new KeyedEntityDef[A, K] {
    def getId(a: A) = a.id

    def isPersisted(a: A) = a.isPersisted

    def idPropertyName = "id"
  }

}
