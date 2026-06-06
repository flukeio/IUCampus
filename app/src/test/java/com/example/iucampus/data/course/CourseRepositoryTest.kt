package com.example.iucampus.data.course

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class CourseRepositoryTest {

    private lateinit var repository: CourseRepository
    private val context = mockk<Context>()
    private val assets = mockk<AssetManager>()

    private val mockJson = """
        {
          "program": { "name": "B.Sc. Computer Science", "total_credits": 180 },
          "semesters": [
            {
              "semester": 1,
              "modules": [
                {
                  "module_name": "Introduction",
                  "credits": 5,
                  "courses": [
                    { "course_code": "C1", "course_name": "Course 1", "description": "Desc 1" }
                  ]
                },
                {
                  "module_name": "Advanced",
                  "credits": 21,
                  "courses": [
                    { "course_code": "C2", "course_name": "Course 2", "description": "Desc 2" }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    @Before
    fun setUp() {
        every { context.assets } returns assets
        every { assets.open("courses.json") } returns ByteArrayInputStream(mockJson.toByteArray())
        
        // Reset the companion object state via reflection if needed, 
        // but for this test we'll just instantiate and let it initialize.
        repository = CourseRepository(context)
    }

    @Test
    fun `test enrollInCourse enforces 25 CP limit`() = runBlocking {
        // C1 is 5 credits, C2 is 21 credits. Total = 26 (exceeds 25)
        
        // Clear enrolled courses for test isolation (companion object persists)
        val currentEnrolled = repository.getCourses().first()
        currentEnrolled.forEach { repository.dropCourse(it.id) }
        
        // Enroll in C1 (5 CP)
        repository.enrollInCourse("C1")
        val enrolledAfterC1 = repository.getCourses().first()
        assertTrue("Should be enrolled in C1", enrolledAfterC1.any { it.id == "C1" })
        assertEquals(5, enrolledAfterC1.sumOf { it.credits })

        // Try to enroll in C2 (21 CP) -> Total would be 26
        repository.enrollInCourse("C2")
        val enrolledAfterC2 = repository.getCourses().first()
        assertFalse("Should NOT be enrolled in C2 (26 > 25)", enrolledAfterC2.any { it.id == "C2" })
        assertEquals("Total credits should remain 5", 5, enrolledAfterC2.sumOf { it.credits })
    }

    @Test
    fun `test dropCourse removes course correctly`() = runBlocking {
        val currentEnrolled = repository.getCourses().first()
        currentEnrolled.forEach { repository.dropCourse(it.id) }

        repository.enrollInCourse("C1")
        assertTrue(repository.getCourses().first().any { it.id == "C1" })

        repository.dropCourse("C1")
        assertFalse(repository.getCourses().first().any { it.id == "C1" })
    }
}
