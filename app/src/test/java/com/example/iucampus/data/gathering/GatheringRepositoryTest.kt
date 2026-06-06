package com.example.iucampus.data.gathering

import android.content.Context
import android.content.res.AssetManager
import com.example.iucampus.data.auth.SessionManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class GatheringRepositoryTest {

    private lateinit var repository: GatheringRepository
    private val context = mockk<Context>()
    private val assets = mockk<AssetManager>()

    private val mockJson = """
        [
          {
            "id": "G1",
            "name": "Small Gathering",
            "description": "Desc 1",
            "organizer": "Alice",
            "location": "Loc 1",
            "time": "Time 1",
            "max_participants": 2,
            "current_participants": 1
          }
        ]
    """.trimIndent()

    @Before
    fun setUp() {
        every { context.assets } returns assets
        every { assets.open("gatherings.json") } returns ByteArrayInputStream(mockJson.toByteArray())
        
        SessionManager.currentUsername = "Bob"
        repository = GatheringRepository(context)
        
        // Manual state reset for companion object
        runBlocking {
            val gatherings = repository.getGatherings().first()
            gatherings.filter { it.organizer == "Bob" }.forEach { repository.deleteGathering(it.id) }
        }
    }

    @Test
    fun `test joinGathering respects maxParticipants`() = runBlocking {
        val gatheringId = "G1"
        
        // 1st join effort (current was 1, max is 2)
        val joinedFirst = repository.joinGathering(gatheringId)
        assertTrue("Bob should be able to join", joinedFirst)
        
        val gatheringAfterFirst = repository.getGatheringById(gatheringId)
        assertEquals(2, gatheringAfterFirst?.currentParticipants)

        // 2nd join effort (already at max 2)
        val joinedSecond = repository.joinGathering(gatheringId)
        assertFalse("Should not be able to join (already at max)", joinedSecond)
        
        val gatheringAfterSecond = repository.getGatheringById(gatheringId)
        assertEquals(2, gatheringAfterSecond?.currentParticipants)
    }

    @Test
    fun `test organizer cannot withdraw from own gathering`() = runBlocking {
        // Alice is organizer of G1. Bob is current user.
        // Let Bob create a gathering (so he's organizer)
        repository.addGathering("My Event", "Desc", "Loc", "Time", 10)
        val myEvent = repository.getGatherings().first().find { it.name == "My Event" }
        assertNotNull(myEvent)
        assertEquals("Bob", myEvent?.organizer)

        // Bob tries to withdraw from his own event
        val withdrew = repository.withdrawFromGathering(myEvent!!.id)
        assertFalse("Organizer should not be able to withdraw", withdrew)
    }
}
