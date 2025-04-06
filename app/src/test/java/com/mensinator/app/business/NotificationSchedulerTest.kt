package com.mensinator.app.business

import android.content.Context
import com.mensinator.app.business.notifications.IAndroidNotificationScheduler
import com.mensinator.app.business.notifications.NotificationScheduler
import com.mensinator.app.data.Setting
import com.mensinator.app.settings.IntSetting
import com.mensinator.app.settings.StringSetting
import com.mensinator.app.ui.ResourceMapper
import com.mensinator.app.utils.IDispatcherProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.TimeZone

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class NotificationSchedulerTest {

    @RelaxedMockK
    private lateinit var context: Context

    @MockK
    private lateinit var dbHelper: IPeriodDatabaseHelper

    @MockK
    private lateinit var periodPrediction: IPeriodPrediction

    @RelaxedMockK
    private lateinit var androidNotificationScheduler: IAndroidNotificationScheduler

    @MockK
    private lateinit var dispatcherProvider: IDispatcherProvider

    private lateinit var notificationScheduler: NotificationScheduler
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { dispatcherProvider.IO } returns testDispatcher
        every { dispatcherProvider.Main } returns testDispatcher

        notificationScheduler = NotificationScheduler(
            context,
            dbHelper,
            periodPrediction,
            dispatcherProvider,
            androidNotificationScheduler
        )

        mockkObject(ResourceMapper)

        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `schedule notification with valid parameters`() = runTest(testDispatcher) {
        // Given
        val reminderDays = 2
        val today = LocalDate.now()
        val predictedDate = today.plusDays(5)
        val expectedNotificationDate = predictedDate.minusDays(reminderDays.toLong())
        val customMessage = "Custom period message"
        val mappedMessage = "Your period is coming in 2 days"

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns mockk<Setting> {
            every { value } returns reminderDays.toString()
        }
        every { periodPrediction.getPredictedPeriodDate() } returns predictedDate
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns customMessage
        every {
            ResourceMapper.getPeriodReminderMessage(customMessage, any())
        } returns mappedMessage

        // When
        notificationScheduler.schedulePeriodNotification()

        verify {
            androidNotificationScheduler.scheduleNotification(
                mappedMessage,
                expectedNotificationDate
            )
        }
    }

    @Test
    fun `do not schedule notification when reminder days is zero`() = runTest(testDispatcher) {
        // Given
        val reminderDays = 0
        val predictedDate = LocalDate.now().plusDays(5)

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns mockk<Setting> {
            every { value } returns reminderDays.toString()
        }
        every { periodPrediction.getPredictedPeriodDate() } returns predictedDate
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns "message"
        every { ResourceMapper.getPeriodReminderMessage(any(), any()) } returns "mapped message"

        // When
        notificationScheduler.schedulePeriodNotification()

        // Then
        verify { androidNotificationScheduler.cancelScheduledNotification() }
    }

    @Test
    fun `do not schedule notification when reminder date is in the past`() = runTest(testDispatcher) {
        // Given
        val reminderDays = 3
        val today = LocalDate.now()
        val predictedDate = today.plusDays(2) // This makes the reminder date in the past

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns mockk<Setting> {
            every { value } returns reminderDays.toString()
        }
        every { periodPrediction.getPredictedPeriodDate() } returns predictedDate
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns "message"
        every {
            ResourceMapper.getPeriodReminderMessage(any(), any())
        } returns "mapped message"

        // When
        notificationScheduler.schedulePeriodNotification()

        // Then
        verify { androidNotificationScheduler.cancelScheduledNotification() }
    }

    @Test
    fun `do not schedule notification when predicted date is null`() = runTest(testDispatcher) {
        // Given
        val reminderDays = 2

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns mockk<Setting> {
            every { value } returns reminderDays.toString()
        }
        every { periodPrediction.getPredictedPeriodDate() } returns null
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns "message"
        every { ResourceMapper.getPeriodReminderMessage(any(), any()) } returns "mapped message"

        // When
        notificationScheduler.schedulePeriodNotification()

        // Then
        verify { androidNotificationScheduler.cancelScheduledNotification() }
    }

    @Test
    fun `use default reminder days when setting is null`() = runTest(testDispatcher) {
        // Given
        val defaultReminderDays = 2
        val today = LocalDate.now()
        val predictedDate = today.plusDays(5)
        val expectedNotificationDate = predictedDate.minusDays(defaultReminderDays.toLong())
        val message = "message"

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns null
        every { periodPrediction.getPredictedPeriodDate() } returns predictedDate
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns message
        every { ResourceMapper.getPeriodReminderMessage(any(), any()) } returns message

        // When
        notificationScheduler.schedulePeriodNotification()

        // Then
        verify {
            androidNotificationScheduler.scheduleNotification(message, expectedNotificationDate)
        }
    }

    @Test
    fun `use default reminder days when setting is not a number`() = runTest(testDispatcher) {
        // Given
        val defaultReminderDays = 2
        val today = LocalDate.now()
        val predictedDate = today.plusDays(5)
        val expectedNotificationDate = predictedDate.minusDays(defaultReminderDays.toLong())
        val message = "message"

        coEvery { dbHelper.getSettingByKey(IntSetting.REMINDER_DAYS.settingDbKey) } returns mockk<Setting> {
            every { value } returns "not a number"
        }
        every { periodPrediction.getPredictedPeriodDate() } returns predictedDate
        coEvery { dbHelper.getStringSettingByKey(StringSetting.PERIOD_NOTIFICATION_MESSAGE.settingDbKey) } returns message
        every { ResourceMapper.getPeriodReminderMessage(any(), any()) } returns message

        // When
        notificationScheduler.schedulePeriodNotification()

        // Then
        verify {
            androidNotificationScheduler.scheduleNotification(
                message,
                expectedNotificationDate
            )
        }
    }
}
