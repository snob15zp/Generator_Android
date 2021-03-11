package com.inhealion.generator.data.repository

import com.inhealion.generator.device.model.BleDevice


internal class DeviceRepositoryImpl : SingleEntityRepository<BleDevice>("DeviceRepository"), DeviceRepository
