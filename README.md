# WAES RDC - Banking Account

## Spec
- [x] Write some code in Java to simulate a simple bank account. It should be possible to transfer and withdraw money from an account.
- [x] It is possible to pay with either debit card or credit card.
- If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra.
    - [x] Withdrawal
    - [x] Transfer
- [ ] Use design patterns where applicable and write some test cases as well.

### Requirements:
- A negative balance is not possible
  - [x] Withdrawal
  - [x] Transfer
- [x] Account should contain at least some user details, card details and current balance
- [x] One rest endpoint to see current available balance in all accounts
- [x] One rest endpoint to withdraw money
- [x] One rest endpoint to transfer money
- [x] One credit card or debit card is linked with one account
- [x] It should be able to audit transfers or withdrawals
- [x] Front end part is not required
- [x] Feel free to make some assumptions if needed & mention them in the code assignment
- [ ] Good to have: Deploy this service somewhere (AWS/Azure) or deploy locally

## Assumptions/Choices
- [x] Account can also be generated via endpoint, asking for user information on generation:
  - [x] If a BSN is non duplicated, the account will be created
  - For user details: 
    - [x] Name and Surname
    - [x] ~~Address~~
    - [x] Documents
      - [x] BSN mandatory for generation
- [x] Endpoint for account details is going to be available
- [x] Endpoint for deposits is going to be available
- [x] A Debit/Credit Card will be requested after account creation
  - For simplicity, we are not going to be PCI-DSS compliant here
    - We will save PAN and CVV on an table that is not more protected than the rest of the system
    - And there will be one endpoint that can return the credit card data for the current user

## Open Issues
- "Account should contain at least some user details, card details and current balance"
  - [x] Do we need current balance as a db field? Or can it be derived? 
    - **what ever suits you the best, for us it’s ok.**
- "A negative balance is not possible"
  - [x] Can user have debt bigger than balance by using his credit card limit?
    - **For simplicity you can assume, it’s not possible to have negative balance at all with any cards.**
- "One credit card or debit card is linked with one account"
  - [x] Can an account have both a credit and a debit card?
    - **For simplicity you can assume, it’s one card is linked with one account only.**
- For me, transfer means that the money should go from one account to another, so:
  - [x] Do we need also a deposit endpoint which allows for money to come from outside, such as ATMs? 
    - **Yes you can add one deposit endpoint.**
    - If not, this is a system that can only lower in total money value(via withdrawal).
- "If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra."
  - [x] Does every transfer/withdrawal has to be associated with a debit or credit card?
    - **Yes**
- "It is possible to pay with either debit card or credit card."
  - [x] This means as a withdrawal? Or as a different operation?
    - **Means withdrawal or transfer.**

## Not implemented
- /account/{accountId}/cards/{cardId} will not be implemented
- Address removed, on basis of priority