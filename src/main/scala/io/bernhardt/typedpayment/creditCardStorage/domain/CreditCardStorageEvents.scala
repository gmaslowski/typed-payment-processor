import io.bernhardt.typedpayment.Configuration.{CreditCardId, UserId}
package io.bernhardt.typedpayment.creditCardStorage.domain {

  object CreditCardStorageEvents {

    sealed trait Event

    final case class CreditCardAdded(id: CreditCardId, userId: UserId, last4Digits: String) extends Event

    case class StoredCreditCard(id: CreditCardId, userId: UserId, last4Digits: String)

  }

}

