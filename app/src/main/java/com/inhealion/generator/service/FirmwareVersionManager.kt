package com.inhealion.generator.service

import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient

class FirmwareVersionManager(
    private val api: GeneratorApiCoroutinesClient,
    private val deviceRepository: DeviceRepository,
    private val connectionFactory: DeviceConnectionFactory
) {


}
