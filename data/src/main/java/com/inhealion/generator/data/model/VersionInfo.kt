package com.inhealion.generator.data.model

import java.util.*

data class VersionInfo(
    val latestVersion: String?,
    val deviceVersion: String?,
    val lastCheckAt: Date
) {
    val isUpdateRequired: Boolean get() = getVersionCode(latestVersion) - getVersionCode(deviceVersion) > 0
}


private fun getVersionCode(version: String?): Int {
    version ?: return 0
    var code = 0
    version.split(".").forEachIndexed { index, value ->
        code += StrictMath.pow(10.0, index.toDouble()).toInt() * (value.toIntOrNull() ?: 0)
    }
    return code
}
