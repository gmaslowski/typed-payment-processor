package io.bernhardt.typedpayment.creditCardStorage.domain {

  import java.util.UUID

  import akka.actor.typed.scaladsl.ActorContext
  import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
  import io.bernhardt.typedpayment.Configuration.CreditCardId
  import io.bernhardt.typedpayment.creditCardStorage.application.CreditCardStorageCommands._
  import io.bernhardt.typedpayment.creditCardStorage.domain.CreditCardStorageEvents.{CreditCardAdded, Event, StoredCreditCard}

  final case class CreditCardStorageState(cards: Map[CreditCardId, StoredCreditCard] = Map.empty) {

    def applyEvent(event: Event): CreditCardStorageState = event match {
      case CreditCardAdded(id, userId, last4Digits) =>
        copy(cards = cards.updated(id, StoredCreditCard(id, userId, last4Digits)))
    }

    def applyCommand(context: ActorContext[Command[_]], cmd: Command[_]): ReplyEffect[Event, CreditCardStorageState] = cmd match {
      case AddCreditCard(userId, last4Digits, replyTo) =>
        val cardAlreadyExists = cards.values.exists(cc => cc.userId == userId && cc.last4Digits == last4Digits)
        if (cardAlreadyExists) {
          Effect.unhandled.thenRun { _: CreditCardStorageState =>
            context.log.warn("Tried adding already existing card")
          }.thenReply(replyTo)(_ => Duplicate)
        } else {
          val event = CreditCardAdded(CreditCardId(UUID.randomUUID().toString), userId, last4Digits)
          Effect
            .persist(event)
            .thenReply(replyTo)(_ => Added(event.id))
        }
      case FindById(id, replyTo) if cards.contains(id) =>
        Effect.reply(replyTo)(CreditCardFound(cards(id)))
      case FindById(id, replyTo) if !cards.contains(id) =>
        Effect.reply(replyTo)(CreditCardNotFound)
    }
  }
}
