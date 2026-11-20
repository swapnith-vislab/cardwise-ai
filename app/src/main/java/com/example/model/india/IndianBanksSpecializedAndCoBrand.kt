package com.example.model.india

import com.example.model.BankCatalogEntry

/**
 * Additional Indian Urban Co-operative Banks, Regional Rural Banks (RRBs),
 * Payment/Fintech Co-Brand Programs, and Specialized Card Issuers in India.
 */
internal object IndianBanksSpecializedAndCoBrand {
    val banks: List<BankCatalogEntry> = listOf(
        // --- Leading Indian Urban Co-operative & State Banks ---
        inBank(
            bankName = "Abhyudaya Co-operative Bank",
            officialWebsiteUrl = "https://www.abhyudayabank.co.in/",
            officialDomain = "abhyudayabank.co.in",
            "Abhyudaya RuPay Select|R|G|0.75|P",
            "Abhyudaya RuPay Platinum|R|S|0.5|P",
            "Abhyudaya RuPay Classic|R|L|0.3|P",
            "Abhyudaya Secured FD RuPay|R|E|0.4|P",
            "Abhyudaya Business RuPay|R|S|0.5|P"
        ),
        inBank(
            bankName = "NKGSB Co-operative Bank",
            officialWebsiteUrl = "https://www.nkgsb-bank.com/",
            officialDomain = "nkgsb-bank.com",
            "NKGSB RuPay Select|R|G|0.75|P",
            "NKGSB RuPay Platinum|R|S|0.5|P",
            "NKGSB RuPay Classic|R|L|0.3|P",
            "NKGSB Secured FD RuPay|R|E|0.4|P",
            "NKGSB Business RuPay|R|S|0.5|P"
        ),
        inBank(
            bankName = "Bharat Co-operative Bank",
            officialWebsiteUrl = "https://www.bharatbank.com/",
            officialDomain = "bharatbank.com",
            "Bharat Bank RuPay Select|R|G|0.75|P",
            "Bharat Bank RuPay Platinum|R|S|0.5|P",
            "Bharat Bank RuPay Classic|R|L|0.3|P",
            "Bharat Bank Secured FD RuPay|R|E|0.4|P",
            "Bharat Bank Business RuPay|R|S|0.5|P"
        ),
        inBank(
            bankName = "Kalupur Commercial Co-op Bank",
            officialWebsiteUrl = "https://www.kalupurbank.com/",
            officialDomain = "kalupurbank.com",
            "Kalupur RuPay Select|R|G|0.75|P",
            "Kalupur RuPay Platinum|R|S|0.5|P",
            "Kalupur RuPay Classic|R|L|0.3|P",
            "Kalupur Secured FD RuPay|R|E|0.4|P",
            "Kalupur Business RuPay|R|S|0.5|P"
        ),
        inBank(
            bankName = "Mehsana Urban Co-op Bank",
            officialWebsiteUrl = "https://www.mucbank.com/",
            officialDomain = "mucbank.com",
            "Mehsana RuPay Select|R|G|0.75|P",
            "Mehsana RuPay Platinum|R|S|0.5|P",
            "Mehsana RuPay Classic|R|L|0.3|P",
            "Mehsana Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "AP Mahesh Co-op Urban Bank",
            officialWebsiteUrl = "https://www.apmaheshbank.com/",
            officialDomain = "apmaheshbank.com",
            "Mahesh Bank RuPay Select|R|G|0.75|P",
            "Mahesh Bank RuPay Platinum|R|S|0.5|P",
            "Mahesh Bank RuPay Classic|R|L|0.3|P",
            "Mahesh Bank Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Janata Sahakari Bank Pune",
            officialWebsiteUrl = "https://www.janatabankpune.com/",
            officialDomain = "janatabankpune.com",
            "Janata RuPay Select|R|G|0.75|P",
            "Janata RuPay Platinum|R|S|0.5|P",
            "Janata RuPay Classic|R|L|0.3|P",
            "Janata Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "GP Parsik Sahakari Bank",
            officialWebsiteUrl = "https://www.gpparsikbank.com/",
            officialDomain = "gpparsikbank.com",
            "GP Parsik RuPay Select|R|G|0.75|P",
            "GP Parsik RuPay Platinum|R|S|0.5|P",
            "GP Parsik RuPay Classic|R|L|0.3|P",
            "GP Parsik Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Bassein Catholic Co-op Bank",
            officialWebsiteUrl = "https://www.bccb.co.in/",
            officialDomain = "bccb.co.in",
            "BCCB RuPay Select|R|G|0.75|P",
            "BCCB RuPay Platinum|R|S|0.5|P",
            "BCCB RuPay Classic|R|L|0.3|P",
            "BCCB Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Rajkot Nagarik Sahakari Bank",
            officialWebsiteUrl = "https://www.rnsbindia.com/",
            officialDomain = "rnsbindia.com",
            "RNSB RuPay Select|R|G|0.75|P",
            "RNSB RuPay Platinum|R|S|0.5|P",
            "RNSB RuPay Classic|R|L|0.3|P",
            "RNSB Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Ahmedabad Mercantile Co-op Bank",
            officialWebsiteUrl = "https://www.amcobank.in/",
            officialDomain = "amcobank.in",
            "AMCO RuPay Select|R|G|0.75|P",
            "AMCO RuPay Platinum|R|S|0.5|P",
            "AMCO RuPay Classic|R|L|0.3|P",
            "AMCO Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Greater Bombay Co-op Bank",
            officialWebsiteUrl = "https://www.greaterbank.com/",
            officialDomain = "greaterbank.com",
            "Greater Bank RuPay Select|R|G|0.75|P",
            "Greater Bank RuPay Platinum|R|S|0.5|P",
            "Greater Bank RuPay Classic|R|L|0.3|P",
            "Greater Bank Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Thane Bharat Sahakari Bank",
            officialWebsiteUrl = "https://www.thanebharatbank.com/",
            officialDomain = "thanebharatbank.com",
            "TBSB RuPay Select|R|G|0.75|P",
            "TBSB RuPay Platinum|R|S|0.5|P",
            "TBSB RuPay Classic|R|L|0.3|P",
            "TBSB Secured FD RuPay|R|E|0.4|P"
        ),
        inBank(
            bankName = "Zoroastrian Co-op Bank",
            officialWebsiteUrl = "https://www.zoroastrianbank.com/",
            officialDomain = "zoroastrianbank.com",
            "ZCB RuPay Select|R|G|0.75|P",
            "ZCB RuPay Platinum|R|S|0.5|P",
            "ZCB RuPay Classic|R|L|0.3|P"
        ),
        inBank(
            bankName = "Dombivli Nagari Sahakari Bank",
            officialWebsiteUrl = "https://www.dnsbank.in/",
            officialDomain = "dnsbank.in",
            "DNS RuPay Select|R|G|0.75|P",
            "DNS RuPay Platinum|R|S|0.5|P",
            "DNS RuPay Classic|R|L|0.3|P",
            "DNS Secured FD RuPay|R|E|0.4|P"
        ),

        // --- Major Indian Regional Rural & State Co-op Banks ---
        inBank(
            bankName = "Kerala Gramin Bank",
            officialWebsiteUrl = "https://keralagbank.com/",
            officialDomain = "keralagbank.com",
            "KGB RuPay Select|R|G|0.5|P",
            "KGB RuPay Platinum|R|S|0.4|P",
            "KGB RuPay Classic|R|L|0.3|P",
            "KGB Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Karnataka Vikas Grameena Bank",
            officialWebsiteUrl = "https://www.kvgbank.com/",
            officialDomain = "kvgbank.com",
            "KVGB RuPay Select|R|G|0.5|P",
            "KVGB RuPay Platinum|R|S|0.4|P",
            "KVGB RuPay Classic|R|L|0.3|P",
            "KVGB Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Andhra Pragathi Grameena Bank",
            officialWebsiteUrl = "https://www.apgb.in/",
            officialDomain = "apgb.in",
            "APGB RuPay Select|R|G|0.5|P",
            "APGB RuPay Platinum|R|S|0.4|P",
            "APGB RuPay Classic|R|L|0.3|P",
            "APGB Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Telangana Grameena Bank",
            officialWebsiteUrl = "https://www.tgbhyd.in/",
            officialDomain = "tgbhyd.in",
            "TGB RuPay Select|R|G|0.5|P",
            "TGB RuPay Platinum|R|S|0.4|P",
            "TGB RuPay Classic|R|L|0.3|P",
            "TGB Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Baroda UP Bank",
            officialWebsiteUrl = "https://www.barodaupbank.in/",
            officialDomain = "barodaupbank.in",
            "Baroda UP RuPay Select|R|G|0.5|P",
            "Baroda UP RuPay Platinum|R|S|0.4|P",
            "Baroda UP RuPay Classic|R|L|0.3|P",
            "Baroda UP Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Prathama UP Gramin Bank",
            officialWebsiteUrl = "https://www.prathamaupbank.com/",
            officialDomain = "prathamaupbank.com",
            "Prathama RuPay Select|R|G|0.5|P",
            "Prathama RuPay Platinum|R|S|0.4|P",
            "Prathama RuPay Classic|R|L|0.3|P",
            "Prathama Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Maharashtra Gramin Bank",
            officialWebsiteUrl = "https://www.mahagramin.in/",
            officialDomain = "mahagramin.in",
            "MGB RuPay Select|R|G|0.5|P",
            "MGB RuPay Platinum|R|S|0.4|P",
            "MGB RuPay Classic|R|L|0.3|P",
            "MGB Kisan Credit RuPay|R|E|0.25|P"
        ),
        inBank(
            bankName = "Rajasthan Marudhara Gramin Bank",
            officialWebsiteUrl = "https://www.rmgb.in/",
            officialDomain = "rmgb.in",
            "RMGB RuPay Select|R|G|0.5|P",
            "RMGB RuPay Platinum|R|S|0.4|P",
            "RMGB RuPay Classic|R|L|0.3|P",
            "RMGB Kisan Credit RuPay|R|E|0.25|P"
        ),

        // --- Specialized Indian Fintech, UPI Credit & NBFC Card Products ---
        inBank(
            bankName = "Jupiter Money Cards",
            officialWebsiteUrl = "https://jupiter.money/edge-rupay-credit-card/",
            officialDomain = "jupiter.money",
            "Jupiter Edge CSB RuPay|R|C|1.0|C",
            "Jupiter Edge+ CSB RuPay|R|O|2.0|C",
            "Jupiter Pro RuPay Credit|R|S|1.5|C",
            "Jupiter Federal Edge|V|E|1.0|C"
        ),
        inBank(
            bankName = "Fi Money Cards",
            officialWebsiteUrl = "https://fi.money/features/credit-card",
            officialDomain = "fi.money",
            "Fi Amplifi Federal Visa|V|E|1.0|P",
            "Fi Simplifi Federal Visa|V|S|0.5|P",
            "Fi Infinite RuPay Credit|R|O|1.5|P",
            "Fi Zero-Forex Travel Card|V|E|1.0|P"
        ),
        inBank(
            bankName = "Scapia Travel Cards",
            officialWebsiteUrl = "https://www.scapia.cards/",
            officialDomain = "scapia.cards",
            "Scapia Federal Visa|V|O|2.0|P",
            "Scapia Federal RuPay|R|O|2.0|P",
            "Scapia Dual Network Pack|V|G|2.0|P"
        ),
        inBank(
            bankName = "Niyo Global India",
            officialWebsiteUrl = "https://goniyo.com/",
            officialDomain = "goniyo.com",
            "Niyo Global SBM Credit (FD)|V|S|1.0|P",
            "Niyo Global DCB Credit (FD)|V|E|1.0|P",
            "Niyo Global Equitas Travel|V|O|0.75|P",
            "Niyo Global Student Credit|V|L|0.5|P"
        ),
        inBank(
            bankName = "Tata Neu Cards",
            officialWebsiteUrl = "https://www.tatadigital.com/creditcard",
            officialDomain = "tatadigital.com",
            "Tata Neu Infinity HDFC RuPay|R|S|1.5|P",
            "Tata Neu Infinity HDFC Visa|V|S|1.5|P",
            "Tata Neu Plus HDFC RuPay|R|L|1.0|P",
            "Tata Neu Plus HDFC Visa|V|L|1.0|P",
            "Tata Neu Infinity SBI RuPay|R|S|1.5|P",
            "Tata Neu Plus SBI RuPay|R|L|1.0|P"
        ),
        inBank(
            bankName = "Paytm Co-Brand Cards",
            officialWebsiteUrl = "https://paytm.com/credit-cards",
            officialDomain = "paytm.com",
            "Paytm HDFC Bank Credit Card|V|S|1.0|C",
            "Paytm HDFC Bank Select|V|O|1.5|C",
            "Paytm HDFC Bank Digital RuPay|R|E|1.0|C",
            "Paytm HDFC Bank Mobile|M|L|0.5|C",
            "Paytm HDFC Bank Business|V|S|1.0|C",
            "Paytm SBI Card RuPay|R|S|1.0|C",
            "Paytm SBI Card Visa|V|S|1.0|C",
            "Paytm SBI Card SELECT|V|O|1.0|C"
        ),
        inBank(
            bankName = "PhonePe Co-Brand Cards",
            officialWebsiteUrl = "https://www.phonepe.com/",
            officialDomain = "phonepe.com",
            "PhonePe SBI Card PURPLE|R|S|1.0|P",
            "PhonePe SBI Card SELECT BLACK|R|O|1.5|P",
            "PhonePe HDFC Ultimo RuPay|R|S|1.0|P",
            "PhonePe HDFC Uno RuPay|R|E|0.5|P"
        ),
        inBank(
            bankName = "Paisabazaar Co-Brand Cards",
            officialWebsiteUrl = "https://www.paisabazaar.com/credit-card/",
            officialDomain = "paisabazaar.com",
            "Paisabazaar PaisaSave YES Bank|M|E|1.5|C",
            "Paisabazaar Duet RBL Bank|M|S|1.0|C",
            "Paisabazaar Duet Plus RBL Bank|M|O|1.0|C",
            "Paisabazaar Step UP SBM (FD)|V|E|0.5|P"
        ),
        inBank(
            bankName = "BankBazaar Co-Brand Cards",
            officialWebsiteUrl = "https://www.bankbazaar.com/credit-card.html",
            officialDomain = "bankbazaar.com",
            "BankBazaar FinBooster YES Bank|M|S|0.75|P",
            "BankBazaar SaveMax RBL Bank|M|S|0.5|P",
            "BankBazaar SaveMax Pro RBL Bank|M|O|0.75|P"
        ),
        inBank(
            bankName = "ZET & Credilio FD Cards",
            officialWebsiteUrl = "https://www.zetapp.in/",
            officialDomain = "zetapp.in",
            "ZET Magnet SBM RuPay (FD)|R|S|1.0|C",
            "ZET Magnet YES Bank RuPay|R|E|1.0|C",
            "Credilio SBM RuPay (FD)|R|S|0.75|P",
            "Rupicard SBM RuPay (FD)|R|E|0.5|P",
            "Kredit.pe SBM RuPay (FD)|R|S|1.0|C",
            "Kredit.pe YES Bank Ace|R|O|1.0|C"
        ),
        inBank(
            bankName = "POP & Rio UPI Cards",
            officialWebsiteUrl = "https://www.popclub.co/",
            officialDomain = "popclub.co",
            "POP-CLUB YES Bank RuPay|R|C|1.0|P",
            "Rio YES Bank RuPay|R|S|1.0|P",
            "Anq Phi YES Bank Mastercard|M|G|1.0|P",
            "Fibe Axis Bank RuPay|R|E|1.0|C",
            "Roarbank Unity SFB RuPay|R|C|1.0|C"
        ),
        inBank(
            bankName = "Bajaj & Tata Capital NBFC Cards",
            officialWebsiteUrl = "https://www.tatacapital.com/",
            officialDomain = "tatacapital.com",
            "Tata Capital Credit Line RuPay|R|S|0.5|P",
            "Tata Capital Select Card|V|O|1.0|P",
            "Aditya Birla Finance RuPay Card|R|C|0.75|P",
            "L&T Finance RuPay Credit Line|R|S|0.5|P",
            "Muthoot Finance RuPay Card|R|G|0.5|P",
            "Poonawalla Fincorp IndusInd eLITE|R|S|0.75|P",
            "Navi UPI Credit Line Card|R|E|0.5|C",
            "KrazyBee / KreditBee Card|R|S|0.5|C",
            "PostPe / BharatPe Unity Card|R|S|1.0|C",
            "CRED Flash / UPI Credit Line|R|O|1.0|C"
        )
    )
}
