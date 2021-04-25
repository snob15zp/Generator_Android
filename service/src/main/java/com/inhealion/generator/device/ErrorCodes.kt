package com.inhealion.generator.device

enum class ErrorCodes(value: Int) {
    /// <summary>
    /// Нет ошибки
    /// </summary>
    NO_ERROR(0),

    /// <summary>
    /// Ошибка коммуникация
    /// </summary>
    COMMUNICATION_ERROR(0x81),

    /// <summary>
    /// Ошибка файла
    /// </summary>
    FILE_ERROR(0x82),

    /// <summary>
    /// Ошибка файловой системы
    /// </summary>
    FILE_SYSTEM_ERROR(0x83),

    /// <summary>
    /// Недостаточно места
    /// </summary>
    SPACE_ERROR(0x84),

    /// <summary>
    /// Критическая ошибка
    /// </summary>
    FATAL_ERROR(0x85),

    CANCELED(0xff),
}
