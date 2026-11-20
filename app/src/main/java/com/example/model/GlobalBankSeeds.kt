package com.example.model

/**
 * Extended worldwide credit-card issuing banks and flagship card catalogs across
 * Europe, Middle East, Asia-Pacific, North/Latin America, and Oceania.
 * Merged with [BankCardCatalog.banks] and seeded into local Room SQLite for instant filtering.
 */
internal object GlobalBankSeeds {

    val internationalBanks: List<BankCatalogEntry> = listOf(
        // --- United Kingdom (GB) ---
        BankCatalogEntry(
            bankName = "Barclays UK",
            countryCode = "GB",
            countryName = "United Kingdom",
            officialWebsiteUrl = "https://www.barclaycard.co.uk/personal/credit-cards",
            officialDomain = "barclaycard.co.uk",
            currencySymbol = "£",
            cards = listOf(
                CreditCardCatalogItem("Barclaycard Avios Plus", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Barclaycard Avios", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Barclaycard Rewards", CardNetwork.VISA, CardTheme.EMERALD, 0.25, RewardType.CASHBACK),
                CreditCardCatalogItem("Barclaycard Platinum", CardNetwork.VISA, CardTheme.SLATE, 0.25, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "American Express UK",
            countryCode = "GB",
            countryName = "United Kingdom",
            officialWebsiteUrl = "https://www.americanexpress.com/en-gb/credit-cards/all-cards/",
            officialDomain = "americanexpress.com",
            currencySymbol = "£",
            cards = listOf(
                CreditCardCatalogItem("Preferred Rewards Gold", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("The Platinum Card UK", CardNetwork.AMEX, CardTheme.SLATE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("British Airways Premium Plus", CardNetwork.AMEX, CardTheme.OBSIDIAN, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Amex Cashback Everyday", CardNetwork.AMEX, CardTheme.EMERALD, 0.5, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "HSBC UK",
            countryCode = "GB",
            countryName = "United Kingdom",
            officialWebsiteUrl = "https://www.hsbc.co.uk/credit-cards/",
            officialDomain = "hsbc.co.uk",
            currencySymbol = "£",
            cards = listOf(
                CreditCardCatalogItem("HSBC Premier World Elite", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("HSBC Premier Credit Card", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("HSBC Rewards Credit Card", CardNetwork.VISA, CardTheme.SLATE, 0.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "NatWest",
            countryCode = "GB",
            countryName = "United Kingdom",
            officialWebsiteUrl = "https://www.natwest.com/credit-cards.html",
            officialDomain = "natwest.com",
            currencySymbol = "£",
            cards = listOf(
                CreditCardCatalogItem("NatWest Reward Credit Card", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 0.5, RewardType.CASHBACK),
                CreditCardCatalogItem("NatWest Reward Black", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("NatWest Travel Reward", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "Lloyds Bank",
            countryCode = "GB",
            countryName = "United Kingdom",
            officialWebsiteUrl = "https://www.lloydsbank.com/credit-cards.html",
            officialDomain = "lloydsbank.com",
            currencySymbol = "£",
            cards = listOf(
                CreditCardCatalogItem("Lloyds Ultra Credit Card", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Lloyds Cashback Credit Card", CardNetwork.MASTERCARD, CardTheme.SLATE, 0.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Lloyds World Elite", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),

        // --- United Arab Emirates (AE) ---
        BankCatalogEntry(
            bankName = "Emirates NBD",
            countryCode = "AE",
            countryName = "United Arab Emirates",
            officialWebsiteUrl = "https://www.emiratesnbd.com/en/cards/credit-cards",
            officialDomain = "emiratesnbd.com",
            currencySymbol = "AED",
            cards = listOf(
                CreditCardCatalogItem("Skywards Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.MILES),
                CreditCardCatalogItem("Marriott Bonvoy World Elite", CardNetwork.MASTERCARD, CardTheme.GOLD, 2.5, RewardType.POINTS),
                CreditCardCatalogItem("Duo Credit Card", CardNetwork.DINERS, CardTheme.SAPPHIRE, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("Share Visa Infinite", CardNetwork.VISA, CardTheme.EMERALD, 2.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "First Abu Dhabi Bank (FAB)",
            countryCode = "AE",
            countryName = "United Arab Emirates",
            officialWebsiteUrl = "https://www.bankfab.com/en-ae/personal/credit-cards",
            officialDomain = "bankfab.com",
            currencySymbol = "AED",
            cards = listOf(
                CreditCardCatalogItem("FAB Cashback Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("FAB Travel Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.POINTS),
                CreditCardCatalogItem("Etihad Guest Infinite", CardNetwork.VISA, CardTheme.GOLD, 2.0, RewardType.MILES),
                CreditCardCatalogItem("SHARE Standard Signature", CardNetwork.VISA, CardTheme.EMERALD, 1.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "ADCB",
            countryCode = "AE",
            countryName = "United Arab Emirates",
            officialWebsiteUrl = "https://www.adcb.com/en/personal/cards/credit-cards/",
            officialDomain = "adcb.com",
            currencySymbol = "AED",
            cards = listOf(
                CreditCardCatalogItem("ADCB 365 Cashback", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("ADCB Traveller", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("Talabat ADCB", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.25, RewardType.CASHBACK),
                CreditCardCatalogItem("TouchPoints Infinite", CardNetwork.VISA, CardTheme.GOLD, 1.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Mashreq Bank",
            countryCode = "AE",
            countryName = "United Arab Emirates",
            officialWebsiteUrl = "https://www.mashreq.com/en/uae/personal/cards/credit-cards/",
            officialDomain = "mashreq.com",
            currencySymbol = "AED",
            cards = listOf(
                CreditCardCatalogItem("Mashreq Cashback", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Mashreq Platinum Elite", CardNetwork.VISA, CardTheme.SLATE, 1.25, RewardType.POINTS),
                CreditCardCatalogItem("Solitaire Credit Card", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.POINTS)
            )
        ),

        // --- Singapore (SG) ---
        BankCatalogEntry(
            bankName = "DBS Bank",
            countryCode = "SG",
            countryName = "Singapore",
            officialWebsiteUrl = "https://www.dbs.com.sg/personal/cards/credit-cards/default.page",
            officialDomain = "dbs.com.sg",
            currencySymbol = "S$",
            cards = listOf(
                CreditCardCatalogItem("DBS Altitude Visa", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.3, RewardType.MILES),
                CreditCardCatalogItem("DBS Woman's World Card", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.2, RewardType.MILES),
                CreditCardCatalogItem("DBS Live Fresh", CardNetwork.VISA, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("POSB Everyday Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "UOB Singapore",
            countryCode = "SG",
            countryName = "Singapore",
            officialWebsiteUrl = "https://www.uob.com.sg/personal/cards/credit-cards/index.page",
            officialDomain = "uob.com.sg",
            currencySymbol = "S$",
            cards = listOf(
                CreditCardCatalogItem("UOB One Card", CardNetwork.VISA, CardTheme.SAPPHIRE, 3.33, RewardType.CASHBACK),
                CreditCardCatalogItem("UOB PRVI Miles", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.4, RewardType.MILES),
                CreditCardCatalogItem("UOB Preferred Platinum Visa", CardNetwork.VISA, CardTheme.SLATE, 1.2, RewardType.MILES),
                CreditCardCatalogItem("UOB KrisFlyer", CardNetwork.MASTERCARD, CardTheme.GOLD, 1.2, RewardType.MILES)
            )
        ),
        BankCatalogEntry(
            bankName = "OCBC Bank",
            countryCode = "SG",
            countryName = "Singapore",
            officialWebsiteUrl = "https://www.ocbc.com/personal-banking/cards",
            officialDomain = "ocbc.com",
            currencySymbol = "S$",
            cards = listOf(
                CreditCardCatalogItem("OCBC 365 Credit Card", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("OCBC 90°N Visa", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.3, RewardType.MILES),
                CreditCardCatalogItem("OCBC Rewards Card", CardNetwork.MASTERCARD, CardTheme.GOLD, 1.2, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Citi Singapore",
            countryCode = "SG",
            countryName = "Singapore",
            officialWebsiteUrl = "https://www.citibank.com.sg/credit-cards/",
            officialDomain = "citibank.com.sg",
            currencySymbol = "S$",
            cards = listOf(
                CreditCardCatalogItem("Citi PremierMiles", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.2, RewardType.MILES),
                CreditCardCatalogItem("Citi Cash Back+", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.6, RewardType.CASHBACK),
                CreditCardCatalogItem("Citi Rewards Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.2, RewardType.POINTS)
            )
        ),

        // --- Canada (CA) ---
        BankCatalogEntry(
            bankName = "American Express Canada",
            countryCode = "CA",
            countryName = "Canada",
            officialWebsiteUrl = "https://www.americanexpress.com/en-ca/credit-cards/all-cards/",
            officialDomain = "americanexpress.com",
            currencySymbol = "CA$",
            cards = listOf(
                CreditCardCatalogItem("Amex Cobalt Card", CardNetwork.AMEX, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Amex Gold Rewards Canada", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Amex Platinum Canada", CardNetwork.AMEX, CardTheme.SLATE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("SimplyCash Preferred", CardNetwork.AMEX, CardTheme.EMERALD, 2.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "RBC Royal Bank",
            countryCode = "CA",
            countryName = "Canada",
            officialWebsiteUrl = "https://www.rbcroyalbank.com/credit-cards/index.html",
            officialDomain = "rbcroyalbank.com",
            currencySymbol = "CA$",
            cards = listOf(
                CreditCardCatalogItem("RBC Avion Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("RBC Ion+ Visa", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("RBC Cash Back Preferred", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.5, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "TD Canada Trust",
            countryCode = "CA",
            countryName = "Canada",
            officialWebsiteUrl = "https://www.td.com/ca/en/personal-banking/products/credit-cards",
            officialDomain = "td.com",
            currencySymbol = "CA$",
            cards = listOf(
                CreditCardCatalogItem("TD Aeroplan Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.MILES),
                CreditCardCatalogItem("TD First Class Travel Visa Infinite", CardNetwork.VISA, CardTheme.EMERALD, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("TD Cash Back Visa Infinite", CardNetwork.VISA, CardTheme.EMERALD, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "Scotiabank",
            countryCode = "CA",
            countryName = "Canada",
            officialWebsiteUrl = "https://www.scotiabank.com/ca/en/personal/credit-cards.html",
            officialDomain = "scotiabank.com",
            currencySymbol = "CA$",
            cards = listOf(
                CreditCardCatalogItem("Scotia Momentum Visa Infinite", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Scotiabank Gold Amex", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Scotiabank Passport Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "CIBC",
            countryCode = "CA",
            countryName = "Canada",
            officialWebsiteUrl = "https://www.cibc.com/en/personal-banking/credit-cards.html",
            officialDomain = "cibc.com",
            currencySymbol = "CA$",
            cards = listOf(
                CreditCardCatalogItem("CIBC Dividend Visa Infinite", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("CIBC Aventura Visa Infinite", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("CIBC Aeroplan Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.MILES)
            )
        ),

        // --- Australia (AU) ---
        BankCatalogEntry(
            bankName = "CommBank (CBA)",
            countryCode = "AU",
            countryName = "Australia",
            officialWebsiteUrl = "https://www.commbank.com.au/credit-cards.html",
            officialDomain = "commbank.com.au",
            currencySymbol = "A$",
            cards = listOf(
                CreditCardCatalogItem("CommBank Ultimate Awards", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("CommBank Smart Awards", CardNetwork.MASTERCARD, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("CommBank Low Fee Gold", CardNetwork.MASTERCARD, CardTheme.SLATE, 0.5, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "ANZ Australia",
            countryCode = "AU",
            countryName = "Australia",
            officialWebsiteUrl = "https://www.anz.com.au/personal/credit-cards/",
            officialDomain = "anz.com.au",
            currencySymbol = "A$",
            cards = listOf(
                CreditCardCatalogItem("ANZ Frequent Flyer Black", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.25, RewardType.MILES),
                CreditCardCatalogItem("ANZ Rewards Black", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.25, RewardType.POINTS),
                CreditCardCatalogItem("ANZ Rewards Platinum", CardNetwork.VISA, CardTheme.SLATE, 0.75, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Westpac",
            countryCode = "AU",
            countryName = "Australia",
            officialWebsiteUrl = "https://www.westpac.com.au/personal-banking/credit-cards/",
            officialDomain = "westpac.com.au",
            currencySymbol = "A$",
            cards = listOf(
                CreditCardCatalogItem("Altitude Qantas Black", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.25, RewardType.MILES),
                CreditCardCatalogItem("Altitude Rewards Black", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.25, RewardType.POINTS),
                CreditCardCatalogItem("Altitude Platinum", CardNetwork.MASTERCARD, CardTheme.SLATE, 0.75, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "NAB",
            countryCode = "AU",
            countryName = "Australia",
            officialWebsiteUrl = "https://www.nab.com.au/personal/credit-cards",
            officialDomain = "nab.com.au",
            currencySymbol = "A$",
            cards = listOf(
                CreditCardCatalogItem("NAB Qantas Rewards Signature", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.25, RewardType.MILES),
                CreditCardCatalogItem("NAB Rewards Signature", CardNetwork.VISA, CardTheme.CRIMSON, 1.25, RewardType.POINTS),
                CreditCardCatalogItem("NAB Rewards Platinum", CardNetwork.VISA, CardTheme.SLATE, 0.75, RewardType.POINTS)
            )
        ),

        // --- Germany (DE) ---
        BankCatalogEntry(
            bankName = "Deutsche Bank",
            countryCode = "DE",
            countryName = "Germany",
            officialWebsiteUrl = "https://www.deutsche-bank.de/pk/konto-und-karte/karten/kreditkarten.html",
            officialDomain = "deutsche-bank.de",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Miles & More Gold", CardNetwork.MASTERCARD, CardTheme.GOLD, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Deutsche Bank MasterCard Travel", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 0.75, RewardType.POINTS),
                CreditCardCatalogItem("Deutsche Bank MasterCard Platin", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "American Express Germany",
            countryCode = "DE",
            countryName = "Germany",
            officialWebsiteUrl = "https://www.americanexpress.com/de-de/kreditkarten/",
            officialDomain = "americanexpress.com",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Amex Gold Card DE", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Amex Platinum Card DE", CardNetwork.AMEX, CardTheme.SLATE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("PAYBACK American Express", CardNetwork.AMEX, CardTheme.SAPPHIRE, 0.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Barclays Germany",
            countryCode = "DE",
            countryName = "Germany",
            officialWebsiteUrl = "https://www.barclays.de/kreditkarten/",
            officialDomain = "barclays.de",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Barclays Visa", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Barclays Platinum Double", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Eurowings Kreditkarte Premium", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.MILES)
            )
        ),

        // --- France (FR) ---
        BankCatalogEntry(
            bankName = "BNP Paribas",
            countryCode = "FR",
            countryName = "France",
            officialWebsiteUrl = "https://mabanque.bnpparibas/fr/gerer/cartes-moyens-paiement/cartes-bancaires",
            officialDomain = "mabanque.bnpparibas",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Visa Premier BNP", CardNetwork.VISA, CardTheme.GOLD, 0.75, RewardType.POINTS),
                CreditCardCatalogItem("Visa Infinite BNP", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.25, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "American Express France",
            countryCode = "FR",
            countryName = "France",
            officialWebsiteUrl = "https://www.americanexpress.com/fr-fr/carte-de-paiement/",
            officialDomain = "americanexpress.com",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Air France KLM Amex Gold", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Air France KLM Amex Platinum", CardNetwork.AMEX, CardTheme.SLATE, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Carte Gold American Express", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS)
            )
        ),

        // --- Japan (JP) ---
        BankCatalogEntry(
            bankName = "Sumitomo Mitsui Card (SMBC)",
            countryCode = "JP",
            countryName = "Japan",
            officialWebsiteUrl = "https://www.smbc-card.com/nyukai/index.jsp",
            officialDomain = "smbc-card.com",
            currencySymbol = "¥",
            cards = listOf(
                CreditCardCatalogItem("SMBC Card (NL)", CardNetwork.VISA, CardTheme.EMERALD, 0.5, RewardType.POINTS),
                CreditCardCatalogItem("SMBC Gold (NL)", CardNetwork.VISA, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("SMBC Platinum Preferred", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Olive Flexible Pay", CardNetwork.VISA, CardTheme.EMERALD, 0.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Rakuten Card",
            countryCode = "JP",
            countryName = "Japan",
            officialWebsiteUrl = "https://www.rakuten-card.co.jp/",
            officialDomain = "rakuten-card.co.jp",
            currencySymbol = "¥",
            cards = listOf(
                CreditCardCatalogItem("Rakuten Card", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Rakuten Gold Card", CardNetwork.MASTERCARD, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Rakuten Premium Card", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "JCB Card",
            countryCode = "JP",
            countryName = "Japan",
            officialWebsiteUrl = "https://www.jcb.co.jp/ordercard/",
            officialDomain = "jcb.co.jp",
            currencySymbol = "¥",
            cards = listOf(
                CreditCardCatalogItem("JCB Card W", CardNetwork.DISCOVER, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("JCB Gold", CardNetwork.DISCOVER, CardTheme.GOLD, 0.75, RewardType.POINTS),
                CreditCardCatalogItem("JCB Platinum", CardNetwork.DISCOVER, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),

        // --- Hong Kong (HK) ---
        BankCatalogEntry(
            bankName = "HSBC Hong Kong",
            countryCode = "HK",
            countryName = "Hong Kong",
            officialWebsiteUrl = "https://www.hsbc.com.hk/credit-cards/",
            officialDomain = "hsbc.com.hk",
            currencySymbol = "HK$",
            cards = listOf(
                CreditCardCatalogItem("HSBC Red Credit Card", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("HSBC Visa Signature", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.6, RewardType.POINTS),
                CreditCardCatalogItem("HSBC EveryMile", CardNetwork.VISA, CardTheme.SAPPHIRE, 2.0, RewardType.MILES)
            )
        ),
        BankCatalogEntry(
            bankName = "Standard Chartered HK",
            countryCode = "HK",
            countryName = "Hong Kong",
            officialWebsiteUrl = "https://www.sc.com/hk/credit-cards/",
            officialDomain = "sc.com",
            currencySymbol = "HK$",
            cards = listOf(
                CreditCardCatalogItem("Cathay Mastercard", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Simply Cash Visa", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Smart Card HK", CardNetwork.VISA, CardTheme.SLATE, 0.56, RewardType.CASHBACK)
            )
        ),

        // --- Malaysia (MY) ---
        BankCatalogEntry(
            bankName = "Maybank",
            countryCode = "MY",
            countryName = "Malaysia",
            officialWebsiteUrl = "https://www.maybank2u.com.my/maybank2u/malaysia/en/personal/cards/credit_cards_listing.page",
            officialDomain = "maybank2u.com.my",
            currencySymbol = "RM",
            cards = listOf(
                CreditCardCatalogItem("Maybank 2 Gold Cards", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Maybank Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Maybank Islamic Ikhwan Amex", CardNetwork.AMEX, CardTheme.EMERALD, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "CIMB Malaysia",
            countryCode = "MY",
            countryName = "Malaysia",
            officialWebsiteUrl = "https://www.cimb.com.my/en/personal/day-to-day-banking/cards/credit-cards.html",
            officialDomain = "cimb.com.my",
            currencySymbol = "RM",
            cards = listOf(
                CreditCardCatalogItem("CIMB Travel World Elite", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 2.0, RewardType.MILES),
                CreditCardCatalogItem("CIMB Cash Rebate Platinum", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("CIMB e Credit Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS)
            )
        ),

        // --- Philippines (PH) ---
        BankCatalogEntry(
            bankName = "BPI (Bank of the Philippine Islands)",
            countryCode = "PH",
            countryName = "Philippines",
            officialWebsiteUrl = "https://www.bpi.com.ph/personal/cards/credit-cards",
            officialDomain = "bpi.com.ph",
            currencySymbol = "₱",
            cards = listOf(
                CreditCardCatalogItem("BPI Visa Signature", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("BPI Amore Cashback", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("BPI Rewards Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 0.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "BDO Unibank",
            countryCode = "PH",
            countryName = "Philippines",
            officialWebsiteUrl = "https://www.bdo.com.ph/personal/cards/credit-cards",
            officialDomain = "bdo.com.ph",
            currencySymbol = "₱",
            cards = listOf(
                CreditCardCatalogItem("BDO Platinum Mastercard", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("BDO Amex Cashback", CardNetwork.AMEX, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("BDO Visa Gold", CardNetwork.VISA, CardTheme.GOLD, 0.5, RewardType.POINTS)
            )
        ),

        // --- Saudi Arabia (SA) ---
        BankCatalogEntry(
            bankName = "Al Rajhi Bank",
            countryCode = "SA",
            countryName = "Saudi Arabia",
            officialWebsiteUrl = "https://www.alrajhibank.com.sa/Personal/Cards",
            officialDomain = "alrajhibank.com.sa",
            currencySymbol = "SAR",
            cards = listOf(
                CreditCardCatalogItem("Al Rajhi Cashback Plus", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Al Rajhi Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.POINTS),
                CreditCardCatalogItem("AlFursan Infinite", CardNetwork.VISA, CardTheme.GOLD, 2.0, RewardType.MILES)
            )
        ),
        BankCatalogEntry(
            bankName = "Saudi National Bank (SNB)",
            countryCode = "SA",
            countryName = "Saudi Arabia",
            officialWebsiteUrl = "https://www.alahli.com/en-us/personal-banking/credit-cards",
            officialDomain = "alahli.com",
            currencySymbol = "SAR",
            cards = listOf(
                CreditCardCatalogItem("SNB AlFursan Visa Infinite", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.MILES),
                CreditCardCatalogItem("SNB Cashback Credit Card", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.5, RewardType.CASHBACK)
            )
        ),

        // --- South Korea (KR) ---
        BankCatalogEntry(
            bankName = "Hyundai Card",
            countryCode = "KR",
            countryName = "South Korea",
            officialWebsiteUrl = "https://www.hyundaicard.com/",
            officialDomain = "hyundaicard.com",
            currencySymbol = "₩",
            cards = listOf(
                CreditCardCatalogItem("Hyundai Card M", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("Hyundai Card ZERO Edition3", CardNetwork.MASTERCARD, CardTheme.SLATE, 0.8, RewardType.CASHBACK),
                CreditCardCatalogItem("Korean Air Card Edition2", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.5, RewardType.MILES)
            )
        ),
        BankCatalogEntry(
            bankName = "Shinhan Card",
            countryCode = "KR",
            countryName = "South Korea",
            officialWebsiteUrl = "https://www.shinhancard.com/",
            officialDomain = "shinhancard.com",
            currencySymbol = "₩",
            cards = listOf(
                CreditCardCatalogItem("Shinhan Deep Dream", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.7, RewardType.POINTS),
                CreditCardCatalogItem("Shinhan Mr. Life", CardNetwork.VISA, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Shinhan SOL Travel", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.2, RewardType.POINTS)
            )
        ),

        // --- Brazil (BR) ---
        BankCatalogEntry(
            bankName = "Nubank",
            countryCode = "BR",
            countryName = "Brazil",
            officialWebsiteUrl = "https://nubank.com.br/ultravioleta/",
            officialDomain = "nubank.com.br",
            currencySymbol = "R$",
            cards = listOf(
                CreditCardCatalogItem("Nubank Ultravioleta", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Cartão Nubank Gold", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 0.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Itaú Unibanco",
            countryCode = "BR",
            countryName = "Brazil",
            officialWebsiteUrl = "https://www.itau.com.br/cartoes",
            officialDomain = "itau.com.br",
            currencySymbol = "R$",
            cards = listOf(
                CreditCardCatalogItem("Itaú Personnalité Mastercard Black", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 2.0, RewardType.POINTS),
                CreditCardCatalogItem("Azul Itaú Visa Infinite", CardNetwork.VISA, CardTheme.SAPPHIRE, 2.5, RewardType.MILES),
                CreditCardCatalogItem("LATAM Pass Itaú Black", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 2.5, RewardType.MILES)
            )
        ),

        // --- Mexico (MX) ---
        BankCatalogEntry(
            bankName = "BBVA México",
            countryCode = "MX",
            countryName = "Mexico",
            officialWebsiteUrl = "https://www.bbva.mx/personas/productos/tarjetas-de-credito.html",
            officialDomain = "bbva.mx",
            currencySymbol = "MX$",
            cards = listOf(
                CreditCardCatalogItem("Tarjeta Azul BBVA", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.9, RewardType.POINTS),
                CreditCardCatalogItem("Tarjeta Oro BBVA", CardNetwork.VISA, CardTheme.GOLD, 1.1, RewardType.POINTS),
                CreditCardCatalogItem("Tarjeta Platinum BBVA", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Santander México",
            countryCode = "MX",
            countryName = "Mexico",
            officialWebsiteUrl = "https://www.santander.com.mx/personas/tarjetas-de-credito/",
            officialDomain = "santander.com.mx",
            currencySymbol = "MX$",
            cards = listOf(
                CreditCardCatalogItem("Santander LikeU", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Santander World Elite", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 2.0, RewardType.POINTS)
            )
        ),

        // --- Switzerland (CH) ---
        BankCatalogEntry(
            bankName = "UBS Switzerland",
            countryCode = "CH",
            countryName = "Switzerland",
            officialWebsiteUrl = "https://www.ubs.com/ch/en/private/accounts-and-cards/cards/credit-cards.html",
            officialDomain = "ubs.com",
            currencySymbol = "CHF",
            cards = listOf(
                CreditCardCatalogItem("UBS Key4 Gold", CardNetwork.VISA, CardTheme.GOLD, 0.75, RewardType.POINTS),
                CreditCardCatalogItem("UBS Platinum Card", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Swisscard",
            countryCode = "CH",
            countryName = "Switzerland",
            officialWebsiteUrl = "https://www.swisscard.ch/en/private-customers/products",
            officialDomain = "swisscard.ch",
            currencySymbol = "CHF",
            cards = listOf(
                CreditCardCatalogItem("Cashback Cards Amex", CardNetwork.AMEX, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("SWISS Miles & More Gold", CardNetwork.AMEX, CardTheme.GOLD, 1.5, RewardType.MILES),
                CreditCardCatalogItem("SWISS Miles & More Platinum", CardNetwork.AMEX, CardTheme.OBSIDIAN, 2.0, RewardType.MILES)
            )
        ),

        // --- Netherlands (NL) ---
        BankCatalogEntry(
            bankName = "American Express Netherlands",
            countryCode = "NL",
            countryName = "Netherlands",
            officialWebsiteUrl = "https://www.americanexpress.com/nl-nl/creditcards/",
            officialDomain = "americanexpress.com",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Flying Blue Amex Gold", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Flying Blue Amex Platinum", CardNetwork.AMEX, CardTheme.SLATE, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Amex Gold Card NL", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "ICS Cards (Netherlands)",
            countryCode = "NL",
            countryName = "Netherlands",
            officialWebsiteUrl = "https://www.icscards.nl/",
            officialDomain = "icscards.nl",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Visa World Card", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.5, RewardType.POINTS),
                CreditCardCatalogItem("Mastercard Gold ICS", CardNetwork.MASTERCARD, CardTheme.GOLD, 0.75, RewardType.POINTS)
            )
        ),

        // --- Ireland (IE) ---
        BankCatalogEntry(
            bankName = "Bank of Ireland",
            countryCode = "IE",
            countryName = "Ireland",
            officialWebsiteUrl = "https://personalbanking.bankofireland.com/borrow/credit-cards/",
            officialDomain = "bankofireland.com",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Aer Credit Card", CardNetwork.MASTERCARD, CardTheme.EMERALD, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Classic Credit Card", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 0.25, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "AIB (Allied Irish Banks)",
            countryCode = "IE",
            countryName = "Ireland",
            officialWebsiteUrl = "https://aib.ie/our-products/credit-cards",
            officialDomain = "aib.ie",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("AIB Platinum Visa", CardNetwork.VISA, CardTheme.OBSIDIAN, 0.5, RewardType.CASHBACK),
                CreditCardCatalogItem("AIB Click Visa", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.25, RewardType.CASHBACK)
            )
        ),

        // --- Spain (ES) ---
        BankCatalogEntry(
            bankName = "Banco Santander Spain",
            countryCode = "ES",
            countryName = "Spain",
            officialWebsiteUrl = "https://www.bancosantander.es/particulares/cuentas-tarjetas/tarjetas",
            officialDomain = "bancosantander.es",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Tarjeta Iberia Icon", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.MILES),
                CreditCardCatalogItem("Tarjeta Santander Crédito", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 0.5, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "BBVA Spain",
            countryCode = "ES",
            countryName = "Spain",
            officialWebsiteUrl = "https://www.bbva.es/personas/productos/tarjetas/tarjetas-credito.html",
            officialDomain = "bbva.es",
            currencySymbol = "€",
            cards = listOf(
                CreditCardCatalogItem("Tarjeta Aqua Crédito", CardNetwork.VISA, CardTheme.SAPPHIRE, 0.5, RewardType.POINTS),
                CreditCardCatalogItem("Tarjeta Después BBVA", CardNetwork.VISA, CardTheme.SLATE, 0.5, RewardType.POINTS)
            )
        ),

        // --- New Zealand (NZ) ---
        BankCatalogEntry(
            bankName = "ANZ New Zealand",
            countryCode = "NZ",
            countryName = "New Zealand",
            officialWebsiteUrl = "https://www.anz.co.nz/personal/credit-cards/",
            officialDomain = "anz.co.nz",
            currencySymbol = "NZ$",
            cards = listOf(
                CreditCardCatalogItem("ANZ Airpoints Visa Platinum", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.2, RewardType.MILES),
                CreditCardCatalogItem("ANZ CashBack Visa Platinum", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "ASB Bank",
            countryCode = "NZ",
            countryName = "New Zealand",
            officialWebsiteUrl = "https://www.asb.co.nz/credit-cards",
            officialDomain = "asb.co.nz",
            currencySymbol = "NZ$",
            cards = listOf(
                CreditCardCatalogItem("ASB Visa Platinum Rewards", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("ASB Visa Rewards", CardNetwork.VISA, CardTheme.GOLD, 0.67, RewardType.POINTS)
            )
        )
    )
}
