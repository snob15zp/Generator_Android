package com.inhealion.generator.data.repository

import com.inhealion.generator.data.model.VersionInfo

class VersionInfoRepositoryImpl :
    SingleEntityRepository<VersionInfo>("VersionUpdateInfoRepository"),
    VersionInfoRepository
