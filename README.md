# WAES RDC - Banking Account

## Spec
Write some code in Java to simulate a simple bank account. It should be possible to transfer and withdraw money from an account.
It is possible to pay with either debit card or credit card.
If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra.
Use design patterns where applicable and write some test cases as well.

### Requirements:
- [ ] A negative balance is not possible
- [ ] Account should contain at least some user details, card details and current balance
- [ ] One rest endpoint to see current available balance in all accounts
- [ ] One rest endpoint to withdraw money
- [ ] One rest endpoint to transfer money
- [ ] One credit card or debit card is linked with one account
- [ ] It should be able to audit transfers or withdrawals
- [ ] Front end part is not required
- [ ] Feel free to make some assumptions if needed & mention them in the code assignment
- [ ] Good to have: Deploy this service somewhere (AWS/Azure) or deploy locally

## Assumptions/Choices
- [ ] Account can also be generated via endpoint, asking for user information on generation:
  - [ ] If a BSN is non duplicated, the account will be created
  - For user details: 
    - [x] Name and Surname
    - [ ] Address
    - [x] Documents
      - [ ] BSN mandatory for generation
- [ ] Endpoint for account details is going to be available
- [ ] A Debit/Credit Card will be requested after account creation

## Open Issues
- "Account should contain at least some user details, card details and current balance"
  - [ ] Do we need current balance as a db field? Or can it be derived?
- "A negative balance is not possible"
  - [ ] Can user have debt bigger than balance by using his credit card limit?
- "One credit card or debit card is linked with one account"
  - [ ] Can an account have both a credit and a debit card?
- For me, transfer means that the money should go from one account to another, so:
  - [ ] Do we need also a deposit endpoint which allows for money to come from outside, such as ATMs? 
    - If not, this is a system that can only lower in total money value(via withdrawal).
- "If a transfer/withdraw is done with a credit card, 1% of the amount is charged extra."
  - [ ] Does every transfer/withdrawal has to be associated with a debit or credit card?
- "It is possible to pay with either debit card or credit card."
  - [ ] This means as a withdrawal? Or as a different operation?