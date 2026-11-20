package com.example

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CardWise AI", appName)
    }

    @Test
    fun `bundled fonts and drawables decode without corruption`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val heroBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_auth_hero_1795192703757)
        assertNotNull("Hero banner drawable must decode cleanly", heroBitmap)
        assertTrue("Hero banner width must be > 0", heroBitmap.width > 0)

        val iconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.app_icon_cardwise_1795187380889)
        assertNotNull("Launcher foreground drawable must decode cleanly", iconBitmap)
        assertTrue("Launcher icon width must be > 0", iconBitmap.width > 0)

        assertNotNull("Plus Jakarta Sans font must load", ResourcesCompat.getFont(context, R.font.plus_jakarta_sans))
        assertNotNull("Inter font must load", ResourcesCompat.getFont(context, R.font.inter))
        assertNotNull("JetBrains Mono font must load", ResourcesCompat.getFont(context, R.font.jetbrains_mono))
    }

    @Test
    fun `main activity launches without crashing`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull("MainActivity should be created", activity)
        controller.pause().stop().destroy()
    }

    @Test
    fun `room global catalog seeds worldwide banks and saves custom entries without duplicates`() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.CardWiseLocalDatabase::class.java
        ).allowMainThreadQueries().build()

        try {
            val dao = db.globalCatalogDao()
            val repo = com.example.data.GlobalCardCatalogRepository(dao = dao)
            repo.seedCatalogIfNeeded()

            val bankCount = dao.getBankCount()
            assertTrue("Room should seed at least 60 worldwide banks", bankCount >= 60)

            // Idempotent re-seed should not duplicate rows
            repo.seedCatalogIfNeeded()
            assertEquals("Re-seeding must not duplicate banks", bankCount, dao.getBankCount())

            // Save a custom bank & card locally and verify deduplication on repeat save
            val inserted = repo.saveCustomBankAndCard(
                countryCode = "IN",
                countryName = "India",
                currencySymbol = "₹",
                bankName = "My Local Cooperative Bank",
                cardName = "Platinum Rewards 2026",
                officialWebsiteUrl = "https://example.com/cards"
            )
            assertTrue("First save of custom bank/card should succeed", inserted)

            val duplicate = repo.saveCustomBankAndCard(
                countryCode = "IN",
                countryName = "India",
                currencySymbol = "₹",
                bankName = "My Local Cooperative Bank",
                cardName = "Platinum Rewards 2026 Credit Card"
            )
            assertTrue("Normalized duplicate custom card should be rejected", !duplicate)

            val banks = repo.banksFlow.first()
            val customBank = banks.firstOrNull { it.bankName == "My Local Cooperative Bank" }
            assertNotNull("Custom bank should appear in banksFlow", customBank)
            assertEquals(1, customBank?.cards?.size)
            assertEquals("Platinum Rewards 2026", customBank?.cards?.first()?.cardName)
        } finally {
            db.close()
        }
    }
}
