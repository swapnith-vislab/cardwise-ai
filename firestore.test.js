const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validCardPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 5000);
  return {
    userId: uid,
    issuer: "HDFC Bank",
    cardName: "Millennia Credit Card",
    network: "VISA",
    lastFour: "4821",
    creditLimit: 300000,
    statementBalance: 24500,
    billingDay: 15,
    dueDay: 5,
    currencySymbol: "₹",
    cardTheme: "EMERALD",
    baseRewardRatePercent: 1.0,
    rewardType: "CASHBACK",
    pointValueInCurrency: 1.0,
    aiFetched: true,
    aiSummary: "5% cashback on Amazon, Flipkart & Swiggy up to ₹1,000/month.",
    aiLastUpdatedEpochMs: Date.now() - 10000,
    sampleData: false,
    rules: [
      {
        ruleId: "rule_amz",
        title: "5% Cashback on Amazon & Flipkart",
        ruleType: "MERCHANT_OFFER",
        merchantKeywords: ["amazon", "flipkart"],
        category: "SHOPPING",
        paymentModes: ["ONLINE"],
        benefitPercent: 5.0,
        flatBenefitAmount: 0.0,
        pointsPer100: 0.0,
        minSpend: 500.0,
        monthlyCapAmount: 1000.0,
        excludedCategories: "Rent, Wallet loads, EMI",
        conditionsText: "Min order ₹500. Capped at ₹1,000/cycle.",
        validityWindow: "Ongoing",
        promoOrChannelReq: "Direct online checkout",
        aiGenerated: true,
      },
    ],
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validTransactionPayload(uid, cardId, overrides = {}) {
  const now = new Date(Date.now() - 3000);
  return {
    userId: uid,
    cardId: cardId,
    cardDisplayName: "HDFC Bank Millennia •• 4821",
    cardNetwork: "VISA",
    merchantName: "Amazon",
    itemNote: "Wireless headphones",
    category: "SHOPPING",
    paymentMode: "ONLINE",
    amount: 4000,
    currencySymbol: "₹",
    matchedRuleId: "rule_amz",
    matchedRuleTitle: "5% Cashback on Amazon & Flipkart",
    expectedBenefitAmount: 200,
    effectiveReturnPercent: 5.0,
    billingCycleKey: "2026-11",
    sampleData: false,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write cards, transactions, or meta", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("cards").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .set(validCardPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("meta")
      .doc("settings")
      .get()
  );
});

test("Owner can create, list, partially update, and delete credit cards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardsCol = aliceDb.collection("users").doc(ALICE_UID).collection("cards");

  // Empty subcollection list query succeeds
  await assertSucceeds(cardsCol.get());

  // Valid Kotlin-aligned create succeeds
  await assertSucceeds(cardsCol.doc("card_1").set(validCardPayload(ALICE_UID)));

  // Partial update: balance only
  await assertSucceeds(
    cardsCol.doc("card_1").update({
      statementBalance: 28500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: AI rules refresh
  await assertSucceeds(
    cardsCol.doc("card_1").update({
      aiFetched: true,
      aiSummary: "Updated AI summary for Millennia card.",
      aiLastUpdatedEpochMs: Date.now() - 500,
      baseRewardRatePercent: 1.0,
      rewardType: "CASHBACK",
      pointValueInCurrency: 1.0,
      rules: [],
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    cardsCol.doc("card_1").update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject Ghost Field / Shadow Update and @DocumentId `id` key
  await assertFails(
    cardsCol
      .doc("card_2")
      .set(validCardPayload(ALICE_UID, { id: "card_2", isAdmin: true }))
  );

  // Reject storing more than 4 digits in lastFour (PCI safety)
  await assertFails(
    cardsCol
      .doc("card_3")
      .set(validCardPayload(ALICE_UID, { lastFour: "4111111111111111" }))
  );

  // Reject tampered future timestamp
  await assertFails(
    cardsCol.doc("card_4").set(
      validCardPayload(ALICE_UID, {
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
    )
  );

  await assertSucceeds(cardsCol.doc("card_1").delete());
});

test("Cross-user isolation blocks Bob from accessing Alice's cards and transactions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .set(validCardPayload(ALICE_UID))
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("cards").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .update({ statementBalance: 0, updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .delete()
  );
});

test("Transactions require existing parent card and enforce bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardsCol = aliceDb.collection("users").doc(ALICE_UID).collection("cards");
  const txCol = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions");

  // Orphan transaction referencing non-existent card fails
  await assertFails(
    txCol
      .doc("tx_orphan")
      .set(validTransactionPayload(ALICE_UID, "nonexistent_card"))
  );

  // Create card first, then log purchase succeeds
  await assertSucceeds(cardsCol.doc("card_1").set(validCardPayload(ALICE_UID)));
  await assertSucceeds(
    txCol.doc("tx_1").set(validTransactionPayload(ALICE_UID, "card_1"))
  );

  // List transactions succeeds
  await assertSucceeds(txCol.get());

  // Zero/negative amount fails
  await assertFails(
    txCol
      .doc("tx_bad")
      .set(validTransactionPayload(ALICE_UID, "card_1", { amount: 0 }))
  );

  // Valid note update succeeds
  await assertSucceeds(
    txCol.doc("tx_1").update({
      itemNote: "Updated order note",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Delete transaction succeeds
  await assertSucceeds(txCol.doc("tx_1").delete());
});

test("UserMeta settings document enforces owner isolation and strict schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("meta")
      .doc("settings")
      .set({
        userId: ALICE_UID,
        sampleSeeded: true,
        preferredCurrency: "₹",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("meta")
      .doc("settings")
      .update({
        preferredCurrency: "$",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("meta")
      .doc("settings")
      .get()
  );
});
