package io.bernhardt.typedpayment.creditCardStorage.application {

  import akka.actor.typed.ActorRef
  import io.bernhardt.typedpayment.Configuration.{CreditCardId, UserId}
  import io.bernhardt.typedpayment.creditCardStorage.domain.CreditCardStorageEvents.StoredCreditCard

  object CreditCardStorageCommands {

    sealed trait Command[Reply <: CommandReply] {
      def replyTo: ActorRef[Reply]
    }

    sealed trait CommandReply

    // the protocol for adding credit cards
    final case class AddCreditCard(userId: UserId, last4Digits: String, replyTo: ActorRef[AddCreditCardResult]) extends Command[AddCreditCardResult]

    sealed trait AddCreditCardResult extends CommandReply

    case class Added(id: CreditCardId) extends AddCreditCardResult

    case object Duplicate extends AddCreditCardResult

    // the protocol for looking up credit cards by credit card id
    final case class FindById(id: CreditCardId, replyTo: ActorRef[FindCreditCardResult]) extends Command[FindCreditCardResult]

    sealed trait FindCreditCardResult extends CommandReply

    // fixme: events should be rather close to domain, right?
    case class CreditCardFound(card: StoredCreditCard) extends FindCreditCardResult

    case object CreditCardNotFound extends FindCreditCardResult

  }

}
