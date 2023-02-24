package com.appcues.mocks

import com.appcues.Storage
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot

internal fun storageMockk(): Storage = mockk(relaxed = true) {
    val userIdSlot = slot<String>().apply { captured = "" }
    // mutableListOf below is a workaround for capturing optional type values
    // https://github.com/mockk/mockk/issues/293#issuecomment-774785539
    val groupIdSlot = mutableListOf<String>()
    val isAnonymousSlot = slot<Boolean>().apply { captured = true }
    val userSignatureSlot = mutableListOf<String>()
    every { this@mockk.userId = capture(userIdSlot) } just runs
    every { this@mockk.userId } answers { userIdSlot.captured }
    every { this@mockk.groupId = capture(groupIdSlot) } just runs
    every { this@mockk.groupId } answers { groupIdSlot.firstOrNull() }
    every { this@mockk.isAnonymous = capture(isAnonymousSlot) } just runs
    every { this@mockk.isAnonymous } answers { isAnonymousSlot.captured }
    every { this@mockk.deviceId } returns "test_device_id"
    every { this@mockk.userSignature = capture(userSignatureSlot) } just runs
    every { this@mockk.userSignature } answers { userSignatureSlot.firstOrNull() }
}
